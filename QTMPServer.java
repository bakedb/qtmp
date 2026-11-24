import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class QTMPServer {
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void errorMessage() {
        System.out.println("Uh oh, QTMP is sad :(\nHere is the stack trace:");
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("server.properties")) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            System.out.println("server.properties not found, falling back to default values.");
        } catch (IOException e) {
            errorMessage();
            e.printStackTrace();
            return;
        }

        final int PORT;
        try {
            PORT = Integer.parseInt(properties.getProperty("port", "7867"));
        } catch (NumberFormatException e) {
            errorMessage();
            e.printStackTrace();
            return;
        }
        final String ADMIN_NAME = properties.getProperty("admin-name", "Server");

        System.out.println("QTMP is developed and maintained by Lavender Persson <contact@bkd.lol>");
        System.out.println("Starting QTMP...");

        try {
            ServerSocket socket = new ServerSocket(PORT);
            System.out.println("QTMP has started successfully and is ready to accept connections.");

            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    broadcast("[" + ADMIN_NAME + "]: " + message, null);
                }
            }).start();

            while (true) {
                Socket clientSocket = socket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            errorMessage();
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                errorMessage();
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out.println("Enter your username:");
                username = in.readLine();
                if (username == null) {
                    return;
                }
                System.out.println("User " + username + " connected.");
                broadcast(username + " has entered the chat.", this);
                out.println("Welcome to the chat, " + username + "!");

                String inputLine;
                boolean quit = false;
                while (!quit && (inputLine = in.readLine()) != null) {
                    if (inputLine.isEmpty()) {
                        continue;
                    }
                    if (inputLine.startsWith("//")) {
                        inputLine = inputLine.substring(1);
                    } else if (inputLine.startsWith("/")) {
                        if (inputLine.equals("/help")) {
                            out.println("/help: displays this message\n"
                                    + "/quit: closes the connection\n"
                                    + "/clear: clears the screen");
                        } else if (inputLine.equals("/quit")) {
                            out.println("Goodbye!");
                            quit = true;
                        } else if (inputLine.equals("/clear")) {
                            out.println("\033[2J\033[H");
                        } else {
                            out.println("Invalid command.\n"
                                    + "If you didn't mean to input a command, you can start your message with '//' to insert one slash at the beginning of your message.");
                        }
                        continue;
                    }

                    System.out.println("[" + username + "]: " + inputLine);
                    broadcast("[" + username + "]: " + inputLine, this);
                }
            } catch (IOException e) {
                errorMessage();
                e.printStackTrace();
            } finally {
                clients.remove(this);
                if (username != null) {
                    System.out.println(username + " has left the chat.");
                    broadcast(username + " has left the chat.", this);
                }
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    errorMessage();
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}