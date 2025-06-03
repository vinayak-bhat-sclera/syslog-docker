package com.websocket.serverapp.terminal;

import java.io.IOException;
import java.io.PipedOutputStream;

/*
 * Extension of Class PipedOutputStream
 *
 * This class notifies all the subscribers of the stream that there is data to read so they do not have to wait
 *
 */

public class CustomPipedOutputStream extends PipedOutputStream{

    @Override
    public void write(final int b) throws IOException {
        super.write(b);
        flush();
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        super.write(b, off, len);
        flush();
    }
}
