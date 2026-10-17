package cms;


public abstract class User {

    private final String userID;
    private String username;
    private String password;
    private String name;
    private final Role role;

    public User(String userID, String username, String password, String name, Role role) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // ---------- GETTERS ----------
    public String getUserID() { return userID; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    // ---------- SETTERS ----------
    // userID is not meant to change after account creation, so no setter for it
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }

    public boolean login(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }

    /**
     * ABSTRACT METHOD - each role class MUST override this.
     */
    public abstract String getDashboardTitle();

    // Convert this user to a pipe-delimited line for saving to users.txt
    public String toFileString() {
        return userID + "|" + username + "|" + password + "|" + name + "|" + role.name();
    }

    @Override
    public String toString() {
        return "[" + role.name() + "] " + name + " (" + userID + ")";
    }
}
