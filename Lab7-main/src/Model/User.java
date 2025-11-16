package Model;

public abstract class User {
    private String userId;
    private String username;
    private String email;
    private String passwordHash;
    private int role;

    public static final int RoleStudent = 1;
    public static final int RoleInstructor = 2;

    public User() {
    }

    public User(String userId, String username, String email, String passwordHash, int role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }

    public boolean isStudent() { return this.role == RoleStudent; }
    public boolean isInstructor() { return this.role == RoleInstructor; }

    public String getRoleString() {
        switch (this.role) {
            case RoleStudent: return "Student";
            case RoleInstructor: return "Instructor";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + getRoleString() +
                '}';
    }
}