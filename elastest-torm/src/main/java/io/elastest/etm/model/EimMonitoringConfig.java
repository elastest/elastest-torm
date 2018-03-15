package io.elastest.etm.model;

import java.util.List;

public class EimMonitoringConfig {
	String exec;
	String component;
	EimBeatConfig packetbeat;
	EimBeatConfig filebeat;
	EimBeatConfig topbeat;

	public EimMonitoringConfig() {
		this.packetbeat = new EimBeatConfig();
		this.filebeat = new EimBeatConfig();
		this.topbeat = new EimBeatConfig();
	}

	public String getExec() {
		return exec;
	}

	public void setExec(String exec) {
		this.exec = exec;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public EimBeatConfig getPacketbeat() {
		return packetbeat;
	}

	public void setPacketbeat(EimBeatConfig packetbeat) {
		this.packetbeat = packetbeat;
	}

	public EimBeatConfig getFilebeat() {
		return filebeat;
	}

	public void setFilebeat(EimBeatConfig filebeat) {
		this.filebeat = filebeat;
	}

	public EimBeatConfig getTopbeat() {
		return topbeat;
	}

	public void setTopbeat(EimBeatConfig topbeat) {
		this.topbeat = topbeat;
	}

	public class EimBeatConfig {
		String stream;
		List<String> paths;

		public EimBeatConfig() {
		}

		public String getStream() {
			return stream;
		}

		public void setStream(String stream) {
			this.stream = stream;
		}

		public List<String> getPaths() {
			return paths;
		}

		public void setPaths(List<String> paths) {
			this.paths = paths;
		}
	}
}
