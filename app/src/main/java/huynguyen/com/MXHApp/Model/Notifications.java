package huynguyen.com.MXHApp.Model;

public class Notifications {
    private String userid;
    private String comment;
    private String postid;
    // REFACTORED: Renamed field for better deserialization
    private boolean post;

    public Notifications(String userid, String comment, String postid, boolean post) {
        this.userid = userid;
        this.comment = comment;
        this.postid = postid;
        this.post = post;
    }

    public Notifications(){}

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    // REFACTORED: Renamed getter to standard Java convention
    public boolean isPost() {
        return post;
    }

    // REFACTORED: Renamed setter to standard Java convention
    public void setPost(boolean post) {
        this.post = post;
    }
}
