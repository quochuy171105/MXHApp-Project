package huynguyen.com.MXHApp.Model;

public class Conversation {
    private String id;
    private String username;
    private String profileUrl;
    private String lastMessage;
    private long timestamp;
    private boolean isSeen; // ADDED
    private String lastMessageSenderId; // ADDED

    public Conversation() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSeen() { // ADDED
        return isSeen;
    }

    public String getLastMessageSenderId() { // ADDED
        return lastMessageSenderId;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSeen(boolean seen) { // ADDED
        isSeen = seen;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) { // ADDED
        this.lastMessageSenderId = lastMessageSenderId;
    }
}
