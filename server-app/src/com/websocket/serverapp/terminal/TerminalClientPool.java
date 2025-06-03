package com.websocket.serverapp.terminal;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.websocket.serverapp.ClientConnectionSTOMP;
import com.websocket.serverapp.User;
import com.websocket.serverapp.Util;

public class TerminalClientPool {
	final static Logger logger = Logger.getLogger(ClientConnectionSTOMP.class.getName());
	static final int POOL_SIZE = 10;

	private TerminalService[] clients = new TerminalService[POOL_SIZE];

	HashMap<String, Integer> indexMap = new HashMap<>();

	public TerminalService get(User user) {

		Integer index = indexMap.get(user.getTimestamp());
		// System.out.println("Got client at index - "+ index);
		if (index == null) {
			try {
				index = createAndReturnNewClientIndex(user);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		return clients[index];
	}

	public TerminalService get(String timestamp) {

		Integer index = indexMap.get(timestamp);
		// System.out.println("Got client at index - "+ index);

		return clients[index];
	}

	public void release(String timestamp) {
		Integer index = indexMap.get(timestamp);
		logger.info("Releasing client at index - " + index);
		//System.out.println("Releasing client at index - " + index);
		clients[index] = null;
	}

	private Integer createAndReturnNewClientIndex(User user) throws Exception {
		Integer index = getEmptySlot();

		indexMap.put(user.getTimestamp(), index);

		if (user.getProtocol().equals("SSH")) {
			clients[index] = new SSHClient(user.getPrivate_ip(), user.getUsername(), user.getPassword(), user.getPrivate_port());
		} else if (user.getProtocol().equals("Telnet")) {
			clients[index] = new TelClient(user.getPrivate_ip(), null, null, user.getPrivate_port());
		}

		clients[index].setId(user.getTimestamp());
		//System.out.println("New client created at index - " + index);
		logger.info("New client created at index - " + index + " service - " + user.getProtocol() + " id - " + user.getTimestamp());
		return index;
	}

	private Integer getEmptySlot() throws Exception {
		for (int i = 0; i < POOL_SIZE; i++) {
			if (clients[i] == null) {
				//System.out.println("Found empty slot at - " + i);
				return i;
			}
		}
		logger.warn("No slots available for terminal service");
		throw new Exception("No slots available");
	}

}
