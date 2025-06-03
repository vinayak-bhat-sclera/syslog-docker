package com.websocket.serverapp.speedtest;

public class SpeedTestReport {
	private String isp;
	private int latency;
	private String publicIp;
	private float jitter;
	private float downloadSpeed;
	private float uploadSpeed;

	public float getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(float downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public float getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(float uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

	public SpeedTestReport() {
	}

	public SpeedTestReport(String isp, int latency, String publicIp, float jitter) {
		this.isp = isp;
		this.latency = latency;
		this.publicIp = publicIp;
		this.jitter = jitter;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}

	public float getJitter() {
		return jitter;
	}

	public void setJitter(int jitter) {
		this.jitter = jitter;
	}

	@Override
	public String toString() {
		return "SpeedTestReport [isp=" + isp + ", latency=" + latency + ", publicIp=" + publicIp + ", jitter=" + jitter
				+ "]";
	}

}
