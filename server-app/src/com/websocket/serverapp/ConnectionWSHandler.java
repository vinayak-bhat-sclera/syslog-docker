package com.websocket.serverapp;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;


import com.websocket.serverapp.speedtest.SpeedTest;
import com.websocket.serverapp.terminal.Command;
import com.websocket.serverapp.terminal.SSHClient;
import com.websocket.serverapp.terminal.TelClient;
import com.websocket.serverapp.terminal.TerminalClientPool;

public class ConnectionWSHandler extends StompSessionHandlerAdapter {

	final static Logger logger = Logger.getLogger(ConnectionWSHandler.class.getName());
	static final int PIPE_CONNECTION_TIME_MS = 1000;
	static final int IDLE_ALLOWED_TIME_MS = 10000;
	static final int MAX_ALLOWED_SESSION_TIME_MS = 10000;

	String myIp = null;
	SSHClient sshClient = null;
	String protocol = null;
	TelClient telnetClient = null;
	TerminalClientPool terminalClientPool = new TerminalClientPool();

	@Override
    public void handleTransportError(StompSession stompSession, Throwable throwable) {
        if (throwable instanceof ConnectionLostException) {
            logger.error("Connection lost. Closing application");
            System.exit(0);
        }
    }
	
	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		System.out.println("Connection established + " + session.getSessionId());

		session.subscribe("/topic/user", new StompFrameHandler() {

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				System.out.println(payload);
				sendResponse(session, ((User) payload));
			}

			@Override
			public Type getPayloadType(StompHeaders headers) {
				// TODO Auto-generated method stub
				return User.class;
			}

		});

		session.subscribe("/user/queue/item", new StompFrameHandler() {

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				sendResponse(session, ((User) payload));
			}

			@Override
			public Type getPayloadType(StompHeaders headers) {
				// TODO Auto-generated method stub
				return User.class;
			}

		});

		session.subscribe("/topic/messages-" + Util.getMyIp(), new StompFrameHandler() {

			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Command.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				// TODO Auto-generated method stub
				String receivedCommand = ((Command) payload).getCommand();
				String clientId = ((Command) payload).getId();
				// System.out.println("Client id - " + clientId);
				try {
					terminalClientPool.get(clientId).getFeedOut().write(receivedCommand.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				// System.out.println("Received command -> " + receivedCommand);
			}

		});
	}

	private void sendResponse(StompSession session, User user) {
		if (Util.getMyIp().equals(user.getDocker_ip())) {
			session.send("/app/handshake", user);
			System.out.println("User - " + user);

			if (user.getProtocol().equals("SSH")) {
				System.out.println("Starting ssh");
				startSSH(session, user);
			} else if (user.getProtocol().equals("Telnet")) {
				System.out.println("Starting telnet");
				startTelnet(session, user);
			} else if (user.getProtocol().equals("SpeedTest")) {
				System.out.println("Starting speed test");
				startSpeedTest(session, user);
			}

		}
	}

	public void startSSH(StompSession session, User user) {
		new Thread(() -> {
			String timestamp = user.getTimestamp();
			System.out.println(Util.getMyIp());
			String websocketPath = "/topic/response-" + user.getDocker_ip() + "-" + timestamp;
			try {
				terminalClientPool.get(user).startService();
				System.out.println("Terminal started");
				System.out.println(websocketPath);
				SSHClient client = (SSHClient) terminalClientPool.get(timestamp);
				while (true) {
					int readInt = client.getPIn().read();
					client.setLastActivityTyime(LocalDateTime.now());
					session.send(websocketPath, (readInt));
					if (readInt == -1) {
						terminalClientPool.get(timestamp).disconnect();
						terminalClientPool.release(timestamp);
						return;
					}

				}
			} catch (Exception e) {
				System.out.println(e.toString());
				if (e.toString().contains("Auth fail")) {
					session.send(websocketPath, -2);
				} else {
					session.send(websocketPath, -3);
				}
				if (terminalClientPool.get(timestamp) != null) {
					terminalClientPool.get(timestamp).disconnect();
					terminalClientPool.release(timestamp);
				}
				System.out.println("Error getting command output");
			}
		}).start();
	}

	public void startTelnet(StompSession session, User user) {
		new Thread(() -> {
			new Thread(() -> {
				try {
					terminalClientPool.get(user).startService();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
			try {
				Thread.sleep(PIPE_CONNECTION_TIME_MS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String timestamp = user.getTimestamp();
			String websocketPath = "/topic/response-" + Util.getMyIp() + "-" + timestamp;
			// System.out.println("Terminal started");
			try {

				while (true) {
					int readInt = terminalClientPool.get(timestamp).getPIn().read();

					session.send(websocketPath, (readInt));
					// System.out.println(((char) readInt));
					// System.out.println(readInt);
					if (readInt == -1) {
						System.out.println("Releasing telnet client");
						terminalClientPool.get(timestamp).disconnect();
						terminalClientPool.release(timestamp);
						return;
					}

				}
			} catch (IOException e) {
				System.out.println("Close telnet");
				if (session != null) {
					session.send(websocketPath, -1);
				}
				if (terminalClientPool.get(timestamp) != null) {
					terminalClientPool.get(timestamp).disconnect();
					terminalClientPool.release(timestamp);
					return;
				}
			} catch (Exception e) {
				// System.out.println("Error getting command output");
				e.printStackTrace();
			}
		}).start();
	}

	public void startSpeedTest(StompSession session, User user) {
		// System.out.println("Starting speed test...");
		new SpeedTest(session, user.getTimestamp()).start();
	}

}
