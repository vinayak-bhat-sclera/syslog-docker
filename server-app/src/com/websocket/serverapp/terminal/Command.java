package com.websocket.serverapp.terminal;

public class Command {
	private String command;
	private String ip;
	private String id;

	public Command() {

	}

	public Command(String command, String ip) {
		this.command = command;
		this.ip = ip;
	}

	public String getCommand() {
		return command;
	}

	public String getIp() {
		return ip;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Command [command=" + command + ", ip=" + ip + "]";
	}

}