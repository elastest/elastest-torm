package io.elastest.etm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import io.elastest.etm.model.TJobExecution;

@Service
public class FilesService {

    @Value("${et.shared.folder}")
    private String sharedFolder;

    public static final Logger logger = LoggerFactory
            .getLogger(FilesService.class);

    public List<File> getFilesFromFolder(String path)
            throws IOException {
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

    public String buildFilesPath(TJobExecution tJobExec, String folder) {
        String path = "";
        if (tJobExec != null && tJobExec.getTjob() != null) {
            Long tJobId = tJobExec.getTjob().getId();
            Long tJobExecId = tJobExec.getId();
            // etmcontextService.getMonitoringEnvVars
            String fileSeparator = "/";
            path = sharedFolder + fileSeparator + ElastestConstants.TJOBS_FOLDER
                    + fileSeparator + ElastestConstants.TJOB_FOLDER_PREFIX
                    + tJobId + fileSeparator
                    + ElastestConstants.TJOB_EXEC_FOLDER_PREFIX + tJobExecId
                    + fileSeparator + folder + fileSeparator;
        }

        return path;
    }

    public void createExecFilesFolder(String path) {
        logger.debug("Try to create folder structure: {}", path);
        File folderStructure = new File(path);

        try {
            if (!folderStructure.exists()) {
                logger.info("creating folder at {}.",
                        folderStructure.getAbsolutePath());
                folderStructure.mkdirs();
                logger.info("Folder created.");
            }
        } catch (Exception e) {
            logger.error("File does not created.");
            throw e;
        }
    }

}
