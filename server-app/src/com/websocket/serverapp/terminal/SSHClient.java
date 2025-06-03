package com.websocket.serverapp.terminal;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.websocket.serverapp.Util;


public class SSHClient implements TerminalService {

	private String host = null;
	private String username = null;
	private String password = null;
	private Integer port = null;
	private JSchSession jschSession = null;
	private ChannelShell channel = null;
	private String id = null;
	private Timer sessionTimer = null;
	private Timer idleTimer = null;
	private LocalDateTime lastActivityTime = null;

	public CustomPipedOutputStream pout = new CustomPipedOutputStream();
	public PipedInputStream pin = new PipedInputStream();
	public PipedInputStream feedStreamIn = new PipedInputStream();
	public CustomPipedOutputStream feedStreamOut = new CustomPipedOutputStream();

	public SSHClient(String host, String username, String password, Integer port) {
		this.host = host;
		this.username = username;
		this.password = password;

		// if port is not provided, default port number 22 will be used
		if (port != null) {
			this.port = port;
		} else {
			this.port = Util.sshPort;
		}
		sessionTimer = new Timer();
		idleTimer = new Timer();
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void disconnect() {
		if (channel != null && channel.isConnected()) {
			channel.disconnect();
		}
		if (idleTimer != null) {
			idleTimer.cancel();
		}
		if (sessionTimer != null) {
			sessionTimer.cancel();
		}
	}

	@Override
	public void startService() throws Exception {
		// TODO Auto-generated method stub



		jschSession = JSchSession.getSession(username, password, host, port, null);
		channel = (ChannelShell) jschSession.getSession().openChannel("shell");

		feedStreamIn.connect(feedStreamOut);
		channel.setInputStream(feedStreamIn);
		// channel.setOutputStream(System.out);

		pout.connect(pin);
		channel.setOutputStream(pout);

		// out = new OutputStreamWriter(channel.getOutputStream());
		// in = new InputStreamReader(channel.getInputStream());
		channel.setPtyType("xterm");
		channel.connect();

		sessionTimer.schedule(new SessionTimeoutTimer(), SSHClient.MAX_ALLOWED_SESSION_TIME_SECONDS * 1000);
		idleTimer.schedule(new IdleTimeoutTimer(), 5 * 1000, 5 * 1000);

	}

	@Override
	public CustomPipedOutputStream getPOut() {
		// TODO Auto-generated method stub
		return pout;
	}

	@Override
	public PipedInputStream getPIn() {
		// TODO Auto-generated method stub
		return pin;
	}

	@Override
	public PipedInputStream getFeedIn() {
		// TODO Auto-generated method stub
		return this.feedStreamIn;
	}

	@Override
	public CustomPipedOutputStream getFeedOut() {
		// TODO Auto-generated method stub
		return this.feedStreamOut;
	}

	public void setLastActivityTyime(LocalDateTime time) {
		this.lastActivityTime = time;
	}

	class SessionTimeoutTimer extends TimerTask {
		public void run() {
			System.out.println("Max time allowed exceeded. Closing session");
			disconnect();
			sessionTimer.cancel(); // Terminate the timer thread
		}
	}

	class IdleTimeoutTimer extends TimerTask {
		public void run() {
			// System.out.println("Last activity time - " + lastActivityTime);
			if (lastActivityTime == null) {
				return;
			}
			if (Duration.between(lastActivityTime, LocalDateTime.now())
					.toMillis() >= SSHClient.MAX_ALLOWED_IDLE_TIME_SECONDS * 1000) {
				disconnect();
				idleTimer.cancel();
			}
		}
	}

}
