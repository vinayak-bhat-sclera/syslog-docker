package com.websocket.serverapp.speedtest;

import org.springframework.messaging.simp.stomp.StompSession;

import com.websocket.serverapp.Util;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.IRepeatListener;

public class DownloadSpeedTest implements IRepeatListener {

	SpeedTestSocket speedTestSocket = null;
	com.websocket.serverapp.speedtest.SpeedTestReport report = null;
	StompSession session = null;
	String websocketPath = null;

	public DownloadSpeedTest(com.websocket.serverapp.speedtest.SpeedTestReport report, StompSession session,
			String path) {
		speedTestSocket = new SpeedTestSocket(Util.REPORTING_TIME_MS);
		this.report = report;
		this.session = session;
		this.websocketPath = path;
	}

	public SpeedTestSocket getSpeedTestSocket() {
		return this.speedTestSocket;
	}

	public void startDownloadSpeedTest() {
		speedTestSocket.startDownloadRepeat("http://ipv4.ikoula.testdebit.info/1G.iso",
				Util.MAX_TEST_TIME_SECONDS * 1000, Util.REPORTING_TIME_MS, this);

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
		System.out.println("[DOWNLOAD COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
		this.report.setDownloadSpeed(report.getTransferRateBit().floatValue());
		session.send(this.websocketPath, this.report);
		this.closeConnection();
	}

	@Override
	public void onReport(SpeedTestReport report) {
		this.report.setDownloadSpeed(report.getTransferRateBit().floatValue());
		session.send(this.websocketPath, this.report);
	}
}
