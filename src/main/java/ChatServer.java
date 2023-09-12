import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private static final String SETTINGS_FILE = "settings.txt"; // Файл настроек
    private static final String LOG_FILE = "file.log"; // Файл логирования
    private int port;
    private PrintWriter logWriter;
    private List<Socket> clients = new ArrayList<>();

    public ChatServer() {
        loadSetting();
        startServer();
    }

    private void loadSetting() {
        try (Scanner scanner = new Scanner(new File(SETTINGS_FILE))) {
            port = Integer.parseInt(scanner.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servet started on port " + port);

            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);

            while (true) {
                Socket clientSocked = serverSocket.accept();
                clients.add(clientSocked);

                Thread clientThread = new Thread(new ClientHandler(clientSocked));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        logWriter.println(message);

        for (Socket client : clients) {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private String clientName;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                clientName = in.readLine();
                System.out.println("New client connected: " + clientName);

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    String message = "[" + getCurrentTime() + "] " + clientName + ": " + clientMessage;
                    broadcastMessage(message);
                }

                System.out.println("Client disconnected: " + clientName);
                clients.remove(clientSocket);
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }
}
