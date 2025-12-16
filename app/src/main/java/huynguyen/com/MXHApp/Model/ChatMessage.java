package huynguyen.com.MXHApp.Model;

public class ChatMessage {
    private String message;
    private String senderId;
    private String receiverId;
    private long timestamp;
    private boolean isSeen;

    public ChatMessage() {
    }

    public ChatMessage(String message, String senderId, String receiverId, long timestamp, boolean isSeen) {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
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

    public boolean isSeen() {
        return isSeen;
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

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
