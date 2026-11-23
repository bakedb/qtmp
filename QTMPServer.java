import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class QTMPServer {
    private static final int PORT = 7867;
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void errorMessage() {
        System.out.println("Uh oh, QTMP is sad :(\nHere is the stack trace:");
    }

    public static void main(String[] args) {
        System.out.println("QTMP is developed and maintained by Lavender Persson <contact@bkd.lol>");
        System.out.println("Starting QTMP...");

        try {
            ServerSocket socket = new ServerSocket(PORT);
            System.out.println("QTMP has started successfully and is ready to accept connections.");

            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String message = scanner.nextLine();
                    broadcast("[Server]: " + message, null);
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
                System.out.println("User " + username + " connected.");
                out.println(username + " has entered the chat.");
                out.print("Type your message\n> ");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
		    if (inputLine.substring(0, 1).equals("/")) {
		    	if (inputLine.equals("/help")) {
		    		out.println("/help: displays this message\n"
						+ "/quit: closes the connection");
			} else if {
			    
			} else {
			    out.println("Invalid command.\nIf you didn't mean to input a command, you can start your message with '//' to insert one slash at the beginning of your message.")
			}
		    } else {
                    	System.out.println("[" + username + "]: " + inputLine);
		    	broadcast("[" + username + "]: " + inputLine, this);
                    }
                clients.remove(this);
		}
            System.out.println(username + " has left the chat.");
            } catch (IOException e) {
                errorMessage();
                e.printStackTrace();
            } finally {
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
