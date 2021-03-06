package com.tibelian.gangaphone.messenger.socket;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Creates the connection with the TCP Server
 */
public class SocketClient {

    // connection ip and port
    public static final String HOST = "51.91.58.72";
    public static final int PORT = 6000;

    // the unique socket
    private static SocketClient socketClient;

    // current connection data
    private String host;
    private int port;
    private Socket client;
    private InputStream input;
    private OutputStream output;
    // control variable
    private boolean quit = false;

    // singleton getter
    public static SocketClient get() {
        if (socketClient == null)
            socketClient = new SocketClient(HOST, PORT);
        return socketClient;
    }

    // kill the connection
    public void setAsNull() {
        socketClient = null;
    }

    // constructor as private
    private SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // init conn
    public void open() {
        try {
            client = new Socket(host, port);
            Log.d("SocketClient", "*** CLIENT CONNECTED SUCCESSFULLY ***");
        }
        catch(Exception e) {
            Log.e("SocketClient", "open() error --> " + e);
        }
    }

    // wait for input data
    public String receive() {
        try {
            // client waits for message
            input = client.getInputStream();
            DataInputStream msgFromServer = new DataInputStream(input);
            Log.d("SocketClient", "** CLIENT RECEIVED A MESSAGE FROM THE SERVER **");
            return msgFromServer.readUTF();
        }
        catch (SocketException | EOFException eo) {
            Log.e("SocketClient", "server is offline --> " + eo);
            quit = true;
        }
        catch (IOException e) {
            Log.e("SocketClient", "receive() error --> " + e);
        }
        return null;
    }

    // output data, send a message to the server
    public void send(String msg) {
        try {
            output = client.getOutputStream();
            DataOutputStream msgToServer = new DataOutputStream(output);
            msgToServer.writeUTF(msg);
        }
        catch (NullPointerException ne) {
            Log.e("SocketClient", "send() NullPointerException --> server could be offline");
        }
        catch (IOException e) {
            Log.e("SocketClient", "send() error --> " + e);
        }
    }

    // close all connections
    public void close() {
        try {
            input.close();
            output.close();
            client.close();
        }
        catch (IOException e) {
            Log.e("SocketClient", "close() error --> " + e);
        }
    }

    // check if connection is valid
    public boolean isConnected() {
        return (client != null && quit == false);
    }

}
