import java.io.*;
import java.util.*;

public class Database {
    private static final String FILENAME = "userdata.txt";
    private Map<String, String> userCredentials;

    public Database() {
        this.userCredentials = loadUserDataFromFile();
    }

    // Load user data from file
    private Map<String, String> loadUserDataFromFile() {
        Map<String, String> userData = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    userData.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userData;
    }

    // Save user data to file
    private void saveUserDataToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                bw.write(entry.getKey() + ":" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Register a new user with username and password
    public boolean registerUser(String username, String password) {
        if (userCredentials.containsKey(username)) {
            // Username already exists
            return false;
        } else {
            userCredentials.put(username, password);
            saveUserDataToFile(); // Save user data to file
            return true;
        }
    }

    // Validate username and password for login
    public boolean validateUser(String username, String password) {
        String storedPassword = userCredentials.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }
}
