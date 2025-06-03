package com.websocket.serverapp.test;

import com.websocket.serverapp.terminal.SSHClient;

public class TerminalTest {
	public static SSHClient sshClient = null;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		sshClient = new SSHClient("localhost", "osboxes", "osboxes.org", 22);
		sshClient.startService();
		System.out.println("Terminal started");
	}

}
