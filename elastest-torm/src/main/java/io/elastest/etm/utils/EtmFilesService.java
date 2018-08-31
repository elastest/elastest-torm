package io.elastest.etm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.external.ExternalTJobExecution;

@Service
public class EtmFilesService {

    @Value("${et.shared.folder}")
    private String sharedFolder;

    public EtmFilesService() {

    }

    public static final Logger logger = LoggerFactory
            .getLogger(EtmFilesService.class);

    public List<File> getFilesFromFolder(String path) throws IOException {
        logger.info("Get files inside the folder: {}", path);
        List<File> files = new ArrayList<>();
        try {
            File file = ResourceUtils.getFile(path);
            // If not in dev mode
            if (file.exists()) {
                List<String> filesNames = new ArrayList<>(
                        Arrays.asList(file.list()));
                for (String nameOfFile : filesNames) {
                    logger.debug("File name: {}", nameOfFile);
                    File serviceFile = ResourceUtils
                            .getFile(path + "/" + nameOfFile);
                    files.add(serviceFile);
                }
            } else { // Dev mode
                Resource resource = new ClassPathResource(path);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(resource.getInputStream()),
                        1024)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.debug("File name (dev mode):" + line);
                        File serviceFile = new ClassPathResource(path + line)
                                .getFile();
                        files.add(serviceFile);
                    }
                } catch (IOException ioe) {
                    logger.warn(
                            "Error reading the files. The file with the path "
                                    + path + " does not exist:");
                    throw ioe;
                }
            }

            return files;

        } catch (IOException ioe) {
            logger.warn("Error reading the files. The file with the path "
                    + path + " does not exist:");
            throw ioe;
        }
    }

    public File getFileFromResources(String path, String fileName)
            throws IOException, URISyntaxException {
        File file = null;
        try {
            logger.info("Load file in dev mode");
            Resource resource = new ClassPathResource(path);
            logger.info("Resource loading ok");
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream()), 1024)) {
                String line;
                while ((line = br.readLine()) != null) {
                    logger.info("File name (dev mode):" + line);
                    if (line.equals(fileName)) {
                        file = new ClassPathResource(path + line).getFile();
                        return file;
                    }
                }
            }

            if (file == null || !file.exists()) {
                logger.info("Load file prod mode");
                file = getFileFromJarFile("/" + path + fileName,
                        sharedFolder + "/tmp/" + fileName);
                logger.info("File loaded");
            }

        } catch (IOException ioe) {
            logger.error("Error reading the files. The file with the path "
                    + path + " does not exist:");
            try {
                file = getFileFromJarFile("/" + path + fileName,
                        sharedFolder + "/tmp/" + fileName);
            } catch (Exception e) {
                throw e;
            }
        }
        return file;
    }

    public File getFileFromJarFile(String sourcePath, String targetPath)
            throws IOException {
        InputStream iStream = getFileContentAsInputStream(sourcePath);
        return createFileFromInputStream(iStream, targetPath);
    }

    public InputStream getFileContentAsInputStream(String path) {
        InputStream fileAsInputStream = getClass().getResourceAsStream(path);
        return fileAsInputStream;
    }

    public File createFileFromInputStream(InputStream iStream,
            String targetPath) throws IOException {
        File file = new File(targetPath);
        FileUtils.copyInputStreamToFile(iStream, file);
        return ResourceUtils.getFile(targetPath);
    }

    public File createFileFromString(String string, String targetPath)
            throws IOException {
        File file = new File(targetPath);
        FileUtils.writeStringToFile(file, string, StandardCharsets.UTF_8);
        return ResourceUtils.getFile(targetPath);
    }

    public String readFile(File file) throws IOException {
        String content = null;

        if (!file.isDirectory()) {
            try {
                content = new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                logger.error("Error reading the content of the file {}",
                        file.getName());
                throw e;
            }
        }

        return content;
    }

    public String buildExecutionFilesPath(Long tJobId, Long tJobExecId,
            boolean isExternal, String folder) {
        String path = "";

        // etmcontextService.getMonitoringEnvVars
        String fileSeparator = "/";
        String parsedSharedFolder = sharedFolder;
        if (parsedSharedFolder.endsWith(fileSeparator)) {
            parsedSharedFolder = parsedSharedFolder.substring(0,
                    parsedSharedFolder.length() - 1);
        }

        String tJobsFolder = ElastestConstants.TJOBS_FOLDER;
        String tJobFolder = ElastestConstants.TJOB_FOLDER_PREFIX;
        String tJobExecFolder = ElastestConstants.TJOB_EXEC_FOLDER_PREFIX;

        if (isExternal) {
            tJobsFolder = ElastestConstants.EXTERNAL_TJOBS_FOLDER;
            tJobFolder = ElastestConstants.EXTERNAL_TJOB_FOLDER_PREFIX;
            tJobExecFolder = ElastestConstants.EXTERNAL_TJOB_EXEC_FOLDER_PREFIX;
        }

        path = parsedSharedFolder + fileSeparator + tJobsFolder + fileSeparator
                + tJobFolder + tJobId + fileSeparator + tJobExecFolder
                + tJobExecId + fileSeparator + folder + fileSeparator;

        return path;
    }

    public String buildTJobFilesPath(TJobExecution tJobExec, String folder) {
        String path = "";
        if (tJobExec != null && tJobExec.getTjob() != null) {
            Long tJobId = tJobExec.getTjob().getId();
            Long tJobExecId = tJobExec.getId();
            path = buildExecutionFilesPath(tJobId, tJobExecId, false, folder);
        }

        return path;
    }

    public String buildExternalTJobFilesPath(
            ExternalTJobExecution externalTJobExec, String folder) {
        String path = "";
        if (externalTJobExec != null && externalTJobExec.getExTJob() != null) {
            Long externalTJobId = externalTJobExec.getExTJob().getId();
            Long externalTJobExecId = externalTJobExec.getId();
            path = buildExecutionFilesPath(externalTJobId, externalTJobExecId,
                    true, folder);
        }

        return path;
    }

    public void createExecFilesFolder(String path) {
        logger.debug("Try to create folder structure: {}", path);
        File folderStructure = new File(path);

        if (!folderStructure.exists()) {
            logger.info("creating folder at {}.",
                    folderStructure.getAbsolutePath());
            boolean created = folderStructure.mkdirs();
            if (!created) {
                logger.error("File does not created.");
                return;
            }
            logger.info("Folder created.");
        }

    }

    public void removeExecFilesFolder(String path) throws IOException {
        logger.debug("Try to remove folder structure: {}", path);
        File folderStructure = new File(path);

        if (folderStructure.exists()) {
            logger.info("removing folder at {}.",
                    folderStructure.getAbsolutePath());
            try {
                FileUtils.deleteDirectory(folderStructure);
            } catch (IOException e) {
                logger.error("File does not removed.");
                throw e;
            }
            logger.info("Folder removed.");
        }

    }

}
