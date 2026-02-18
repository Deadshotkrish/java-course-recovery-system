package pack.crs.models;

public abstract class User {

    private String userId;
    private String username;
    private String password;
    private String role;     // either "SystemAdmin" or "AcademicOfficer"
    private boolean active;  // true = active, false = deactivated
    private String email;

    // user constructor (default scenario)
    public User(String userId, String username, String password, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }
    
    // override if we want to use email
    public User(String userId, String username, String password, String role, boolean active, String email) {
        this(userId, username, password, role, active);
        this.email = email;
    }

    // getter and setter methods
    public String getUserId() { 
        return userId; }
    public void setUserId(String userId) { 
        this.userId = userId; }

    public String getUsername() { 
        return username; }
    public void setUsername(String username) { 
        this.username = username; }

    public String getPassword() { 
        return password; }
    public void setPassword(String password) { 
        this.password = password; }

    public String getRole() { 
        return role; }
    public void setRole(String role) { 
        this.role = role; }

    public boolean isActive() { 
        return active; }
    public void setActive(boolean active) { 
        this.active = active; }
    
    public String getEmail() {
        return email; }
    public void setEmail(String email) {
        this.email = email; }

    @Override
    public String toString() {
        return String.format("%s (%s)", username, role);
    }
}
