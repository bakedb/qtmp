import java.io.*;
import java.net.*;
import java.util.Scanner;

public class QTMPClient {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 7867;

    public static void errorMessage() {
        System.out.println("Uh oh, QTMP is sad :(\nHere is the stack trace:");
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(ADDRESS, PORT);
            System.out.println("Connected to the chat server!");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println(serverResponse);
                    }
                } catch (IOException e) {
                    errorMessage();
                    e.printStackTrace();
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            String userInput;
            while (true) {
                userInput = scanner.nextLine();
                out.println(userInput);
            }

        } catch (IOException e) {
            errorMessage();
            e.printStackTrace();
        }
    }
}