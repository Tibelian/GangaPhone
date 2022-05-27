package com.tibelian.gangaphone.socket;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketClient {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 10000;

    private static SocketClient socketClient;

    private String host;
    private int port;
    private Socket client;
    private InputStream input;
    private OutputStream output;

    public static SocketClient get() {
        if (socketClient == null)
            socketClient = new SocketClient(HOST, PORT);
        return socketClient;
    }

    private SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void open() {
        try {
            client = new Socket(host, port);
            Log.d("SocketClient", "*** CLIENT CONNECTED SUCCESSFULLY ***");
        }
        catch(Exception e) {
            Log.e("SocketClient", "open() error --> " + e);
        }
    }

    public String receive() {
        try {
            // client waits for message
            input = client.getInputStream();
            DataInputStream msgFromServer = new DataInputStream(input);
            Log.d("SocketClient", "** CLIENT RECEIVED A MESSAGE FROM THE SERVER **");
            return msgFromServer.readUTF();
        }
        catch (IOException e) {
            Log.e("SocketClient", "receive() error --> " + e);
        }
        return null;
    }

    public void send(String msg) {
        try {
            output = client.getOutputStream();
            DataOutputStream msgToServer = new DataOutputStream(output);
            msgToServer.writeUTF(msg);
        }
        catch (IOException e) {
            Log.e("SocketClient", "send() error --> " + e);
        }
    }

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

}