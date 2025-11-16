package Model;
import java.util.ArrayList;

public class Instructor extends User {
    private ArrayList<String> createdCourses;
    public Instructor() {
        super();
        this.createdCourses = new ArrayList<>();
    }
    public Instructor(String userId, String username, String email, String passwordHash, int role) {
        super(userId, username, email, passwordHash, role);
        this.createdCourses = new ArrayList<>();
    }
    public ArrayList<String> getCreatedCourses() {
        return createdCourses;
    }
    public void setCreatedCourses(ArrayList<String> createdCourses) {
        this.createdCourses = createdCourses != null ? createdCourses : new ArrayList<>();
    }
    public void addCourse(String courseId) {
        if (!createdCourses.contains(courseId)) {
            createdCourses.add(courseId);
        }
    }

    public void removeCourse(String courseId) {
        createdCourses.remove(courseId);
    }
}