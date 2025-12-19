package huynguyen.com.MXHApp.Model;

import com.google.firebase.database.PropertyName;

public class Conversation {
    private String id;
    private String username;
    private String profileUrl;
    private String lastMessage;
    private long timestamp;
    private boolean seen;
    private String lastMessageSenderId;

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

    @PropertyName("seen") // MODIFIED: to match the database field name
    public boolean isSeen() {
        return seen;
    }

    public String getLastMessageSenderId() {
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

    @PropertyName("seen") // MODIFIED: to match the database field name
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }
}
