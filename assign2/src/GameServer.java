import java.io.*;
import java.net.*;
import java.util.*;

//to implement ranked levels and when queue time is high put players with different ranks
public class GameServer {
    private static Queue<Socket> clientQueue = new LinkedList<>();
    private static String mode;
    private static int batchSize = 4; // Change this to the desired batch size
    private static List<Game> games = new ArrayList<>();
    private static Map<Socket, Integer> playerLevels = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                // Prompt the client with mode selection options
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Welcome to the game server!");
                writer.println("Choose a mode:");
                writer.println("1. Simple");
                writer.println("2. Rank");
                writer.println("Enter your choice (1 or 2):");

                // Handle client connection
                handleClient(socket);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String choice = reader.readLine();

            // Process client's mode selection
            if (choice.equals("1")) {
                mode = "simple";
                System.out.println("Simple mode selected.");
            } else if (choice.equals("2")) {
                mode = "rank";
                System.out.println("Rank mode selected.");
            } else {
                System.out.println("Invalid mode selection.");
                return;
            }

            // Add the new client to the queue
            clientQueue.add(socket);

            // Inform the client about their status in the queue
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("You are in the queue. Please wait for other players.");

            // Print the updated queue in the terminal
            printQueue();

            // Handle client connection based on the chosen mode
            if (mode.equals("simple")) {
                handleSimpleMode();
            } else if (mode.equals("rank")) {
                handleRankMode();
            }
        } catch (IOException ex) {
            System.out.println("Client disconnected: " + ex.getMessage());
            clientQueue.remove(socket);
            playerLevels.remove(socket); // Remove player's level when disconnected
            printQueue();
        }
    }

    private static void handleSimpleMode() {
        if (clientQueue.size() >= batchSize) {
            List<Socket> batch = new ArrayList<>();

            // Create a batch of clients for the game instance
            for (int i = 0; i < batchSize; i++) {
                batch.add(clientQueue.poll());
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
        if (clientQueue.size() >= batchSize) {
            List<Socket> batch = new ArrayList<>();
            List<Socket> team = new ArrayList<>();

            // Create a batch of clients for the game instance
            for (int i = 0; i < batchSize; i++) {
                Socket clientSocket = clientQueue.poll();
                batch.add(clientSocket);
                team.add(clientSocket);
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
            Game game = new Game(team1, team2); // Pass teams instead of batchSize
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
        System.out.println("Current clients in the queue: " + clientQueue.size());
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
