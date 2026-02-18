package pack.crs.models;
import pack.crs.auth.*;
import java.io.IOException;
import java.util.List;

public class SystemAdmin extends User {

    private UserDAO userDAO;

    // constructor (inheriting from User class)
    public SystemAdmin(String userId, String username, String password, boolean active) {
        super(userId, username, password, "SystemAdmin", active);
        this.userDAO = new UserDAO("Resources/users.txt"); // file path
    }
    
    // alternate contstructor for email
    public SystemAdmin(String userId, String username, String password, boolean active, String email) {
        super(userId, username, password, "SystemAdmin", active);
        setEmail(email);
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
    
    // add new user
    public void addUser(User newUser) throws IOException {
        List<User> users = userDAO.loadAllUsers();
        users.add(newUser);
        userDAO.saveAllUsers(users);
    }

    // update existing user
    public void updateUser(User updatedUser) throws IOException {
        List<User> users = userDAO.loadAllUsers();
        for (User u : users) {
            if (u.getUserId().equals(updatedUser.getUserId())) {
                u.setUsername(updatedUser.getUsername());
                u.setPassword(updatedUser.getPassword());
                u.setRole(updatedUser.getRole());
                u.setActive(updatedUser.isActive());
                u.setEmail(updatedUser.getEmail());
                break;
            }
        }
        userDAO.saveAllUsers(users);
    }

    // deactivate (remove) user
    public void removeUser(String userId) throws IOException {
        List<User> users = userDAO.loadAllUsers();
        for (User u : users) {
            if (u.getUserId().equals(userId)) {
                u.setActive(false);
                break;
            }
        }
        userDAO.saveAllUsers(users);
    }
}
