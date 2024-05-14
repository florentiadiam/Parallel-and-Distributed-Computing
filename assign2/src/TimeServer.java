import java.io.*;
import java.net.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeServer {

    private static final int REQUIRED_PLAYERS = 1; // Adjust as needed
    private static final Queue<Socket> gameQueue = new LinkedList<>();

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                // Create a new thread to handle the client connection
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final PrintWriter writer;

        public ClientHandler(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                // Prompt the client with options for login or registration
                writer.println("Welcome to the game server!");
                writer.println("Choose an option:");
                writer.println("1. Login");
                writer.println("2. Register");
                writer.println("Enter your choice (1 or 2):");

                // Read the client's choice
                String choice = reader.readLine();
                if ("1".equals(choice)) {
                    // Handle login
                    handleLogin(reader);
                } else if ("2".equals(choice)) {
                    // Handle registration
                    handleRegistration(reader);
                } else {
                    writer.println("Invalid choice. Please try again.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to handle login
        private void handleLogin(BufferedReader reader) throws IOException {
            // Authentication logic (replace with your own authentication mechanism)
            writer.println("Please enter your username:");
            String username = reader.readLine();
            writer.println("Please enter your password:");
            String password = reader.readLine();

            // Simulate validation (replace with your own validation logic)
            if ("admin".equals(username) && "password".equals(password)) {
                writer.println("Login successful!");
                joinGameQueue(clientSocket);
            } else {
                writer.println("Login failed. Invalid username or password.");
            }
        }

        // Method to handle registration
        private void handleRegistration(BufferedReader reader) throws IOException {
            // Registration logic (replace with your own registration mechanism)
            writer.println("Please enter a new username:");
            String username = reader.readLine();
            writer.println("Please enter a password:");
            String password = reader.readLine();

            // Simulate registration (replace with your own registration logic)
            writer.println("Registration successful!");
            joinGameQueue(clientSocket);
          
        }

        // Method to join the game queue
        private void SimpleMode(Socket clientSocket) {
            this.queue_lock.lock();
            synchronized (gameQueue) {
                gameQueue.add(clientSocket);
                if (gameQueue.size() >= REQUIRED_PLAYERS) {
                    List<Client> gameClients = new ArrayList<>();
                    gameClients.add(this.queue.remove(0));
                    System.out.println("Client " + gameClients.get(i).getUsername() + " removed from waiting queue");
                }
            }
            this.queue_lock.unlock();
        }


        public Game(List<Client> players,List<Client> queue,ReentrantLock queue_lock) {
        this.players = players;
        //this.database = database;
        //this.database_lock = database_lock;
        this.queue = queue;
        this.queue_lock = queue_lock;
}
    }
}
