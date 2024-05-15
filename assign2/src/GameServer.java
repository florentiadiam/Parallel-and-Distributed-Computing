import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class GameServer {
    private static Queue<Socket> simpleQueue = new LinkedList<>();
    private static Queue<Socket> rankQueue = new LinkedList<>();
    private static String mode;
    private static int batchSize = 2; // Change this to the desired batch size
    private static List<Game> games = new ArrayList<>();
    private static Map<Socket, Integer> playerLevels = new HashMap<>();
    private static Map<String, String> players = new HashMap<>(); // Map to store usernames and passwords
    private static Map<String, Socket> sessionTokens = new HashMap<>(); // Map to store session tokens
    private static Lock lock = new ReentrantLock();

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
            writer.println("Do you want to reconnect(1) or login(2)");
            String reconnectChoice = reader.readLine();
    
            if (reconnectChoice.equalsIgnoreCase("1")) {
                writer.println("Enter your session token:");
                String token = reader.readLine();
    
                if (sessionTokens.containsKey(token)) {
                    Socket existingSocket = sessionTokens.get(token);
                    writer.println("Reconnected successfully with token: " + token);
                    synchronized (simpleQueue) {
                        synchronized (rankQueue) {
                            if (mode.equals("simple")) {
                                simpleQueue.add(existingSocket);
                            } else if (mode.equals("rank")) {
                                rankQueue.add(existingSocket);
                            }
                        }
                    }
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
        if (simpleQueue.size() >= 2) { // At least two players needed for a game
            List<Socket> players = new ArrayList<>();
    
            // Retrieve players from the queue
            for (int i = 0; i < 2; i++) {
                Socket playerSocket = simpleQueue.poll(); // Change rankQueue to simpleQueue
                players.add(playerSocket);
            }
    
            // Pair players with similar levels
            Socket player1 = players.get(0);
            Socket player2 = players.get(1);
    
            // Create a new game instance with the provided players
            Game game = new Game(player1, player2, lock); // Pass playerChoices, lock, and gameReady
  
            games.add(game);
    
            System.out.println("New game started with 2 players.");

            game.start(player1, player2, lock);
    
            // Start the game instance in a separate thread
            //new Thread(game::start).start();
        }
    }
    

    private static void handleRankMode() {
        if (rankQueue.size() >= 2) { // At least two players needed for a game
            List<Socket> players = new ArrayList<>();
    
            // Retrieve players from the queue
            for (int i = 0; i < 2; i++) {
                Socket playerSocket = rankQueue.poll();
                players.add(playerSocket);
            }
    
            // Pair players with similar levels
            Socket player1 = players.get(0);
            Socket player2 = players.get(1);
    
            int player1Level = playerLevels.getOrDefault(player1, 1); // Default level is 1
            int player2Level = playerLevels.getOrDefault(player2, 1); // Default level is 1
    
            // Check if players have similar levels
            if (Math.abs(player1Level - player2Level) <= 1) {
                // Players have similar levels, inform them and start the game
                try {
                    PrintWriter writer1 = new PrintWriter(player1.getOutputStream(), true);
                    PrintWriter writer2 = new PrintWriter(player2.getOutputStream(), true);
    
                    writer1.println("You are matched with an opponent. Get ready for the game!");
                    writer2.println("You are matched with an opponent. Get ready for the game!");
    
                    // Create a new game instance with the paired players
                    Game game = new Game(player1, player2,lock); // Pass playerChoices, lock, and gameReady
                    games.add(game);
                    // game.start();
    
                    System.out.println("New game started between players with similar levels.");
    
                    // Start the game instance in a separate thread
                    // new Thread(game::start).start();
                } catch (IOException ex) {
                    System.out.println("Error informing clients: " + ex.getMessage());
                }
            } else {
                // Players have different levels, put them back into the queue
                rankQueue.addAll(players);
            }
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
}