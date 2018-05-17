package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ContainerPort;
import io.elastest.etm.model.VersionInfo;

public class CoreServiceInfo {
    String name;
    VersionInfo versionInfo;
    String imageName;
    List<String> containerNames;
    List<ContainerPort> ports;
    String status;
    List<String> networks;

    public CoreServiceInfo() {
        this.containerNames = new ArrayList<>();
        this.ports = new ArrayList<>();
        this.networks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public List<String> getContainerNames() {
        return containerNames;
    }

    public void setContainerNames(List<String> containerNames) {
        this.containerNames = containerNames;
    }

    public List<ContainerPort> getPorts() {
        return ports;
    }

    public void setPorts(List<ContainerPort> ports) {
        this.ports = ports;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getNetworks() {
        return networks;
    }

    public void setNetworks(List<String> networks) {
        this.networks = networks;
    }

    /* **** Others **** */

    public void setDataByContainer(Container container) {
        this.setContainerNames(Arrays.asList(container.getNames()));
        this.setPorts(Arrays.asList(container.getPorts()));
        this.setStatus(container.getStatus());

        for (Map.Entry<String, ContainerNetwork> entry : container
                .getNetworkSettings().getNetworks().entrySet()) {
            this.getNetworks().add(entry.getKey());
        }
    }

}
