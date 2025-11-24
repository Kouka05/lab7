package Logic;

import Database.JSONDatabaseManager;
import Model.*;

import java.util.ArrayList;
import java.util.HashMap;

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

        // Ensure the student is not already enrolled
        if (targetStudent.getEnrolledCourses().contains(courseId)) {
            System.out.println("Student is already enrolled in this course: " + courseId);
            return false;
        }

        targetCourse.addStudent(studentId);
        targetStudent.enrollCourse(courseId);

        // Initialize progress for the new course - set to 0%
        targetStudent.updateProgress(courseId, 0);

        // Ensure lesson progress map is initialized and empty for this course
        if (targetStudent.getLessonProgress() == null) {
            targetStudent.setLessonProgress(new HashMap<>());
        }

        // Don't initialize any lesson progress - let it be created when students actually take quizzes
        JSONDatabaseManager.saveCourses(courses);
        JSONDatabaseManager.saveUsers(users);
        System.out.println("Student " + studentId + " enrolled in course " + courseId + " with initial progress 0%");
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

    public static boolean updateLessonProgress(String studentId, String courseId, String lessonId, Integer quizScore) {
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

        // Get or create lesson progress
        String progressKey = courseId + lessonId;
        LessonProgress lessonProgress = student.getLessonProgress()
                .computeIfAbsent(progressKey, k -> new LessonProgress());

        // Update quiz score if provided
        if (quizScore != null&&quizScore>50) {
            lessonProgress.setQuizScore(quizScore);
            lessonProgress.setCompleted(true); // Mark as completed when quiz score is provided
        }

        // Update course progress based on completed lessons
        if(quizScore>=50) {
            int totalLessons = course.getLessons().size();
            if (totalLessons > 0) {
                int completedLessons = countCompletedLessons(student, course);
                System.out.println("Completed lessons: " + completedLessons);
                int newProgress = (completedLessons * 100) / totalLessons;
                student.updateProgress(courseId, newProgress);

                // Check if course is completed for certificate
                if (newProgress == 100) {
                    generateCertificate(student, courseId);
                }
            }

            JSONDatabaseManager.saveCourses(courses);
            JSONDatabaseManager.saveUsers(users);
            System.out.println("Lesson progress updated for student " + studentId);
            return true;
        }else
            return false;
    }

    private static int countCompletedLessons(Student student, Course course) {
        int completed = 0;
        for (Lesson lesson : course.getLessons()) {
            // Use the same key format as in updateLessonProgress: courseId + lessonId
            String progressKey = course.getCourseId() + lesson.getLessonId();
            LessonProgress progress = student.getLessonProgress().get(progressKey);
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

    public static boolean isLessonAccessible(Student student, String courseId, String lessonId) {
        Course course = CourseService.getCourseById(courseId);
        if (course == null) return false;

        // Find the index of the current lesson
        int currentLessonIndex = -1;
        for (int i = 0; i < course.getLessons().size(); i++) {
            if (course.getLessons().get(i).getLessonId().equals(lessonId)) {
                currentLessonIndex = i;
                break;
            }
        }

        // First lesson is always accessible
        if (currentLessonIndex == 0) return true;

        // Check if previous lesson is completed
        if (currentLessonIndex > 0) {
            Lesson previousLesson = course.getLessons().get(currentLessonIndex - 1);
            // Use the same key format as in updateLessonProgress: courseId + lessonId
            String previousKey = courseId + previousLesson.getLessonId();
            LessonProgress previousProgress = student.getLessonProgress().get(previousKey);
            return previousProgress != null && previousProgress.isCompleted();
        }

        return false;
    }

    public static LessonProgress getLessonProgress(Student student, String courseId, String lessonId) {
        if (student == null || student.getLessonProgress() == null) {
            return null;
        }
        // Use the same key format as in updateLessonProgress: courseId + lessonId
        String progressKey = courseId + lessonId;
        return student.getLessonProgress().get(progressKey);
    }

    public static boolean validateStudentProgress(Student student, String courseId) {
        if (student == null || courseId == null) return false;

        // Check if student is actually enrolled in the course
        if (!student.getEnrolledCourses().contains(courseId)) {
            System.out.println("Student " + student.getUserId() + " is not enrolled in course " + courseId);
            return false;
        }

        Course course = CourseService.getCourseById(courseId);
        if (course == null) {
            System.out.println("Course " + courseId + " not found");
            return false;
        }

        // Validate that progress doesn't exceed 100% or show completion without actual quiz results
        Integer courseProgress = student.getProgress().get(courseId+course.getCourseId());
        if (courseProgress != null && courseProgress > 100) {
            System.out.println("Invalid progress value: " + courseProgress + " for student " + student.getUserId());
            student.updateProgress(courseId, 100); // Cap at 100%
        }

        // Validate lesson progress - ensure completed lessons have quiz scores
        if (student.getLessonProgress() != null) {
            for (Lesson lesson : course.getLessons()) {
                // Use the same key format as in updateLessonProgress: courseId + lessonId
                String progressKey = courseId + lesson.getLessonId();
                LessonProgress lp = student.getLessonProgress().get(progressKey);
                if (lp != null && lp.isCompleted() && lp.getQuizScore() == null) {
                    System.out.println("Invalid: Lesson " + lesson.getLessonId() + " marked completed without quiz score");
                    lp.setCompleted(false); // Reset completion status
                }
            }
        }

        return true;
    }
}