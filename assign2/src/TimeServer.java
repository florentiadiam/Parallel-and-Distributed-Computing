import java.io.*;
import java.net.*;
import java.util.*;

public class TimeServer {
    private static final int PORT = 12345;
    private static final Database database = new Database();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

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

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
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
                    handleLogin(reader, writer);
                } else if ("2".equals(choice)) {
                    // Handle registration
                    handleRegistration(reader, writer);
                } else {
                    writer.println("Invalid choice. Please try again.");
                }

                // Continue with other game-related logic...
                // For example: Joining game queue, forming teams, starting games, handling gameplay
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to handle login
        private void handleLogin(BufferedReader reader, PrintWriter writer) throws IOException {
            // Authentication logic
            writer.println("Please enter your username:");
            String username = reader.readLine();
            writer.println("Please enter your password:");
            String password = reader.readLine();

            // Validate username and password using the Database class
            boolean authenticated = database.validateUser(username, password);
            if (authenticated) {
                writer.println("Login successful!");
            } else {
                writer.println("Login failed. Invalid username or password.");
            }
        }

        // Method to handle registration
        private void handleRegistration(BufferedReader reader, PrintWriter writer) throws IOException {
            // Registration logic
            writer.println("Please enter a new username:");
            String username = reader.readLine();
            writer.println("Please enter a password:");
            String password = reader.readLine();

            // Register the new user using the Database class
            boolean registered = database.registerUser(username, password);
            if (registered) {
                writer.println("Registration successful!");
            } else {
                writer.println("Registration failed. Username already exists.");
            }
        }
    }
}
