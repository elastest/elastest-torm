package io.elastest.etm.utils;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.ElastestFile;
import io.elastest.etm.model.external.ExternalTJobExecution;

@Service
public class EtmFilesService {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.shared.folder}")
    private String sharedFolder;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${registry.contextPath}")
    private String registryContextPath;

    public static final String FILE_SEPARATOR = "/";

    public static final String TJOBS_FOLDER = "tjobs";
    public static final String TJOB_FOLDER_PREFIX = "tjob_";
    public static final String TJOB_EXEC_FOLDER_PREFIX = "exec_";

    public static final String EXTERNAL_TJOBS_FOLDER = "external_tjobs";
    public static final String EXTERNAL_TJOB_FOLDER_PREFIX = "external_tjob_";
    public static final String EXTERNAL_TJOB_EXEC_FOLDER_PREFIX = "external_exec_";

    private static final String EXEC_ATTACHMENTS_FOLDER = "attachments";

    public EtmFilesService() {
    }

    /* **************************************************************** */
    /* ********************* Elastest data folder ********************* */
    /* **************************************************************** */

    public List<ElastestFile> getElastestFilesUrls(String fileSeparator,
            String relativeFilePath) throws InterruptedException, IOException {
        List<ElastestFile> filesList = new ArrayList<ElastestFile>();

        String tJobExecFolder = sharedFolder.endsWith(fileSeparator)
                ? sharedFolder
                : sharedFolder + fileSeparator;
        tJobExecFolder += relativeFilePath;
        logger.debug("Shared folder: " + tJobExecFolder);

        File file = ResourceUtils.getFile(tJobExecFolder);

        if (file.exists()) {
            List<String> folders = new ArrayList<>(Arrays.asList(file.list()));
            for (String folderName : folders) {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ie) {
                    logger.error("Thread sleep fail");
                    throw ie;
                }
                logger.debug("Files folder:" + folderName);
                String fullPathFolder = tJobExecFolder + folderName;
                logger.debug("Full path:" + fullPathFolder);
                File folder = ResourceUtils.getFile(fullPathFolder);

                filesList.addAll(this.getElastestFilesByFolder(folder,
                        relativeFilePath + folderName + fileSeparator,
                        folderName, fileSeparator));
            }
        }

        return filesList;
    }

    public List<ElastestFile> getElastestFilesByFolder(File folder,
            String relativePath, String folderName, String fileSeparator)
            throws IOException {
        String absolutePath = sharedFolder + relativePath;
        if (sharedFolder.endsWith("/") && relativePath.startsWith("/")) {
            absolutePath = sharedFolder + relativePath.replaceFirst("/", "");
        } else {
            if (!sharedFolder.endsWith("/") && !relativePath.startsWith("/")) {
                absolutePath = sharedFolder + "/" + relativePath;
            }
        }
        List<ElastestFile> filesList = new ArrayList<ElastestFile>();

        List<String> folderFilesNames = new ArrayList<>(
                Arrays.asList(folder.list()));

        for (String currentFileName : folderFilesNames) {
            String absoluteFilePath = absolutePath + currentFileName;
            String relativeFilePath = relativePath + currentFileName;
            File currentFile = ResourceUtils.getFile(absoluteFilePath);
            if (currentFile.isDirectory()) {
                filesList.addAll(this.getElastestFilesByFolder(currentFile,
                        relativeFilePath + fileSeparator, folderName,
                        fileSeparator));
            } else {
                String encodedCurrentFileName = URLEncoder
                        .encode(currentFileName, "UTF-8");
                String relativeEncodedFilePath = relativePath
                        + encodedCurrentFileName;

                filesList.add(new ElastestFile(currentFileName,
                        getElastestFileUrl(relativeFilePath),
                        getElastestFileUrl(relativeEncodedFilePath),
                        folderName));
            }
        }
        return filesList;
    }

    public String getElastestFileUrl(String serviceFilePath)
            throws IOException {
        String urlResponse = contextPath.replaceFirst("/", "")
                + registryContextPath + "/"
                + serviceFilePath.replace("\\\\", "/");
        return urlResponse;
    }

    public File getElastestFileFromResources(String path, String fileName)
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

    /* *************************************************************** */
    /* ************************** TJob Exec ************************** */
    /* *************************************************************** */

    public String getTJobExecFolderPath(TJobExecution tJobExec,
            boolean relativePath) {

        String path = (relativePath ? "" : sharedFolder) + FILE_SEPARATOR
                + TJOBS_FOLDER + FILE_SEPARATOR + TJOB_FOLDER_PREFIX
                + tJobExec.getTjob().getId() + FILE_SEPARATOR
                + TJOB_EXEC_FOLDER_PREFIX + tJobExec.getId() + FILE_SEPARATOR;
        return path;
    }

    public String getTJobExecFolderPath(TJobExecution tJobExec) {
        return getTJobExecFolderPath(tJobExec, false);
    }

    public List<ElastestFile> getTJobExecFilesUrls(Long tJobId, Long tJobExecId)
            throws InterruptedException {
        logger.info("Retrived the files generated by the TJob execution: {}",
                tJobExecId);

        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        String tJobExecFilePath = TJOBS_FOLDER + fileSeparator
                + TJOB_FOLDER_PREFIX + tJobId + fileSeparator
                + TJOB_EXEC_FOLDER_PREFIX + tJobExecId + fileSeparator;

        List<ElastestFile> filesList = null;
        try {
            filesList = this.getElastestFilesUrls(fileSeparator,
                    tJobExecFilePath);
        } catch (IOException fnfe) {
            logger.warn("Error building the URLs of the execution files {}",
                    tJobExecId);
        }
        return filesList;
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

        String tJobsFolder = TJOBS_FOLDER;
        String tJobFolder = TJOB_FOLDER_PREFIX;
        String tJobExecFolder = TJOB_EXEC_FOLDER_PREFIX;

        if (isExternal) {
            tJobsFolder = EXTERNAL_TJOBS_FOLDER;
            tJobFolder = EXTERNAL_TJOB_FOLDER_PREFIX;
            tJobExecFolder = EXTERNAL_TJOB_EXEC_FOLDER_PREFIX;
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

    public String getTJobExecAttachmentFilePath(TJobExecution tJobExec,
            boolean relativePath) {
        String attachmentPath = getTJobExecFolderPath(tJobExec, relativePath)
                + EXEC_ATTACHMENTS_FOLDER + FILE_SEPARATOR;

        return attachmentPath;
    }

    public String getTJobExecAttachmentFilePath(TJobExecution tJobExec) {
        return getTJobExecAttachmentFilePath(tJobExec, false);
    }

    public Boolean saveExecAttachmentFile(TJobExecution tJobExec,
            String fileName, MultipartFile multipartFile)
            throws IllegalStateException, IOException {
        String path = getTJobExecAttachmentFilePath(tJobExec);

        return saveMultipartFile(fileName, multipartFile, path, true, false);
    }

    public Boolean saveExecAttachmentFile(TJobExecution tJobExec,
            MultipartFile file) throws IllegalStateException, IOException {
        return saveExecAttachmentFile(tJobExec, file.getOriginalFilename(),
                file);
    }

    /* *************************************************************** */
    /* ************************ External TJob ************************ */
    /* *************************************************************** */

    public List<ElastestFile> getExternalTJobExecutionFilesUrls(Long exTJobId,
            Long exTJobExecId) throws InterruptedException {
        logger.info("Retrived the files generated by the TJob execution: {}",
                exTJobExecId);

        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        String tJobExecFilePath = EXTERNAL_TJOBS_FOLDER + fileSeparator
                + EXTERNAL_TJOB_FOLDER_PREFIX + exTJobId + fileSeparator
                + EXTERNAL_TJOB_EXEC_FOLDER_PREFIX + exTJobExecId
                + fileSeparator;

        List<ElastestFile> filesList = null;
        try {
            filesList = this.getElastestFilesUrls(fileSeparator,
                    tJobExecFilePath);
        } catch (IOException fnfe) {
            logger.warn("Error building the URLs of the execution files {}",
                    exTJobExecId);
        }
        return filesList;
    }

    public String getExternalTJobExecFolderPath(
            ExternalTJobExecution exTJobExec, boolean relativePath) {
        return (relativePath ? "" : sharedFolder) + FILE_SEPARATOR
                + EXTERNAL_TJOBS_FOLDER + FILE_SEPARATOR
                + EXTERNAL_TJOB_FOLDER_PREFIX + exTJobExec.getExTJob().getId()
                + FILE_SEPARATOR + EXTERNAL_TJOB_EXEC_FOLDER_PREFIX
                + exTJobExec.getId() + FILE_SEPARATOR;
    }

    public String getExternalTJobExecFolderPath(
            ExternalTJobExecution exTJobExe) {
        return getExternalTJobExecFolderPath(exTJobExe, false);
    }

    /* *********************************************************** */
    /* ************************* Generic ************************* */
    /* *********************************************************** */

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

    // TODO move
    public void createFolderIfNotExists(String path) {
        File folderStructure = new File(path);

        if (!folderStructure.exists()) {
            logger.debug("Try to create folder structure: {}", path);
            logger.info("Creating folder at {}.",
                    folderStructure.getAbsolutePath());
            boolean created = folderStructure.mkdirs();
            if (!created) {
                logger.error("Folder does not created at {}.", path);
                return;
            }
            logger.info("Folder created at {}.", path);
        }
    }

    // TODO MOVE
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

    public void removeFolder(String folderPath) throws IOException {
        logger.debug("Try to remove folder structure: {}", folderPath);
        File folderStructure = new File(folderPath);

        if (folderStructure.exists() && folderStructure.isDirectory()) {
            logger.info("Removing folder at {}.",
                    folderStructure.getAbsolutePath());
            try {
                FileUtils.deleteDirectory(folderStructure);
            } catch (IOException e) {
                logger.error("Folder has not been removed.");
                throw e;
            }
            logger.info("Folder has been removed.");
        }
    }

    public boolean removeFile(String fileName, String path) throws IOException {
        String completePath = path;
        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        if (!completePath.endsWith(fileSeparator)) {
            completePath += fileSeparator;
        }
        completePath += fileName;
        File file = new File(completePath);
        return file.delete();
    }

    public Boolean saveMultipartFile(String fileName,
            MultipartFile multipartFile, String path,
            boolean createFolderIfNotExists, boolean forceReplace)
            throws IOException {
        if (createFolderIfNotExists) {
            createFolderIfNotExists(path);
        }

        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        if (!path.endsWith(fileSeparator)) {
            path += fileSeparator;
        }
        File file = new File(path + fileName);

        if (file.exists()) {
            if (forceReplace) {
                file.delete();
                file.createNewFile();
            } else {
                return false;
            }
        }

        multipartFile.transferTo(file);
        return true;
    }
}
