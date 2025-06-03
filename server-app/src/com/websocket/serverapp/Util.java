package com.websocket.serverapp;

import java.net.*;

public class Util {
	public static final int sshPort = 22;
	public static final int telnetPort = 23;

	public static final int MAX_TEST_TIME_SECONDS = 30;
	public static final int MAX_SPEED_PER_SECONDS = 104857600; // 100 mb
	public static final int REPORTING_TIME_MS = 100;
	private static String myIp;

	public static String getMyIp() {
		if (myIp != null) {
			return myIp;
		}

		String myip = null;
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			myip = socket.getLocalAddress().getHostAddress();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println(myip);
		return myip;
	}

}
