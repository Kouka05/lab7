package UI;
import Database.JSONDatabaseManager;
import Model.Admin;
import Model.User;
import UI.LoginFrame;
import Utilities.Validation;

import javax.swing.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
//        ArrayList<User> users = JSONDatabaseManager.loadUsers();
//        Admin admin = new Admin("A1", "admin", "admin@gmail.com", Validation.hashPassword("123456"), User.RoleAdmin);
//        users.add(admin);
//        JSONDatabaseManager.saveUsers(users);
        new LoginFrame().setVisible(true);
    }
}