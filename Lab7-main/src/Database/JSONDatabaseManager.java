package Database;

import Model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class JSONDatabaseManager {
    private static final String USERS_FILE = "Data/users.json";
    private static final String COURSES_FILE = "Data/courses.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(User.class, new UserTypeAdapter())
            .create();

    static {
        createFileIfNotExists(USERS_FILE);
        createFileIfNotExists(COURSES_FILE);
    }

    private static void createFileIfNotExists(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                if (filePath.equals(USERS_FILE)) {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("[]");
                    }
                } else if (filePath.equals(COURSES_FILE)) {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("[]");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class UserTypeAdapter extends TypeAdapter<User> {
        @Override
        public void write(JsonWriter out, User user) throws IOException {
            if (user == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            out.name("userId").value(user.getUserId());
            out.name("username").value(user.getUsername());
            out.name("email").value(user.getEmail());
            out.name("passwordHash").value(user.getPasswordHash());
            out.name("role").value(user.getRole());

            if (user instanceof Student) {
                Student student = (Student) user;
                out.name("enrolledCourses");
                gson.toJson(student.getEnrolledCourses(), ArrayList.class, out);
                out.name("progress");
                gson.toJson(student.getProgress(), new TypeToken<java.util.Map<String, Integer>>(){}.getType(), out);
            } else if (user instanceof Instructor) {
                Instructor instructor = (Instructor) user;
                out.name("createdCourses");
                gson.toJson(instructor.getCreatedCourses(), ArrayList.class, out);
            }

            out.endObject();
        }

        @Override
        public User read(JsonReader in) throws IOException {
            in.beginObject();

            String userId = null;
            String username = null;
            String email = null;
            String passwordHash = null;
            int role = 0;
            ArrayList<String> enrolledCourses = new ArrayList<>();
            java.util.Map<String, Integer> progress = new java.util.HashMap<>();
            ArrayList<String> createdCourses = new ArrayList<>();

            while (in.hasNext()) {
                String fieldName = in.nextName();
                switch (fieldName) {
                    case "userId":
                        userId = in.nextString();
                        break;
                    case "username":
                        username = in.nextString();
                        break;
                    case "email":
                        email = in.nextString();
                        break;
                    case "passwordHash":
                        passwordHash = in.nextString();
                        break;
                    case "role":
                        role = in.nextInt();
                        break;
                    case "enrolledCourses":
                        enrolledCourses = gson.fromJson(in, new TypeToken<ArrayList<String>>(){}.getType());
                        break;
                    case "progress":
                        progress = gson.fromJson(in, new TypeToken<java.util.Map<String, Integer>>(){}.getType());
                        break;
                    case "createdCourses":
                        createdCourses = gson.fromJson(in, new TypeToken<ArrayList<String>>(){}.getType());
                        break;
                    default:
                        in.skipValue();
                        break;
                }
            }
            in.endObject();
            if (role == User.RoleStudent) {
                Student student = new Student(userId, username, email, passwordHash, role);
                student.setEnrolledCourses(enrolledCourses);
                student.setProgress(progress);
                return student;
            } else if (role == User.RoleInstructor) {
                Instructor instructor = new Instructor(userId, username, email, passwordHash, role);
                instructor.setCreatedCourses(createdCourses);
                return instructor;
            } else {
                return new Student(userId, username, email, passwordHash, role);
            }
        }
    }

    public static ArrayList<User> loadUsers() {
        try (FileReader reader = new FileReader(USERS_FILE)) {
            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            ArrayList<User> users = gson.fromJson(reader, userListType);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveUsers(ArrayList<User> listOfUsers) {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            gson.toJson(listOfUsers, writer);
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public static ArrayList<Course> loadCourses() {
        try (FileReader reader = new FileReader(COURSES_FILE)) {
            Type courseListType = new TypeToken<ArrayList<Course>>() {}.getType();
            ArrayList<Course> courses = gson.fromJson(reader, courseListType);
            return courses != null ? courses : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Error loading courses: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveCourses(ArrayList<Course> listOfCourses) {
        try (FileWriter writer = new FileWriter(COURSES_FILE)) {
            gson.toJson(listOfCourses, writer);
        } catch (IOException e) {
            System.out.println("Error saving courses: " + e.getMessage());
        }
    }

    public static boolean isEmailTaken(String email) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUserIdUnique(String id) {
        ArrayList<User> users = loadUsers();
        for (User u : users) {
            if (u.getUserId().equals(id)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCourseIdUnique(String id) {
        ArrayList<Course> courses = loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(id)) {
                return false;
            }
        }
        return true;
    }
}