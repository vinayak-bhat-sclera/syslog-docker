package com.websocket.serverapp;

public class User {
	private String docker_ip;
	private String private_ip;
	private String protocol;
	private String timestamp;
	private String username;
	private String password;
	private int private_port;

	public User() {
	}

	public String getProtocol() {
		return protocol;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDocker_ip() {
		return docker_ip;
	}

	public void setDocker_ip(String docker_ip) {
		this.docker_ip = docker_ip;
	}

	public int getPrivate_port() {
		return private_port;
	}

	public void setPrivate_port(int private_port) {
		this.private_port = private_port;
	}

	public String getPrivate_ip() {
		return private_ip;
	}

	public void setPrivate_ip(String private_ip) {
		this.private_ip = private_ip;
	}

	@Override
	public String toString() {
		return "User{" +
				"docker_ip='" + docker_ip + '\'' +
				", private_ip='" + private_ip + '\'' +
				", protocol='" + protocol + '\'' +
				", timestamp='" + timestamp + '\'' +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", private_port=" + private_port +
				'}';
	}
}