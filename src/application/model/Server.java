package application.model;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Server {
    private static final int PORT = 1919;
    private ServerSocket serverSocket;
    private List<Room> rooms;
    private HashMap<String, Player> players;
    private HashMap<Player, SendThread> clientThreadHashMap;
    private Random random;
    private int roomIndex = 0;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
//        serverSocket.setSoTimeout(10000);
        rooms = new ArrayList<>();
        players = new HashMap<>();
        clientThreadHashMap = new HashMap<>();
        random = new Random();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(PORT);
        server.run();
    }

    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(new SendThread(socket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class SendThread implements Runnable {
        private Socket socket;
        public Player player;
        public Room room;
        private PrintWriter printWriter;
        private BufferedReader bufferedReader;
        private Thread receiveThread;
        private Board board;

        public SendThread(Socket socket) throws IOException {
            this.socket = socket;
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            receiveThread = new Thread(() -> {
                String msg;
                while ((msg = receive()) != null) {
                    parseMessage(msg);
                }
                System.out.println("Connection closed");
            });
            board = new Board(0);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                send("exit server");
                close();
            }));

        }

        private void parseMessage(String tempMessage) {
            String[] message = tempMessage.split(" ");
            switch (message[0]) {
                case "step":
                    board.takeStep(Integer.parseInt(message[1]), Integer.parseInt(message[2]), Integer.parseInt(message[3]));
                    informAnotherClient("step " + message[1] + " " + message[2] + " " + message[3]);
                    break;
                case "exit":
                    informAnotherClient("exit client");
                    break;
                default:
                    System.err.println("Invalid message!");
            }
        }

        private void informAnotherClient(String message) {
            for (Player inRoomPlayer : room.players) {
                if (inRoomPlayer != player) {
                    clientThreadHashMap.get(inRoomPlayer).send(message);
                }
            }
        }

        @Override
        public void run() {
            try {
//                loginOrRegister();

                receiveThread.start();
                player = new Player();
                clientThreadHashMap.put(player, this);
                match(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void loginOrRegister(String account) {
            if (players.containsKey(account)) {
                player = players.get(account);
                System.out.println("Login: " + account);
            } else {
                player = new Player(account, players.size());
                players.put(account, player);
                System.out.println("Register: " + account);
            }
        }

        private void match(Player player) {
            boolean isMatched = false;
            for (Room room : rooms) {
                if (!room.isFull()) {
                    room.addPlayer(player);
                    this.room = room;
                    isMatched = true;
                    if (!room.isFull()) {
                        send("match waiting " + room.id);
                    } else {
                        int first = random.nextInt(2) * 2 - 1;
                        for (Player inRoomPlayer : room.players) {
                            SendThread sendThread = clientThreadHashMap.get(inRoomPlayer);
                            sendThread.send("match success " + first);
                            first = -first;
                        }
                    }
                    break;
                }
            }
            if (!isMatched) {
                Room room = new Room(roomIndex++);
                rooms.add(room);
                room.addPlayer(player);
                this.room = room;
                send("match waiting " + room.id);
            }
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

        public void close() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


