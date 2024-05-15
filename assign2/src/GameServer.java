import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static Queue<Socket> simpleQueue = new LinkedList<>();
    private static Queue<Socket> rankQueue = new LinkedList<>();
    private static String mode;
    private static int batchSize = 4; // Change this to the desired batch size
    private static List<Game> games = new ArrayList<>();
    private static Map<Socket, Integer> playerLevels = new HashMap<>();
    private static Map<String, String> players = new HashMap<>(); // Map to store usernames and passwords
    private static Map<String, Socket> sessionTokens = new HashMap<>(); // Map to store session tokens

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);
        loadPlayers();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                login(socket);
                handleClient(socket);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        new Thread(() -> {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                String choice = reader.readLine();

                synchronized (simpleQueue) {
                    synchronized (rankQueue) {
                        // Process client's mode selection
                        if (choice.equals("1")) {
                            mode = "simple";
                            System.out.println("Simple mode selected.");
                            simpleQueue.add(socket);
                        } else if (choice.equals("2")) {
                            mode = "rank";
                            System.out.println("Rank mode selected.");
                            rankQueue.add(socket);
                        } else {
                            System.out.println("Invalid mode selection.");
                            return;
                        }
                    }
                }

                // Inform the client about their status in the queue
                writer.println("You are in the queue. Please wait for other players.");

                // Generate and send session token
                String token = UUID.randomUUID().toString();
                sessionTokens.put(token, socket);
                writer.println("Your session token is: " + token);

                // Print the updated queue in the terminal
                printQueue();

                // Handle client connection based on the chosen mode
                if (mode.equals("simple")) {
                    handleSimpleMode();
                } else if (mode.equals("rank")) {
                    handleRankMode();
                }

                // Listen for client disconnection
                while (true) {
                    if (reader.readLine() == null) {
                        removeClient(socket);
                        break;
                    }
                }

            } catch (IOException ex) {
                System.out.println("Client disconnected: " + ex.getMessage());
                removeClient(socket);
            }
        }).start();
    }

    private static void removeClient(Socket socket) {
        synchronized (simpleQueue) {
            synchronized (rankQueue) {
                if (simpleQueue.remove(socket) || rankQueue.remove(socket)) {
                    playerLevels.remove(socket); // Remove player's level when disconnected
                    System.out.println("Client disconnected and removed from queue.");
                    printQueue();
                }
            }
        }
    }

    private static void login(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Ask if the client wants to reconnect using a token
            writer.println("Do you want to reconnect using a session token? (yes/no)");
            String reconnectChoice = reader.readLine();

            if (reconnectChoice.equalsIgnoreCase("yes")) {
                writer.println("Enter your session token:");
                String token = reader.readLine();

                if (sessionTokens.containsKey(token)) {
                    Socket existingSocket = sessionTokens.get(token);
                    writer.println("Reconnected successfully with token: " + token);
                    // Handle reconnection logic if needed
                    return;
                } else {
                    writer.println("Invalid token. Please login normally.");
                }
            }

            // Proceed with normal login
            writer.println("Enter your username:");
            String username = reader.readLine();
            writer.println("Enter your password:");
            String password = reader.readLine();

            if (players.containsKey(username)) {
                String storedPassword = players.get(username);
                if (storedPassword.equals(password)) {
                    writer.println("Login successful! Choose a mode:");
                    writer.println("1. Simple");
                    writer.println("2. Rank");

                    // Generate and send session token
                    String token = UUID.randomUUID().toString();
                    sessionTokens.put(token, socket);
                    writer.println("Your session token is: " + token);

                } else {
                    writer.println("Incorrect password. Please try again.");
                    socket.close(); // Close the connection
                }
            } else {
                writer.println("Username not found. Please register or try again.");
                socket.close(); // Close the connection
            }
        } catch (IOException ex) {
            System.out.println("Error during login: " + ex.getMessage());
        }
    }

    private static void loadPlayers() {
        try (BufferedReader br = new BufferedReader(new FileReader("players.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    players.put(parts[0], parts[1]); // Username as key, password as value
                }
            }
        } catch (IOException ex) {
            System.out.println("Error loading players: " + ex.getMessage());
        }
    }

    private static void handleSimpleMode() {
        if (simpleQueue.size() >= batchSize) {
            List<Socket> batch = new ArrayList<>();

            // Create a batch of clients for the game instance
            for (int i = 0; i < batchSize; i++) {
                batch.add(simpleQueue.poll());
            }

            // Inform each client that the game is starting
            for (Socket clientSocket : batch) {
                try {
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    writer.println("The game is starting. Get ready!");
                } catch (IOException ex) {
                    System.out.println("Error informing client: " + ex.getMessage());
                }
            }

            // Create a new game instance with the batch of clients
            Game game = new Game(batch);
            games.add(game);

            System.out.println("New game started with " + batchSize + " players.");

            // Start the game instance in a separate thread
            new Thread(game::start).start();
        }
    }

    private static void handleRankMode() {
        if (rankQueue.size() >= batchSize) {
            List<Socket> batch = new ArrayList<>();

            // Create a batch of clients for the game instance
            for (int i = 0; i < batchSize; i++) {
                Socket clientSocket = rankQueue.poll();
                batch.add(clientSocket);
            }

            // Matchmaking algorithm to form teams with similar levels
            List<Socket> team1 = new ArrayList<>();
            List<Socket> team2 = new ArrayList<>();
            int team1Level = 0;
            int team2Level = 0;

            for (Socket playerSocket : batch) {
                int playerLevel = playerLevels.getOrDefault(playerSocket, 1); // Default level is 1
                if (team1Level <= team2Level) {
                    team1.add(playerSocket);
                    team1Level += playerLevel;
                } else {
                    team2.add(playerSocket);
                    team2Level += playerLevel;
                }
            }

            // Inform each client about their team and the game starting
            for (Socket playerSocket : team1) {
                try {
                    PrintWriter writer = new PrintWriter(playerSocket.getOutputStream(), true);
                    writer.println("You are in Team 1. Get ready for the game!");
                } catch (IOException ex) {
                    System.out.println("Error informing client: " + ex.getMessage());
                }
            }

            for (Socket playerSocket : team2) {
                try {
                    PrintWriter writer = new PrintWriter(playerSocket.getOutputStream(), true);
                    writer.println("You are in Team 2. Get ready for the game!");
                } catch (IOException ex) {
                    System.out.println("Error informing client: " + ex.getMessage());
                }
            }

            // Create a new game instance with the formed teams
            Game game = new Game(team1, team2);
            games.add(game);

            System.out.println("New game started with teams of " + batchSize / 2 + " players.");

            // Start the game instance in a separate thread
            new Thread(game::start).start();
        }
    }

    private static void updatePlayerLevels(List<Socket> team1, List<Socket> team2, int winner) {
        // Update player levels based on game outcome
        int delta = winner == 1 ? 1 : -1; // Adjust level based on game winner
        for (Socket playerSocket : team1) {
            playerLevels.put(playerSocket, playerLevels.getOrDefault(playerSocket, 1) + delta);
        }
        for (Socket playerSocket : team2) {
            playerLevels.put(playerSocket, playerLevels.getOrDefault(playerSocket, 1) - delta);
        }
    }

    private static void printQueue() {
        System.out.println("Current clients in the simple queue: " + simpleQueue.size());
        System.out.println("Current clients in the rank queue: " + rankQueue.size());
    }

    private static class Game {
        private List<Socket> team1;
        private List<Socket> team2;

        // Constructor for simple mode
        public Game(List<Socket> batch) {
            this.team1 = batch;
            this.team2 = new ArrayList<>();
        }

        // Constructor for rank mode
        public Game(List<Socket> team1, List<Socket> team2) {
            this.team1 = team1;
            this.team2 = team2;
        }

        public void start() {
            // Simulate game logic
            Random random = new Random();
            int winner = random.nextInt(2) + 1; // Randomly choose winner (1 or 2)

            // Inform players about game outcome and update levels
            updatePlayerLevels(team1, team2, winner);

            // Inform each client that the game has ended
            for (Socket playerSocket : team1) {
                try {
                    PrintWriter writer = new PrintWriter(playerSocket.getOutputStream(), true);
                    writer.println("The game has ended. Winner: Team " + winner);
                } catch (IOException ex) {
                    System.out.println("Error informing client: " + ex.getMessage());
                }
            }
            for (Socket playerSocket : team2) {
                try {
                    PrintWriter writer = new PrintWriter(playerSocket.getOutputStream(), true);
                    writer.println("The game has ended. Winner: Team " + winner);
                } catch (IOException ex) {
                    System.out.println("Error informing client: " + ex.getMessage());
                }
            }
        }
    }
}
