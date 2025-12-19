package huynguyen.com.MXHApp.Model;

import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notifications {

    // Restored to camelCase for consistency with Java conventions
    private String userId;
    private String text;
    private String postId;
    private boolean isPost;
    private boolean isRead;
    private String receiver;
    private Date timestamp;
    private String comment;

    // No-argument constructor required for Firestore
    public Notifications() {
    }

    // --- Getters ---
    @PropertyName("userid") // This annotation maps the getter to the Firestore field "userid"
    public String getUserId() { return userId; }

    public String getText() { return text; }

    @PropertyName("postid") // Maps to "postid"
    public String getPostId() { return postId; }

    public String getReceiver() { return receiver; }

    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }

    @PropertyName("ispost") // Maps to "ispost"
    public boolean isPost() { return isPost; }

    @PropertyName("isRead") // Maps to "isRead"
    public boolean isRead() { return isRead; }

    public String getComment() { return comment; }

    // --- Setters ---
    @PropertyName("userid")
    public void setUserId(String userId) { this.userId = userId; }

    public void setText(String text) { this.text = text; }

    @PropertyName("postid")
    public void setPostId(String postId) { this.postId = postId; }

    public void setReceiver(String receiver) { this.receiver = receiver; }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @PropertyName("ispost")
    public void setPost(boolean post) { isPost = post; }

    @PropertyName("isRead")
    public void setRead(boolean read) { isRead = read; }

    public void setComment(String comment) { this.comment = comment; }
}
