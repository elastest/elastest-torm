package io.elastest.epm.client.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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

    public List<File> getFilesFromResources(String path) throws IOException {
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

    public List<File> getFilesFromResources1(String path, String tmpFolder)
            throws IOException, URISyntaxException {
        List<File> files = new ArrayList<>();
        boolean inProd = false;
        logger.info("Load files in dev mode. Path {}, target path {}", path,
                tmpFolder);
        Resource resource = new ClassPathResource(path);
        logger.info("Resource loading ok? {}", resource.exists());
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(resource.getInputStream()), 1024)) {
            String line;
            while ((line = br.readLine()) != null) {
                logger.info("File name (dev mode):" + line);
                File file = new ClassPathResource(path + line).getFile();
                if (files.size() == 0 && (file == null || !file.exists())) {
                    inProd = true;
                    break;
                }
                files.add(file);
            }
        }

        if (inProd) {
            logger.info("Load files prod mode");
            files = getFilesFromJarFile("/" + path + "/", tmpFolder + "/");
            logger.info("Files loaded");
        }
        return files;
    }

    public File getFileFromResources(String path, String fileName,
            String tmpFolder) throws IOException, URISyntaxException {
        File file = null;

        logger.info("Load file in dev mode. Path {}, file {}, target path {}",
                path, fileName, tmpFolder);
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
        InputStream iStream = getResourceAsInputStream(sourcePath);
        return createFileFromInputStream(iStream, targetPath);
    }

    public List<File> getFilesFromJarFile(String sourcePath, String targetPath)
            throws IOException {
        InputStream iStream = getResourceAsInputStream(sourcePath);
        return createFilesFromInputStream(iStream, targetPath);
    }

    public InputStream getResourceAsInputStream(String path) {
        final InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path);

        return in == null ? getClass().getResourceAsStream(path) : in;
    }

    public File createFileFromInputStream(InputStream iStream,
            String targetPath) throws IOException {
        File file = new File(targetPath);
        FileUtils.copyInputStreamToFile(iStream, file);
        return ResourceUtils.getFile(targetPath);
    }

    public List<File> createFilesFromInputStream(InputStream iStream,
            String targetPath) throws IOException {
        List<File> files = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(iStream), 1024)) {
            String line;
            while ((line = br.readLine()) != null) {
                File file = createFileFromString(line, targetPath);
                files.add(file);
            }
        }
        return files;
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

    public void saveFileInPath(File file, String path) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());

        OutputStream out = new FileOutputStream(new File(path));
        out.write(fileContent);
        out.close();
    }

    public void saveFilesInPath(List<File> files, String path)
            throws IOException {
        for (File file : files) {
            saveFileInPath(file, path);
        }
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

    public File createTarFile(String path, File... files) throws IOException {
        logger.info("Directory path to cormpress: {}", path);
        logger.info("Init tar compression.");
        try (TarArchiveOutputStream out = getTarArchiveOutputStream(path)) {
            for (File file : files) {
                addToArchiveCompression(out, file, ".");
            }
        }

        logger.info("Path of the tar file: {}",
                files[0].getAbsolutePath() + ".tar");
        return new File(files[0].getAbsolutePath() + ".tar");
    }

    private TarArchiveOutputStream getTarArchiveOutputStream(String name)
            throws FileNotFoundException {
        logger.info("Creating output for the tar file.");
        TarArchiveOutputStream taos = new TarArchiveOutputStream(
                new FileOutputStream(name));
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        return taos;
    }

    private void addToArchiveCompression(TarArchiveOutputStream out, File file,
            String dir) throws IOException {
        logger.info("Add files to the tar file.");
        String entry = dir;
        if (file.isFile()) {
            entry = file.getName();
            logger.info("File to add to the tar file: {}", entry);
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            logger.info("Directory to compress: {}", file.getName());
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            logger.warn("{} is not supported", file.getName());
        }
    }

    public String createTempFolderName(String basePath, String prefix) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd-hh.mm.ss");
        return basePath != null && !basePath.isEmpty()
                ? basePath + prefix + formatter.format(date)
                : prefix + formatter.format(date);
    }

    private void unTar(TarArchiveInputStream tis, File destFolder)
            throws IOException {
        TarArchiveEntry entry = null;
        while ((entry = tis.getNextTarEntry()) != null) {
            FileOutputStream fos = null;
            try {
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(destFolder, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                fos = new FileOutputStream(curfile);
                IOUtils.copy(tis, fos);
            } catch (Exception e) {
                logger.warn("Exception extracting recording {} to {}", tis,
                        destFolder, e);
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.getFD().sync();
                        fos.close();
                    }
                } catch (IOException e) {
                    logger.warn("Exception closing {}", fos, e);
                }
            }
        }
    }

    public InputStream getInputStreamFromTarArchive(
            TarArchiveInputStream tarInput, TarArchiveEntry currentEntry)
            throws IOException {
        // black magic because currentEntry.getFile() is null
        byte[] buf = new byte[(int) currentEntry.getSize()];
        IOUtils.readFully(tarInput, buf);
        return new ByteArrayInputStream(buf);
    }

    public List<InputStream> getFilesFromTarInputStreamAsInputStreamList(
            TarArchiveInputStream tarInput, String fullPath,
            List<String> filterExtensions) throws Exception {
        List<InputStream> files = new ArrayList<>();

        TarArchiveEntry currentEntry = tarInput.getNextTarEntry();

        while (currentEntry != null) {
            boolean addFile = true;
            if (currentEntry.isFile()) {
                if (filterExtensions != null) {
                    String fileName = currentEntry.getName();
                    String[] splittedFileName = fileName != null
                            ? fileName.split("\\.")
                            : null;
                    String fileExtension = splittedFileName != null
                            && splittedFileName.length > 1
                                    ? splittedFileName[splittedFileName.length
                                            - 1]
                                    : null;

                    if (fileExtension != null) {
                        addFile = filterExtensions.stream().anyMatch(
                                str -> str.trim().equals(fileExtension));
                    } else {
                        addFile = false;
                    }

                }

                if (addFile) {
                    files.add(getInputStreamFromTarArchive(tarInput,
                            currentEntry));
                }

            } else if (currentEntry.isDirectory()) {
                // TODO
            }
            currentEntry = tarInput.getNextTarEntry();
        }
        return files;
    }

    public InputStream getSingleFileFromTarInputStream(
            TarArchiveInputStream tarInput, String containerNameOrId,
            String fullFilePath) throws Exception {

        TarArchiveEntry currentEntry = tarInput.getNextTarEntry();

        Path p = Paths.get(fullFilePath);
        String fileName = p.getFileName().toString();

        while (fileName != null && currentEntry != null) {
            if (currentEntry.isFile()
                    && fileName.equals(currentEntry.getName())) {
                return getInputStreamFromTarArchive(tarInput, currentEntry);
            }
            currentEntry = tarInput.getNextTarEntry();
        }
        return null;
    }
}
