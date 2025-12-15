package huynguyen.com.MXHApp.Model;

public class Comment {
    private String comment;
    private String publisher;
    private String time;
    private String commentid; // ADDED: Field for the comment's unique ID

    public Comment(String comment, String publisher, String time, String commentid) {
        this.comment = comment;
        this.publisher = publisher;
        this.time = time;
        this.commentid = commentid;
    }

    public Comment()
    {

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // ADDED: Getter and Setter for commentid
    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }
}
