import java.io.*;
import java.net.*;
import java.util.*;

public class GameClient {
    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Ask if the client wants to reconnect using a token
            System.out.println(serverReader.readLine()); // "Do you want to reconnect using a session token? (yes/no)"
            String reconnectChoice = reader.readLine();
            writer.println(reconnectChoice);

            if (reconnectChoice.equalsIgnoreCase("yes")) {
                System.out.println(serverReader.readLine()); // "Enter your session token:"
                String token = reader.readLine();
                writer.println(token);
                System.out.println(serverReader.readLine()); // "Reconnected successfully with token: " or "Invalid token. Please login normally."
            }

            if (!reconnectChoice.equalsIgnoreCase("yes") || serverReader.readLine().contains("Invalid token")) {
                // Proceed with normal login
                System.out.println(serverReader.readLine()); // "Enter your username:"
                String username = reader.readLine();
                writer.println(username);

                System.out.println(serverReader.readLine()); // "Enter your password:"
                String password = reader.readLine();
                writer.println(password);

                System.out.println(serverReader.readLine()); // "Login successful! Choose a mode:" or error message

                // Read session token
                System.out.println(serverReader.readLine()); // "Your session token is: ..."
                System.out.println(serverReader.readLine()); // Token value
            }

            // Choose a mode
            String modeChoice = reader.readLine();
            writer.println(modeChoice);

            // Handle queue and game messages
            String serverMessage;
            while ((serverMessage = serverReader.readLine()) != null) {
                System.out.println(serverMessage);
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
