package com.websocket.serverapp.speedtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.messaging.simp.stomp.StompSession;

import com.websocket.serverapp.Util;

public class SpeedTest {

	private static int JITTER_SAMPLE_SIZE = 5;
	private static String GET_IP_URL = "http://checkip.amazonaws.com";
	private static String GET_ISP_INFO_URL = "http://ip-api.com/json/";

	private StompSession session;
	public com.websocket.serverapp.speedtest.SpeedTestReport speedTestReport = null;
	private String websocketPath;
	private DownloadSpeedTest downloadSpeedTest;
	private UploadSpeedTest uploadSpeedTest;

	public SpeedTest(StompSession session, String id) {
		this.session = session;
		this.websocketPath = "/topic/response-" + Util.getMyIp() + "-" + id;
	};

	public void start() {
		int latency = getLatency();
		String publicIp = null;

		try {
			publicIp = getPublicIp();

			String isp = getIsp(publicIp);

			float jitter = getJitter();
			speedTestReport = new com.websocket.serverapp.speedtest.SpeedTestReport(isp, latency, publicIp, jitter);
			System.out.println("Response - " + speedTestReport);
			System.out.println("Destination - " + websocketPath);

			new DownloadSpeedTest(speedTestReport, session, websocketPath).startDownloadSpeedTest();
			new UploadSpeedTest(speedTestReport, session, websocketPath).startUploadSpeedTest();

			session.send(websocketPath, speedTestReport);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getLatency() {
		Socket s = new Socket();
		SocketAddress a = new InetSocketAddress("www.google.com", 80);
		int timeoutMillis = 2000;
		long start = System.currentTimeMillis();
		try {
			s.connect(a, timeoutMillis);
		} catch (SocketTimeoutException e) {
			// timeout
		} catch (IOException e) {
			// some other exception
		}
		long stop = System.currentTimeMillis();
		long latency = stop - start;
		try {
			s.close();
		} catch (IOException e) {
			// closing failed
		}
		return (int) latency;
	}

	public String getIsp(String publicIp) throws IOException {
		URL ipInfo = null;
		try {
			ipInfo = new URL(SpeedTest.GET_ISP_INFO_URL + publicIp);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(ipInfo.openStream()));

		String info = in.readLine();
		String ispName = null;
		JSONObject documentObj = new JSONObject(info);
		try {
			ispName = documentObj.getString("as").split(" ", 2)[1];
		} catch (Exception e) {
			ispName = documentObj.getString("as");
		}
		return ispName;
	}

	public String getPublicIp() throws IOException {
		URL whatismyip;

		whatismyip = new URL(SpeedTest.GET_IP_URL);

		BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

		String ip = in.readLine(); // you get the IP as a String
		return ip;
	}

	public float getJitter() {
		int sum = 0;
		int index = 0;
		int prev_latency = getLatency();

		while (index < SpeedTest.JITTER_SAMPLE_SIZE) {
			int new_latency = getLatency();
			sum = sum + Math.abs(new_latency - prev_latency);
			prev_latency = new_latency;
			index += 1;
		}

		return (float) (sum / (SpeedTest.JITTER_SAMPLE_SIZE * 1.0));
	}

	public void cancel() {
		if (downloadSpeedTest != null) {
			downloadSpeedTest.closeConnection();
		}
		if (uploadSpeedTest != null) {
			uploadSpeedTest.closeConnection();
		}
	}

}
