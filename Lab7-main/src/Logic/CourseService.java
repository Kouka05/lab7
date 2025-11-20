package Logic;

import Database.JSONDatabaseManager;
import Model.*;
import Utilities.Validation;
import java.util.ArrayList;

public class CourseService {

    public static boolean createCourse(String instructorId, String title, String description) {
        if (instructorId == null || instructorId.isEmpty()) {
            System.out.println("Instructor ID is required.");
            return false;
        }
        if (title == null || title.isEmpty()) {
            System.out.println("Course title is required.");
            return false;
        }

        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course course : courses) {
            if (course.getTitle().equalsIgnoreCase(title)) {
                System.out.println("Course with name '" + title + "' already exists.");
                return false;
            }
        }

        String courseId = generateNextCourseId(courses);

        Course newCourse = new Course(courseId, title, description, instructorId);
        courses.add(newCourse);
        JSONDatabaseManager.saveCourses(courses);
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        for (User u : users) {
            if (u instanceof Instructor && u.getUserId().equals(instructorId)) {
                Instructor inst = (Instructor) u;
                inst.addCourse(courseId);
                break;
            }
        }
        JSONDatabaseManager.saveUsers(users);

        System.out.println("Course created successfully: " + title + " (ID: " + courseId + ")");
        return true;
    }

    private static String generateNextCourseId(ArrayList<Course> courses) {
        if (courses.isEmpty()) {
            return "C1";
        }
        int maxId = 0;
        for (Course course : courses) {
            try {
                String idStr = course.getCourseId().substring(1); // Remove "C" prefix
                int idNum = Integer.parseInt(idStr);
                if (idNum > maxId) {
                    maxId = idNum;
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid course ID format: " + course.getCourseId());
            }
        }

        return "C" + (maxId + 1);
    }

    public static boolean editCourse(String courseId, String newTitle, String newDescription) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                if (newTitle != null && !newTitle.isEmpty()) {
                    for (Course otherCourse : courses) {
                        if (!otherCourse.getCourseId().equals(courseId) &&
                                otherCourse.getTitle().equalsIgnoreCase(newTitle)) {
                            System.out.println("Course with name '" + newTitle + "' already exists.");
                            return false;
                        }
                    }
                    c.setTitle(newTitle);
                }
                if (newDescription != null) c.setDescription(newDescription);
                JSONDatabaseManager.saveCourses(courses);
                System.out.println("Course updated successfully: " + courseId);
                return true;
            }
        }
        System.out.println("Course not found: " + courseId);
        return false;
    }
    public static boolean removeStudentFromCourse(String courseId, String studentId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();

        boolean success = false;
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.removeStudent(studentId);
                success = true;
                break;
            }
        }
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                Student student = (Student) u;
                student.dropCourse(courseId);
                success = true;
                break;
            }
        }

        if (success) {
            JSONDatabaseManager.saveCourses(courses);
            JSONDatabaseManager.saveUsers(users);
            System.out.println("Student " + studentId + " removed from course " + courseId);
        }

        return success;
    }
    public static boolean deleteCourse(String courseId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        boolean removed = courses.removeIf(c -> c.getCourseId().equals(courseId));

        if (removed) {
            JSONDatabaseManager.saveCourses(courses);
            ArrayList<User> users = JSONDatabaseManager.loadUsers();
            for (User u : users) {
                if (u instanceof Instructor) {
                    Instructor inst = (Instructor) u;
                    inst.removeCourse(courseId);
                }
                if (u instanceof Student) {
                    Student student = (Student) u;
                    student.dropCourse(courseId);
                }
            }
            JSONDatabaseManager.saveUsers(users);

            System.out.println("Course deleted successfully: " + courseId);
            return true;
        }
        System.out.println("Course not found: " + courseId);
        return false;
    }

    public static ArrayList<Course> getCourseByInstructor(String instructorId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        ArrayList<Course> result = new ArrayList<>();

        for (Course c : courses) {
            if (c.getInstructorId().equals(instructorId)) {
                result.add(c);
            }
        }
        return result;
    }

    public static ArrayList<Course> getAllCourses() {
        return JSONDatabaseManager.loadCourses();
    }
    public static ArrayList<Course> getAllCoursesForStudent() {
        return getApprovedCourses();
    }

    public static ArrayList<Course> getApprovedCourses() {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        ArrayList<Course> approvedCourses = new ArrayList<>();
        for (Course c : courses) {
            if (c.getApprovalStatus() == Course.APPROVED) {
                approvedCourses.add(c);
            }
        }
        return approvedCourses;
    }
    public static Course getCourseById(String courseId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                return c;
            }
        }
        return null;
    }
    public static Course getCourseByIdForStudent(String courseId) {
        ArrayList<Course> courses = getApprovedCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                return c;
            }
        }
        return null;
    }
}