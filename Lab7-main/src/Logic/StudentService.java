package Logic;

import Database.JSONDatabaseManager;
import Model.Course;
import Model.Student;
import Model.User;
import Model.Lesson;
import java.util.ArrayList;

public class StudentService {

    public static boolean enroll(String studentId, String courseId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();

        Course targetCourse = null;
        Student targetStudent = null;
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                targetCourse = c;
                break;
            }
        }
        if (targetCourse == null) {
            System.out.println("Course not found: " + courseId);
            return false;
        }
        if(targetCourse.getApprovalStatus()!=Course.APPROVED)
        {
            System.out.println("Course is not approved yet!");
            return false;
        }
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                targetStudent = (Student) u;
                break;
            }
        }
        if (targetStudent == null) {
            System.out.println("Student not found: " + studentId);
            return false;
        }
        targetCourse.addStudent(studentId);
        targetStudent.enrollCourse(courseId);
        JSONDatabaseManager.saveCourses(courses);
        JSONDatabaseManager.saveUsers(users);
        System.out.println("Student " + studentId + " enrolled in course " + courseId);
        return true;
    }
    public static ArrayList<Course> getEnrolledCourses(String studentId) {
        ArrayList<Course> allCourses = JSONDatabaseManager.loadCourses();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        ArrayList<Course> enrolledCourses = new ArrayList<>();
        Student student = null;
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                student = (Student) u;
                break;
            }
        }
        if (student == null) return enrolledCourses;
        for (Course c : allCourses) {
            if (student.getEnrolledCourses().contains(c.getCourseId())&&c.getApprovalStatus()==Course.APPROVED) {
                enrolledCourses.add(c);
            }
        }
        return enrolledCourses;
    }
    public static boolean unenroll(String studentId, String courseId) {
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
            System.out.println("Student " + studentId + " unenrolled from course " + courseId);
        }
        return success;
    }
    public static boolean markLessonCompleted(String studentId, String courseId, String lessonId) {
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        Student student = null;
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                student = (Student) u;
                break;
            }
        }
        if (student == null) {
            System.out.println("Student not found: " + studentId);
            return false;
        }
        Course course = null;
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                course = c;
                break;
            }
        }
        if (course == null) {
            System.out.println("Course not found: " + courseId);
            return false;
        }
        int totalLessons = course.getLessons().size();
        if (totalLessons == 0) {
            System.out.println("No lessons in course");
            return false;
        }
        Lesson lessonCompleted = null;
        for (Lesson lesson : course.getLessons()) {
            if (lesson.getLessonId().equals(lessonId)) {
                lessonCompleted = lesson;
                break;
            }
        }
        if (lessonCompleted == null) return false;
        if (!lessonCompleted.isCompleted()) {
            lessonCompleted.setCompleted(true);
            int currentProgress = student.getProgress().getOrDefault(courseId, 0);
            int progressPerLesson = 100 / totalLessons;
            int newProgress = Math.min(100, currentProgress + progressPerLesson);
            student.updateProgress(courseId, newProgress);
            JSONDatabaseManager.saveCourses(courses);
            JSONDatabaseManager.saveUsers(users);
            System.out.println("Lesson " + lessonId + " marked completed for student " + studentId);
            return true;
        } else {
            System.out.println("Lesson already completed");
            return false;
        }
    }
}