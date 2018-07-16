package io.elastest.epm.client.dockercompose;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

/**
 * Container which launches Docker Compose, for the purposes of launching a
 * defined set of containers.
 */
public class DockerComposeContainer<SELF extends DockerComposeContainer<SELF>> {
    final Logger logger = getLogger(lookup().lookupClass());

    private final String identifier;
    private final List<File> composeFiles;
    private List<String> composeYmlList = new ArrayList<>();

    private final Map<String, Integer> scalingPreferences = new HashMap<>();
    private boolean pull = false;

    private String project;
    private static final Object MUTEX = new Object();
    private Map<String, String> env = new HashMap<>();

    private List<String> imagesList = new ArrayList<>();
    boolean started = false;

    /* ******************** */
    /* *** Constructors *** */
    /* ******************** */

    public DockerComposeContainer(File... composeFiles) {
        this(Arrays.asList(composeFiles));
    }

    public DockerComposeContainer(List<File> composeFiles) {
        this(Base58.randomString(6).toLowerCase(), true, composeFiles);
    }

    public DockerComposeContainer(String identifier, File... composeFiles) {
        this(identifier, false, Arrays.asList(composeFiles));
    }

    public DockerComposeContainer(String identifier, boolean withUniqueId,
            File... composeFiles) {
        this(identifier, withUniqueId, Arrays.asList(composeFiles));
    }

    public DockerComposeContainer(String identifier, boolean withUniqueId,
            List<File> composeFiles) {
        this.composeFiles = composeFiles;
        this.identifier = identifier;

        if (withUniqueId) {
            project = randomProjectId();
        } else {
            project = identifier;
        }
    }

    /* ***************** */
    /* **** Methods **** */
    /* ***************** */

    public void start() throws InterruptedException {
        synchronized (MUTEX) {
            if (pull) {
                runWithCompose("pull");
            }
            // scale before up, so that all scaled instances are available first
            // for linking
            applyScaling();
            // Run the docker-compose container, which starts up the services
            ProcessResult result = runWithCompose("up -d");
            this.started = result.getExitValue() == 0;
        }
    }

    private ProcessResult runWithCompose(String cmd) {
        final DockerCompose dockerCompose;
        dockerCompose = new LocalDockerCompose(composeFiles, project);
        return dockerCompose.withCommand(cmd).withEnv(env).invoke();
    }

    private void applyScaling() {
        // Apply scaling
        if (!scalingPreferences.isEmpty()) {
            StringBuilder sb = new StringBuilder("scale");
            for (Map.Entry<String, Integer> scale : scalingPreferences
                    .entrySet()) {
                sb.append(" ").append(scale.getKey()).append("=")
                        .append(scale.getValue());
            }

            runWithCompose(sb.toString());
        }
    }

    public void stop() {
        synchronized (MUTEX) {
            try {
                // Kill the services using docker-compose
                runWithCompose("down -v");
            } finally {
                project = randomProjectId();
            }
        }
    }

    public SELF withScaledService(String serviceBaseName, int numInstances) {
        scalingPreferences.put(serviceBaseName, numInstances);

        return self();
    }

    public SELF withEnv(String key, String value) {
        env.put(key, value);
        return self();
    }

    public SELF withEnv(Map<String, String> env) {
        env.forEach(this.env::put);
        return self();
    }

    public SELF withImages(List<String> images) {
        this.imagesList = images;
        return self();
    }

    public List<String> getImagesList() {
        return this.imagesList;
    }

    public List<File> getComposeFiles() {
        return composeFiles;
    }

    public SELF setComposeYmlList(List<String> ymlList) {
        if (ymlList != null) {
            this.composeYmlList = ymlList;
        }
        return self();
    }

    public List<String> getComposeYmlList() {
        return this.composeYmlList;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Whether to pull images first.
     *
     * @return this instance, for chaining
     */
    public SELF withPull(boolean pull) {
        this.pull = pull;
        return self();
    }

    @SuppressWarnings("unchecked")
    private SELF self() {
        return (SELF) this;
    }

    private String randomProjectId() {
        return identifier + Base58.randomString(6).toLowerCase();
    }
}

interface DockerCompose {
    String ENV_PROJECT_NAME = "COMPOSE_PROJECT_NAME";
    String ENV_COMPOSE_FILE = "COMPOSE_FILE";

    DockerCompose withCommand(String cmd);

    DockerCompose withEnv(Map<String, String> env);

    ProcessResult invoke();

    default void validateFileList(List<File> composeFiles) {
        checkNotNull(composeFiles);
        checkArgument(!composeFiles.isEmpty(),
                "No docker compose file have been provided");
    }
}

/**
 * Use local Docker Compose binary, if present.
 */
class LocalDockerCompose implements DockerCompose {
    final Logger logger = getLogger(lookup().lookupClass());

    /**
     * Executable name for Docker Compose.
     */
    public static final char UNIX_PATH_SEPERATOR = ':';

    private static final String COMPOSE_EXECUTABLE = SystemUtils.IS_OS_WINDOWS
            ? "docker-compose.exe"
            : "docker-compose";

    private final List<File> composeFiles;
    private final String identifier;
    private String cmd = "";
    private Map<String, String> env = new HashMap<>();

    public LocalDockerCompose(List<File> composeFiles, String identifier) {
        validateFileList(composeFiles);

        this.composeFiles = composeFiles;
        this.identifier = identifier;
    }

    public DockerCompose withCommand(String cmd) {
        this.cmd = cmd;
        return this;
    }

    public DockerCompose withEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public ProcessResult invoke() {
        // bail out early
        if (!CommandLine.executableExists(COMPOSE_EXECUTABLE)) {
            throw new ContainerLaunchException(
                    "Local Docker Compose not found. Is " + COMPOSE_EXECUTABLE
                            + " on the PATH?");
        }

        final Map<String, String> environment = Maps.newHashMap(env);
        environment.put(ENV_PROJECT_NAME, identifier);
        final List<String> absoluteDockerComposeFiles = composeFiles.stream()
                .map(File::getAbsolutePath).collect(toList());

        final String composeFileEnvVariableValue = absoluteDockerComposeFiles
                .stream().collect(joining(UNIX_PATH_SEPERATOR + ""));

        logger.debug("Set env COMPOSE_FILE={}", composeFileEnvVariableValue);

        final File pwd = composeFiles.get(0).getAbsoluteFile().getParentFile()
                .getAbsoluteFile();
        environment.put(ENV_COMPOSE_FILE, composeFileEnvVariableValue);

        logger.info("Local Docker Compose is running command: {}", cmd);

        final List<String> command = Splitter.onPattern(" ").omitEmptyStrings()
                .splitToList(COMPOSE_EXECUTABLE + " " + cmd);

        try {
            ProcessResult processResult = new ProcessExecutor().command(command)
                    .readOutput(true)
                    .redirectOutput(Slf4jStream.of(logger).asInfo())
                    .redirectError(Slf4jStream.of(logger).asError())
                    .environment(environment).directory(pwd).exitValueNormal()
                    .executeNoTimeout();

            logger.info("Docker Compose has finished running");

            return processResult;
        } catch (InvalidExitValueException e) {
            throw new ContainerLaunchException(
                    "Local Docker Compose exited abnormally with code "
                            + e.getExitValue() + " whilst running command: "
                            + cmd);

        } catch (Exception e) {
            throw new ContainerLaunchException(
                    "Error running local Docker Compose command: " + cmd, e);
        }
    }

}
