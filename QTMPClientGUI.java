import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class QTMPClientGUI extends JFrame {
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public static void errorMessage() {
        System.out.println("Uh oh, QTMP is sad :(\nHere is the stack trace:");
    }

    public QTMPClientGUI(Socket socket, String title) throws IOException {
        super(title);
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        buildUi();
        startReaderThread();
    }

    private void buildUi() {
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        ((DefaultCaret) chatArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel bottom = new JPanel(new BorderLayout(5, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        inputField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                disconnect();
                dispose();
                System.exit(0);
            }
        });

        setSize(600, 450);
        setLocationRelativeTo(null);
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (text.isEmpty()) {
            return;
        }
        out.println(text);
        inputField.setText("");
    }

    private void startReaderThread() {
        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    final String received = line;
                    SwingUtilities.invokeLater(() -> handleServerLine(received));
                }
                SwingUtilities.invokeLater(() -> {
                    appendLine("[Disconnected from server]");
                    inputField.setEnabled(false);
                    sendButton.setEnabled(false);
                });
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    errorMessage();
                    e.printStackTrace();
                }
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    private void handleServerLine(String line) {
        if (line.contains("\033[2J")) {
            chatArea.setText("");
            return;
        }
        appendLine(line.replaceAll("\u001B\\[[;\\d]*[A-Za-z]", ""));
    }

    private void appendLine(String line) {
        chatArea.append(line + "\n");
    }

    private void disconnect() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
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

        Socket socket;
        try {
            socket = new Socket(ADDRESS, PORT);
        } catch (IOException e) {
            errorMessage();
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Could not connect to " + ADDRESS + ":" + PORT,
                    "QTMP", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final Socket connected = socket;
        SwingUtilities.invokeLater(() -> {
            try {
                new QTMPClientGUI(connected, "QTMP - " + ADDRESS + ":" + PORT).setVisible(true);
            } catch (IOException e) {
                errorMessage();
                e.printStackTrace();
            }
        });
    }
}