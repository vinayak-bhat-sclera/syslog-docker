package com.websocket.serverapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

public class ClientConnectionSTOMP {
	
	final static Logger logger = Logger.getLogger(ClientConnectionSTOMP.class.getName());
	
	public static void main(String[] args) {
		logger.setLevel(Level.ALL);
		logger.info("Starting websocket java application");
		
		if (args.length < 1) {
			logger.error("Please provide the host for the websocket");
			System.exit(1);
		} 
		String websocketURL = args[0];
//		String websocketURL = "ws://192.168.20.120:8888/ws";
		
		WebSocketClient simpleWebSocketClient = new StandardWebSocketClient();
		List<Transport> transports = new ArrayList<>(1);
		transports.add(new WebSocketTransport(simpleWebSocketClient));

		SockJsClient sockJsClient = new SockJsClient(transports);
		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		StompSessionHandler sessionHandler = new ConnectionWSHandler();
		try {
			stompClient.connect(websocketURL, sessionHandler).get();
			logger.info("Socket Connection established");
			new CountDownLatch(1).await();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Some error has occured while establishing socket connection");
			logger.error(e.getClass() + ": " +  e.getMessage() + ": " + e.getCause(), e);
		
		}

	}
}
