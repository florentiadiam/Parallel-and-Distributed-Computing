import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private List<ClientSide> clients;
    private final String filename = "clients.json";

    // Constructor to initialize the database
    public Database() {
        this.clients = new ArrayList<>();
        loadDatabaseFromFile();
    }

    // Method to add a new client to the database
    public void addClient(ClientSide client) {
        clients.add(client);
        saveDatabaseToFile();
    }

    // Method to retrieve a client from the database by username
    public ClientSide getClient(String username) {
        for (ClientSide client : clients) {
            if (client.getUsername().equals(username)) {
                return client;
            }
        }
        return null; // Client not found
    }

    // Method to check if a username exists in the database
    public boolean usernameExists(String username) {
        for (ClientSide client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    // Method to remove a client from the database
    public void removeClient(String username) {
        clients.removeIf(client -> client.getUsername().equals(username));
        saveDatabaseToFile();
    }

    // Method to load database from a JSON file
    private void loadDatabaseFromFile() {
        try {
            StringBuilder jsonContent = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            br.close();

            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");
                String token = jsonObject.getString("token");
                int rank = jsonObject.getInt("rank");
                // Assuming you have a constructor in ClientSide class
                clients.add(new ClientSide(username, password, token, rank));
            }
        } catch (IOException | JSONException e) {
            System.err.println("Error loading database from file: " + e.getMessage());
        }
    }

    // Method to save database to a JSON file
    private void saveDatabaseToFile() {
        JSONArray jsonArray = new JSONArray();
        for (ClientSide client : clients) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", client.getUsername());
                jsonObject.put("password", client.getPassword());
                jsonObject.put("token", client.getToken());
                jsonObject.put("rank", client.getRank());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter file = new FileWriter(filename)) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Error saving database to file: " + e.getMessage());
        }
    }

    // Other methods for database operations can be added as needed
}

