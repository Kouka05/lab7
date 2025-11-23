package Logic;

import Database.JSONDatabaseManager;
import Model.*;

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
        if (targetCourse.getApprovalStatus() != Course.APPROVED) {
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
            if (student.getEnrolledCourses().contains(c.getCourseId()) && c.getApprovalStatus() == Course.APPROVED) {
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

    public static boolean updateLessonProgress(Student student, String courseId, String lessonId, Integer quizScore) {
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        
        Student s = null;
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(student.getUserId())) {
                s = (Student) u;
                break;
            }
        }
        if (s == null) {
            System.out.println("Student not found: " + student.getUserId());
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

        // Get or create lesson progress
        LessonProgress lessonProgress = s.getLessonProgress()
                .computeIfAbsent(lessonId, k -> new LessonProgress());

        // Update quiz score if provided
        if (quizScore != null) {
            lessonProgress.setQuizScore(quizScore);
            
            // Mark as completed if quiz passed (score >= 50)
            if (quizScore >= 50) {
                lessonProgress.setCompleted(true);
                
                // Update course progress
                int totalLessons = course.getLessons().size();
                if (totalLessons > 0) {
                    int completedLessons = countCompletedLessons(s, course);
                    int newProgress = (completedLessons * 100) / totalLessons;
                    s.updateProgress(courseId, newProgress);
                    
                    // Check if course is completed for certificate
                    if (newProgress == 100) {
                        generateCertificate(s, courseId);
                    }
                }
            }
        }

        JSONDatabaseManager.saveCourses(courses);
        JSONDatabaseManager.saveUsers(users);
        System.out.println("Lesson progress updated for student " + s.getUserId());
        return true;
    }

    private static int countCompletedLessons(Student student, Course course) {
        int completed = 0;
        for (Lesson lesson : course.getLessons()) {
            LessonProgress progress = student.getLessonProgress().get(lesson.getLessonId());
            if (progress != null && progress.isCompleted()) {
                completed++;
            }
        }
        return completed;
    }

    private static void generateCertificate(Student student, String courseId) {
        // Check if certificate already exists
        for (Certificate cert : student.getCertificates()) {
            if (cert.getCourseId().equals(courseId)) {
                return; // Certificate already exists
            }
        }

        // Generate new certificate
        String certificateId = "CERT_" + student.getUserId() + "_" + courseId + "_" + System.currentTimeMillis();
        Certificate certificate = new Certificate(certificateId, student.getUserId(), courseId);
        student.addCertificate(certificate);
        
        System.out.println("Certificate generated: " + certificateId + " for student " + student.getUserId());
    }

    public static ArrayList<Certificate> getStudentCertificates(String studentId) {
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                return ((Student) u).getCertificates();
            }
        }
        return new ArrayList<>();
    }
}
