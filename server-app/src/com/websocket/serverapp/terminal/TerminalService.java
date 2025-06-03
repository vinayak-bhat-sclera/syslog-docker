package com.websocket.serverapp.terminal;

import java.io.PipedInputStream;

public interface TerminalService {

	static final int MAX_ALLOWED_SESSION_TIME_SECONDS = 3600; 
	static final int MAX_ALLOWED_IDLE_TIME_SECONDS = 600;

	public void startService() throws Exception;

	public void setId(String id);

	public void disconnect();

	public CustomPipedOutputStream getPOut();

	public PipedInputStream getPIn();

	public PipedInputStream getFeedIn();

	public CustomPipedOutputStream getFeedOut();

}
