package io.elastest.etm.api;

import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.elastest.etm.utils.EtmFilesService;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class EpmApiController implements EpmApi {

    private static final Logger logger = LoggerFactory
            .getLogger(EpmApiController.class);
    @Autowired
    EtmFilesService etmFilesService;

    private static final String CLUSTER_TAR_FILE_NAME = "ansible-cluster.tar";
    private static final String NODE_TAR_FILE_NAME = "ansible-node.tar";

    @Value("${et.shared.folder}")
    String etSharedFolder;

    @Value("${et.epm.packages.path}")
    String etEpmPackagesPath;

    String epmPackagescompletePath;

    @PostConstruct
    private void init() {
        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        epmPackagescompletePath = etSharedFolder;
        if (!epmPackagescompletePath.endsWith(fileSeparator)) {
            epmPackagescompletePath += fileSeparator;
        }

        epmPackagescompletePath += etEpmPackagesPath;
    }

    @Override
    public ResponseEntity<Boolean> uploadClusterTarFile(
            @RequestParam("file") MultipartFile file) {
        try {
            Boolean saved = etmFilesService.saveMultipartFile(
                    CLUSTER_TAR_FILE_NAME, file, epmPackagescompletePath);
            return new ResponseEntity<Boolean>(saved, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on upload cluster tar file");
            return new ResponseEntity<Boolean>(false,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Boolean> uploadNodeTarFile(
            @RequestParam("file") MultipartFile file) {
        try {
            Boolean saved = etmFilesService.saveMultipartFile(
                    NODE_TAR_FILE_NAME, file, epmPackagescompletePath);
            return new ResponseEntity<Boolean>(saved, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on upload node tar file");
            return new ResponseEntity<Boolean>(false,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
