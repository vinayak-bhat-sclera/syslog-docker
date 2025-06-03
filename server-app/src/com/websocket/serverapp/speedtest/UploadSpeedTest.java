package com.websocket.serverapp.speedtest;

import org.springframework.messaging.simp.stomp.StompSession;

import com.websocket.serverapp.Util;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.IRepeatListener;

public class UploadSpeedTest implements IRepeatListener {

	private static String uploadURL = "http://ipv4.ikoula.testdebit.info/";

	private SpeedTestSocket speedTestSocket = null;
	private com.websocket.serverapp.speedtest.SpeedTestReport report = null;
	private StompSession session = null;
	private String websocketPath = null;

	public UploadSpeedTest(com.websocket.serverapp.speedtest.SpeedTestReport report, StompSession session,
			String path) {
		speedTestSocket = new SpeedTestSocket(Util.REPORTING_TIME_MS);
		this.report = report;
		this.session = session;
		this.websocketPath = path;
	}

	public SpeedTestSocket getSpeedTestSocket() {
		return this.speedTestSocket;
	}

	public void startUploadSpeedTest() {
		speedTestSocket.startUploadRepeat(uploadURL, Util.MAX_TEST_TIME_SECONDS * 1000, Util.MAX_SPEED_PER_SECONDS,
				this);
	}

	public void closeConnection() {
		if (speedTestSocket != null) {
			speedTestSocket.clearListeners();
			speedTestSocket.closeSocket();
			speedTestSocket = null;
		}
	}

	@Override
	public void onCompletion(SpeedTestReport report) {
		// TODO Auto-generated method stub
		System.out.println("[UPLOAD COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
		this.report.setUploadSpeed(report.getTransferRateBit().floatValue());
		session.send(this.websocketPath, this.report);
		this.closeConnection();
	}

	@Override
	public void onReport(SpeedTestReport report) {
		// TODO Auto-generated method stub
		this.report.setUploadSpeed(report.getTransferRateBit().floatValue());
		session.send(this.websocketPath, this.report);
	}
}