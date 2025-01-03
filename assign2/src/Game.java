/*import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Game {
    private Socket player1;
    private Socket player2;
    private Lock lock;

    public Game(Socket player1, Socket player2, Lock lock) {
        this.player1 = player1;
        this.player2 = player2;
        this.lock = lock;
    }

    public void start() {
        try {
            // Prompt player 1 for their choice
            //InputStream input = player1.getInputStream();
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer1 = new PrintWriter(player1.getOutputStream(), true);
            
            writer1.println("Enter your choice (1 for rock, 2 for paper, 3 for scissors): ");
            String input = reader1.readLine();
            int choice1 = Integer.parseInt(input);
            System.out.println("Player 1 chose option " + choice1 + ", wait for the other player's choice.");

            // Prompt player 2 for their choice
            PrintWriter writer2 = new PrintWriter(player2.getOutputStream(), true);
            writer2.println("Enter your choice (1 for rock, 2 for paper, 3 for scissors): ");
            
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            String input = reader2.readLine();
            int choice2 = Integer.parseInt(input);

            // Determine the winner based on the choices
            String result = determineWinner(choice1, choice2);

            // Send the choices and result to both players
            writer1.println("You chose: " + choice1);
            writer1.println("Opponent chose: " + choice2);
            writer1.println("Result: " + result);

            writer2.println("You chose: " + choice2);
            writer2.println("Opponent chose: " + choice1);
            writer2.println("Result: " + result);

        } catch (IOException ex) {
            System.out.println("Error in game: " + ex.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private String determineWinner(String choice1, String choice2) {
        // Game logic to determine the winner
        // For simplicity, I'll assume a basic logic
        if (choice1.equals(choice2)) {
            return "It's a tie!";
        } else if ((choice1.equals("1") && choice2.equals("3")) ||
                   (choice1.equals("2") && choice2.equals("1")) ||
                   (choice1.equals("3") && choice2.equals("2"))) {
            return "Player 1 wins!";
        } else {
            return "Player 2 wins!";
        }
    }
}
*/