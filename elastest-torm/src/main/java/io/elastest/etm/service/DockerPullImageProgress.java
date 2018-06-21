package io.elastest.etm.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spotify.docker.client.messages.ProgressMessage;

public class DockerPullImageProgress {
    Map<String, DockerImageLayerProgress> layers;
    int currentPercentage;
    String image;

    public DockerPullImageProgress() {
        this.layers = new HashMap<>();
    }

    public DockerPullImageProgress(Map<String, DockerImageLayerProgress> layers,
            int currentPercentage, String image) {
        super();
        this.layers = layers != null ? layers : new HashMap<>();
        this.currentPercentage = currentPercentage;
        this.image = image;
    }

    public Map<String, DockerImageLayerProgress> getLayers() {
        return layers;
    }

    public void setLayers(Map<String, DockerImageLayerProgress> layers) {
        this.layers = layers;
    }

    public int getCurrentPercentage() {
        return currentPercentage;
    }

    public void setCurrentPercentage(int currentPercentage) {
        this.currentPercentage = currentPercentage;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "DockerPullImageProgress [layers=" + layers
                + ", currentPercentage=" + currentPercentage + ", image="
                + image + "]";
    }

    public void processNewMessage(ProgressMessage message) {
        DockerPullLayerStatus status = DockerPullLayerStatus
                .fromValue(message.status());
        if (status == null) {
            return;
        }
        int totalPartDownloadPercentage = 80;
        int totalPartExtractPercentage = 20;

        // If new Layer
        String layerId = message.id();
        if (DockerPullLayerStatus.isFirstApparition(status)) {
            int totalImageDownloadPercentage = totalPartDownloadPercentage
                    / (this.getLayers().size() + 1);
            int totalImageExtractPercentage = totalPartExtractPercentage
                    / (this.getLayers().size() + 1);

            DockerImageLayerProgress layer = new DockerImageLayerProgress(
                    layerId, status, null, new Long(0), 0,
                    totalImageDownloadPercentage, 0, 0,
                    totalImageExtractPercentage, 0);

            // Put first
            this.getLayers().put(layer.getId(), layer);

            // Update all layers
            for (Entry<String, DockerImageLayerProgress> currentLayer : this
                    .getLayers().entrySet()) {
                currentLayer.getValue().setTotalImageDownloadPercentage(
                        totalImageDownloadPercentage);
                currentLayer.getValue().setTotalImageExtractPercentage(
                        totalImageExtractPercentage);
                if (currentLayer.getValue().getStatus()
                        .equals(DockerPullLayerStatus.ALREADY_EXIST)) {
                    // Set/Update complete percentage of layer
                    currentLayer.getValue()
                            .setCurrentLayerDownloadPercentage(100);
                    currentLayer.getValue().setCurrentImageDownloadPercentage(
                            totalImageDownloadPercentage);

                    currentLayer.getValue()
                            .setCurrentLayerExtractPercentage(100);
                    currentLayer.getValue().setCurrentImageExtractPercentage(
                            totalImageExtractPercentage);
                }

            }

        } else {

            // Status
            this.getLayers().get(layerId).setStatus(status);

            if (status.equals(DockerPullLayerStatus.DOWNLOADING)) {
                // Total size
                Long totalSize = message.progressDetail().total();
                this.getLayers().get(layerId).setTotalSize(totalSize);
                // Current size
                Long currentSize = message.progressDetail().current();
                this.getLayers().get(layerId).setCurrentSize(currentSize);

                // current Layer downloaded percentage
                int currentLayerDownloadPercentage = (int) ((currentSize * 100)
                        / totalSize);
                this.getLayers().get(layerId).setCurrentLayerDownloadPercentage(
                        currentLayerDownloadPercentage);

                // Layer current percentage of image downloaded
                int currentImageDownloadPercentage = (int) (currentLayerDownloadPercentage
                        * this.getLayers().get(layerId)
                                .getTotalImageDownloadPercentage())
                        / 100;
                this.getLayers().get(layerId).setCurrentImageDownloadPercentage(
                        currentImageDownloadPercentage);

            } else {
                if (status.equals(DockerPullLayerStatus.EXTRACTING)) {
                    // If Extracting, then download of layer complete
                    this.getLayers().get(layerId)
                            .setCurrentImageDownloadPercentage(
                                    this.getLayers().get(layerId)
                                            .getTotalImageDownloadPercentage());
                    this.getLayers().get(layerId)
                            .setCurrentLayerDownloadPercentage(100);

                    // Total size
                    Long totalSize = message.progressDetail().total();
                    this.getLayers().get(layerId).setTotalSize(totalSize);

                    // Current size
                    Long currentSize = message.progressDetail().current();
                    this.getLayers().get(layerId).setCurrentSize(currentSize);

                    // total Layer extracted percentage
                    int totalLayerExtractPercentage = (int) ((currentSize * 100)
                            / totalSize);
                    this.getLayers().get(layerId)
                            .setCurrentLayerExtractPercentage(
                                    totalLayerExtractPercentage);

                    // Layer current percentage of image extracted
                    int currentImageExtractPercentage = (int) (totalLayerExtractPercentage
                            * this.getLayers().get(layerId)
                                    .getTotalImageExtractPercentage())
                            / 100;
                    this.getLayers().get(layerId)
                            .setCurrentImageExtractPercentage(
                                    currentImageExtractPercentage);
                }
            }
        }
        int totalDownloaded = 0;
        for (Entry<String, DockerImageLayerProgress> currentLayer : this
                .getLayers().entrySet()) {
            totalDownloaded += currentLayer.getValue()
                    .getCurrentImageDownloadPercentage();
            totalDownloaded += currentLayer.getValue()
                    .getCurrentImageExtractPercentage();
        }
        this.setCurrentPercentage(totalDownloaded);

    }

    public enum DockerPullLayerStatus {
        ALREADY_EXIST("Already exists"), PULLING_FS_LAYER(
                "Pulling fs layer"), DOWNLOADING("Downloading"), EXTRACTING(
                        "Extracting"), VERIFYING_CHECKSUM(
                                "Verifying Checksum"), PULL_COMPLETE(
                                        "Pull complete"), WAITING("Waiting");

        private String value;

        DockerPullLayerStatus(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static DockerPullLayerStatus fromValue(String text) {
            for (DockerPullLayerStatus b : DockerPullLayerStatus.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        public static boolean isFirstApparition(DockerPullLayerStatus status) {
            return status.equals(DockerPullLayerStatus.ALREADY_EXIST)
                    || status.equals(DockerPullLayerStatus.PULLING_FS_LAYER);

        }
    }

    /* *** LayerProgress Class *** */
    public class DockerImageLayerProgress {
        String id;
        DockerPullLayerStatus status;
        Long totalSize;
        Long currentSize;
        int currentLayerDownloadPercentage;
        int totalImageDownloadPercentage;
        int currentImageDownloadPercentage;

        int currentLayerExtractPercentage;
        int totalImageExtractPercentage;
        int currentImageExtractPercentage;

        public DockerImageLayerProgress() {
        }

        public DockerImageLayerProgress(String id, DockerPullLayerStatus status,
                Long totalSize, Long currentSize,
                int currentLayerDownloadPercentage,
                int totalImageDownloadPercentage,
                int currentImageDownloadPercentage,
                int currentLayerExtractPercentage,
                int totalImageExtractPercentage,
                int currentImageExtractPercentage) {
            super();
            this.id = id;
            this.status = status;
            this.totalSize = totalSize;
            this.currentSize = currentSize;
            this.currentLayerDownloadPercentage = currentLayerDownloadPercentage;
            this.totalImageDownloadPercentage = totalImageDownloadPercentage;
            this.currentImageDownloadPercentage = currentImageDownloadPercentage;
            this.currentLayerExtractPercentage = currentLayerExtractPercentage;
            this.totalImageExtractPercentage = totalImageExtractPercentage;
            this.currentImageExtractPercentage = currentImageExtractPercentage;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public DockerPullLayerStatus getStatus() {
            return status;
        }

        public void setStatus(DockerPullLayerStatus status) {
            this.status = status;
        }

        public Long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(Long totalSize) {
            this.totalSize = totalSize;
        }

        public Long getCurrentSize() {
            return currentSize;
        }

        public void setCurrentSize(Long currentSize) {
            this.currentSize = currentSize;
        }

        public int getCurrentLayerDownloadPercentage() {
            return currentLayerDownloadPercentage;
        }

        public void setCurrentLayerDownloadPercentage(
                int currentLayerDownloadPercentage) {
            this.currentLayerDownloadPercentage = currentLayerDownloadPercentage;
        }

        public int getTotalImageDownloadPercentage() {
            return totalImageDownloadPercentage;
        }

        public void setTotalImageDownloadPercentage(
                int totalImageDownloadPercentage) {
            this.totalImageDownloadPercentage = totalImageDownloadPercentage;
        }

        public int getCurrentImageDownloadPercentage() {
            return currentImageDownloadPercentage;
        }

        public void setCurrentImageDownloadPercentage(
                int currentImageDownloadPercentage) {
            this.currentImageDownloadPercentage = currentImageDownloadPercentage;
        }

        public int getCurrentLayerExtractPercentage() {
            return currentLayerExtractPercentage;
        }

        public void setCurrentLayerExtractPercentage(
                int currentLayerExtractPercentage) {
            this.currentLayerExtractPercentage = currentLayerExtractPercentage;
        }

        public int getTotalImageExtractPercentage() {
            return totalImageExtractPercentage;
        }

        public void setTotalImageExtractPercentage(
                int totalImageExtractPercentage) {
            this.totalImageExtractPercentage = totalImageExtractPercentage;
        }

        public int getCurrentImageExtractPercentage() {
            return currentImageExtractPercentage;
        }

        public void setCurrentImageExtractPercentage(
                int currentImageExtractPercentage) {
            this.currentImageExtractPercentage = currentImageExtractPercentage;
        }

        @Override
        public String toString() {
            return "DockerImageLayerProgress [id=" + id + ", status=" + status
                    + ", totalSize=" + totalSize + ", currentSize="
                    + currentSize + ", currentLayerDownloadPercentage="
                    + currentLayerDownloadPercentage
                    + ", totalImageDownloadPercentage="
                    + totalImageDownloadPercentage
                    + ", currentImageDownloadPercentage="
                    + currentImageDownloadPercentage
                    + ", currentLayerExtractPercentage="
                    + currentLayerExtractPercentage
                    + ", totalImageExtractPercentage="
                    + totalImageExtractPercentage
                    + ", currentImageExtractPercentage="
                    + currentImageExtractPercentage + "]";
        }

    }

}
