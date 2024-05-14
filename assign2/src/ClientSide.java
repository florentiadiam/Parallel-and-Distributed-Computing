public class ClientSide {
    private String username;
    private String password;
    private String token;
    private int rank;
    private Socket socket;
    private Queue<Long> timingQueue; // Assuming timingQueue holds Long values representing timestamps

    // Constructor
    public GameClient(String username, String password, String token, int rank, Socket socket, Queue<Long> timingQueue) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.rank = rank;
        this.socket = socket;
        this.timingQueue = timingQueue;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Queue<Long> getTimingQueue() {
        return timingQueue;
    }

    public void setTimingQueue(Queue<Long> timingQueue) {
        this.timingQueue = timingQueue;
    }

    // Other methods can be added as needed
}
