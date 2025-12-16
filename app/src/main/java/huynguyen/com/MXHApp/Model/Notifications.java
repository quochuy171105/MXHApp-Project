package huynguyen.com.MXHApp.Model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

// Standard model for all new notifications.
// All backward-compatibility logic has been removed to ensure stability.
public class Notifications {

    private String userId;
    private String text;
    private String postId;
    private boolean isPost;
    private boolean isRead;
    private String receiver;
    private Date timestamp;

    // No-argument constructor required for Firestore
    public Notifications() {
    }

    // --- Standard Getters ---
    public String getUserId() { return userId; }
    public String getText() { return text; }
    public String getPostId() { return postId; }
    public boolean isPost() { return isPost; }
    public boolean isRead() { return isRead; }
    public String getReceiver() { return receiver; }
    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }

    // --- Standard Setters ---
    public void setUserId(String userId) { this.userId = userId; }
    public void setText(String text) { this.text = text; }
    public void setPostId(String postId) { this.postId = postId; }
    public void setPost(boolean post) { isPost = post; }
    public void setRead(boolean read) { isRead = read; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
