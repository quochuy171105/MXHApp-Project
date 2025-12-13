package huynguyen.com.MXHApp.Model;

// A data class that holds all the necessary information for a single post item in the RecyclerView.
public class PostItem {
    private Posts post;
    private User user;
    private long likeCount;
    private long commentCount;
    private boolean isLiked;
    private boolean isSaved;

    public PostItem(Posts post, User user, long likeCount, long commentCount, boolean isLiked, boolean isSaved) {
        this.post = post;
        this.user = user;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLiked = isLiked;
        this.isSaved = isSaved;
    }

    // Getters and Setters
    public Posts getPost() {
        return post;
    }

    public void setPost(Posts post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }
}
