package huynguyen.com.MXHApp.Model;

import com.google.firebase.firestore.PropertyName;

public class User {
    private String username;
    private String email;
    private String memer;
    private String userId; // RENAMED: from user_id to follow Java conventions
    private String profileUrl;
    private String background;
    private String accountStatus;
    private String statusReason;
    private String role; // ADDED: missing role field

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String memer, String userId, String profileUrl, String background, String accountStatus, String statusReason, String role) {
        this.username = username;
        this.email = email;
        this.memer = memer;
        this.userId = userId;
        this.profileUrl = profileUrl;
        this.background = background;
        this.accountStatus = accountStatus;
        this.statusReason = statusReason;
        this.role = role;
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

    @PropertyName("user_id") // Maps the user_id field in Firestore to this property
    public String getUserId() {
        return userId;
    }

    @PropertyName("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
