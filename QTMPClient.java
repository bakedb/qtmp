import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;

public class QTMPClient {
    public static void errorMessage() {
        System.out.println("Uh oh, QTMP is sad :(\nHere is the stack trace:");
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("client.properties")) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            System.out.println("client.properties not found, falling back to default values.");
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

        final String ADDRESS = properties.getProperty("address", "localhost");

        try {
            Socket socket = new Socket(ADDRESS, PORT);
            System.out.println("Successfully connected to " + ADDRESS + ":" + PORT);

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
