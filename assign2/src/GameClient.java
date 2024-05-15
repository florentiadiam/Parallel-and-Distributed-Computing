import java.io.*;
import java.net.*;

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
                String reconnectionResponse = serverReader.readLine();
                if (reconnectionResponse.contains("Reconnected successfully")) {
                    System.out.println(reconnectionResponse);
                } else {
                    System.out.println(reconnectionResponse); // "Invalid token. Please login normally."
                    handleNormalLogin(reader, writer, serverReader);
                }
            } else {
                handleNormalLogin(reader, writer, serverReader);
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

    private static void handleNormalLogin(BufferedReader reader, PrintWriter writer, BufferedReader serverReader) throws IOException {
        System.out.println(serverReader.readLine()); // "Enter your username:"
        String username = reader.readLine();
        writer.println(username);

        System.out.println(serverReader.readLine()); // "Enter your password:"
        String password = reader.readLine();
        writer.println(password);

        String loginResponse = serverReader.readLine();
        if (loginResponse.contains("Login successful")) {
            System.out.println(loginResponse);
            // Read session token
            System.out.println(serverReader.readLine()); // "Your session token is: ..."
            System.out.println(serverReader.readLine()); // Token value
        } else {
            System.out.println(loginResponse);
            handleNormalLogin(reader, writer, serverReader); // Retry login
        }
    }
}
