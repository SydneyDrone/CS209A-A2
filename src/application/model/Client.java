package application.model;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    public Client() throws IOException {
        this("localhost", 1919);
    }

    public void send(String msg) {
        printWriter.println(msg);
    }

    public String receive() {
        String msg = null;
        try {
            msg = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
}

