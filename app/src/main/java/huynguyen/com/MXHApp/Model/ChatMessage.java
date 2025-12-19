package huynguyen.com.MXHApp.Model;

import com.google.firebase.database.PropertyName;

public class ChatMessage {
    private String message;
    private String senderId;
    private String receiverId;
    private long timestamp;
    private boolean seen;
    private String type; // ADDED: To distinguish between "text" and "image"

    public ChatMessage() {
    }

    public ChatMessage(String message, String senderId, String receiverId, long timestamp, boolean isSeen, String type) {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.seen = isSeen;
        this.type = type; // ADDED
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("seen") // ADDED: to match the database field name
    public boolean isSeen() {
        return seen;
    }

    public String getType() { // ADDED
        return type;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("seen") // ADDED: to match the database field name
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setType(String type) { // ADDED
        this.type = type;
    }
}
