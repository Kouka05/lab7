package Database;

import Model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JSONDatabaseManager {

    private static final String USERS_FILE = "Data/users.json";
    private static final String COURSES_FILE = "Data/courses.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        createFileIfNotExists(USERS_FILE);
        createFileIfNotExists(COURSES_FILE);
    }

    private static void createFileIfNotExists(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                mapper.writeValue(file, new ArrayList<>());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<User> loadUsers() {
        try {
            return mapper.readValue(new File(USERS_FILE),
                    new TypeReference<ArrayList<User>>() {
                    });
        } catch (IOException e) {
            System.out.println("Error loading users!");
            return new ArrayList<>();
        }
    }
    public static void saveUsers(ArrayList<User> users) {
        try {
            for (User u : users) {
                if (u instanceof Student) {
                    ((Student) u).setType("student");
                } else if (u instanceof Instructor) {
                    ((Instructor) u).setType("instructor");
                }
                else
                    ((Admin) u).setType("admin");
            }
            mapper.writeValue(new File(USERS_FILE), users);
        } catch (IOException e) {
            System.out.println("Error saving users! ");
        }
    }
    public static boolean isEmailTaken(String email) {
        for (User u : loadUsers()) {
            if (u.getEmail().equalsIgnoreCase(email))
                return true;
        }
        return false;
    }
    public static boolean isUserIdUnique(String id) {
        for (User u : loadUsers()) {
            if (u.getUserId().equals(id))
                return false;
        }
        return true;
    }
    public static ArrayList<Course> loadCourses() {
        try {
            return mapper.readValue(new File(COURSES_FILE),
                    new TypeReference<ArrayList<Course>>() {
                    });
        } catch (IOException e) {
            System.out.println("Error loading courses!");
            return new ArrayList<>();
        }
    }
    public static void saveCourses(ArrayList<Course> courses) {
        try {
            mapper.writeValue(new File(COURSES_FILE), courses);
        } catch (IOException e) {
            System.out.println("Error saving courses! ");
        }
    }
}