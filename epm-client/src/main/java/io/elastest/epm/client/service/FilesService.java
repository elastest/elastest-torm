package io.elastest.epm.client.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class FilesService {

    @Value("${et.shared.folder}")
    private String sharedFolder;

    public FilesService() {

    }

    public static final Logger logger = LoggerFactory
            .getLogger(FilesService.class);

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

    public File getFileFromResources(String path, String fileName,
            String tmpFolder) throws IOException, URISyntaxException {
        File file = null;

        logger.info("Load file in dev mode");
        Resource resource = new ClassPathResource(path);
        logger.info("Resource loading ok? {}", resource.exists());
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
                    tmpFolder + "/" + fileName);
            logger.info("File loaded");
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

    public void writeFileFromString(String content, File file)
            throws IOException {
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
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

    private TarArchiveOutputStream getTarArchiveOutputStream(String name)
            throws FileNotFoundException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(
                new FileOutputStream(name));
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        return taos;
    }

    private void addToArchiveCompression(TarArchiveOutputStream out, File file,
            String dir) throws IOException {
        String entry = dir;
        if (file.isFile()) {
            entry = File.separator + file.getName();
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            System.out.println(file.getName() + "is not supported");
        }
    }

    public String createTempFolderName(String basePath, String prefix) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd-hh.mm.ss");
        return basePath != null && !basePath.isEmpty()
                ? basePath + File.separator + prefix + formatter.format(date)
                : prefix + formatter.format(date);
    }

}
