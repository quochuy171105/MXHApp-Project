package huynguyen.com.MXHApp.Model;

public class User {
    private String username;
    private String email;
    private String memer;
    private String user_id;
    private String profileUrl;
    private String background;
    private String accountStatus;
    private String statusReason;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String memer, String user_id, String profileUrl, String background, String accountStatus, String statusReason) {
        this.username = username;
        this.email = email;
        this.memer = memer;
        this.user_id = user_id;
        this.profileUrl = profileUrl;
        this.background = background;
        this.accountStatus = accountStatus;
        this.statusReason = statusReason;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMemer() {
        return memer;
    }

    public void setMemer(String memer) {
        this.memer = memer;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
}
