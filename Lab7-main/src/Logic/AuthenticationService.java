package Logic;

import Database.JSONDatabaseManager;
import Model.*;
import Utilities.Validation;
import java.util.ArrayList;

public class AuthenticationService {
    private static User loggedInUser = null;

    public static boolean signUp(String username, String email, String password, int role) {
        if (!Validation.validateUsername(username)) {
            System.out.println("Invalid username. Must be at least 3 chars and alphanumeric.");
            return false;
        }
        if (!Validation.validateEmail(email)) {
            System.out.println("Invalid email format.");
            return false;
        }
        if (!Validation.validatePassword(password)) {
            System.out.println("Password must be at least 6 characters.");
            return false;
        }
        if (!Validation.validateRole(role)) {
            System.out.println("Invalid role.");
            return false;
        }
        if (JSONDatabaseManager.isEmailTaken(email)) {
            System.out.println("Email is already registered.");
            return false;
        }
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        String userId;
        do {
            userId = (role == User.RoleStudent ? "S" : "I") + (users.size() + 1);
        } while (!JSONDatabaseManager.isUserIdUnique(userId));

        String hashedPassword = Validation.hashPassword(password);
        User newUser;

        if (role == User.RoleStudent) {
            newUser = new Student(userId, username, email, hashedPassword, role);
        } else {
            newUser = new Instructor(userId, username, email, hashedPassword, role);
        }

        users.add(newUser);
        JSONDatabaseManager.saveUsers(users);
        System.out.println("Signup successful for " + username);
        return true;
    }

    public static boolean login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Email and password cannot be empty.");
            return false;
        }

        String hashedPassword = Validation.hashPassword(password);
        ArrayList<User> users = JSONDatabaseManager.loadUsers();

        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPasswordHash().equals(hashedPassword)) {
                loggedInUser = u;
                System.out.println("Login successful. Welcome " + u.getUsername() + " (" + u.getRoleString() + ")");
                return true;
            }
        }

        System.out.println("Email or password incorrect.");
        return false;
    }

    public static void logout() {
        if (loggedInUser != null) {
            System.out.println("User " + loggedInUser.getUsername() + " logged out.");
            loggedInUser = null;
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }
    public static void updateLoggedInUser(User user) {
        loggedInUser = user;
    }
    public static boolean isUserLoggedIn() {
        return loggedInUser != null;
    }
}