package Logic;

import Database.JSONDatabaseManager;
import Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsService {

    public static Map<String, Object> getCourseAnalytics(String courseId) {
        Map<String, Object> analytics = new HashMap<>();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        Course course = CourseService.getCourseById(courseId);

        if (course == null) {
            return analytics;
        }

        // Course statistics
        int totalStudents = course.getStudents().size();
        int completedStudents = 0;
        double totalCourseProgress = 0;

        // Lesson statistics
        Map<String, Object> lessonStats = new HashMap<>();

        for (Lesson lesson : course.getLessons()) {
            Map<String, Object> lessonData = new HashMap<>();
            double totalQuizScore = 0;
            int quizAttempts = 0;
            int completedCount = 0;

            for (User user : users) {
                if (user instanceof Student) {
                    Student student = (Student) user;
                    if (student.getEnrolledCourses().contains(courseId)) {
                        // Course completion check
                        Integer progress = student.getProgress().get(courseId);
                        if (progress != null && progress == 100) {
                            completedStudents++;
                        }
                        if (progress != null) {
                            totalCourseProgress += progress;
                        }

                        // Lesson progress and quiz scores
                        LessonProgress lp = student.getLessonProgress().get(lesson.getLessonId());
                        if (lp != null) {
                            if (lp.isCompleted()) {
                                completedCount++;
                            }
                            if (lp.getQuizScore() != null) {
                                totalQuizScore += lp.getQuizScore();
                                quizAttempts++;
                            }
                        }
                    }
                }
            }

            lessonData.put("completedCount", completedCount);
            lessonData.put("completionRate", totalStudents > 0 ? (double) completedCount / totalStudents * 100 : 0);
            lessonData.put("averageQuizScore", quizAttempts > 0 ? totalQuizScore / quizAttempts : 0);
            lessonData.put("quizAttempts", quizAttempts);

            lessonStats.put(lesson.getLessonId(), lessonData);
        }

        analytics.put("totalStudents", totalStudents);
        analytics.put("completedStudents", completedStudents);
        analytics.put("completionRate", totalStudents > 0 ? (double) completedStudents / totalStudents * 100 : 0);
        analytics.put("averageCourseProgress", totalStudents > 0 ? totalCourseProgress / totalStudents : 0);
        analytics.put("lessonStats", lessonStats);

        return analytics;
    }

    public static Map<String, Object> getInstructorAnalytics(String instructorId) {
        Map<String, Object> analytics = new HashMap<>();
        ArrayList<Course> instructorCourses = CourseService.getCourseByInstructor(instructorId);

        int totalCourses = instructorCourses.size();
        int approvedCourses = 0;
        int totalStudents = 0;
        double totalRevenue = 0; // Placeholder for future revenue tracking

        for (Course course : instructorCourses) {
            if (course.getApprovalStatus() == Course.APPROVED) {
                approvedCourses++;
            }
            totalStudents += course.getStudents().size();
        }

        analytics.put("totalCourses", totalCourses);
        analytics.put("approvedCourses", approvedCourses);
        analytics.put("totalStudents", totalStudents);
        analytics.put("approvalRate", totalCourses > 0 ? (double) approvedCourses / totalCourses * 100 : 0);
        analytics.put("estimatedRevenue", totalRevenue);

        return analytics;
    }

    public static ArrayList<Map<String, Object>> getStudentPerformanceData(String courseId) {
        ArrayList<Map<String, Object>> performanceData = new ArrayList<>();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        Course course = CourseService.getCourseById(courseId);

        if (course == null) {
            return performanceData;
        }

        for (User user : users) {
            if (user instanceof Student) {
                Student student = (Student) user;
                if (student.getEnrolledCourses().contains(courseId)) {
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("studentId", student.getUserId());
                    studentData.put("studentName", student.getUsername());
                    studentData.put("progress", student.getProgress().getOrDefault(courseId, 0));

                    // Calculate average quiz score
                    double totalQuizScore = 0;
                    int quizCount = 0;
                    for (Lesson lesson : course.getLessons()) {
                        LessonProgress lp = student.getLessonProgress().get(lesson.getLessonId());
                        if (lp != null && lp.getQuizScore() != null) {
                            totalQuizScore += lp.getQuizScore();
                            quizCount++;
                        }
                    }
                    studentData.put("averageQuizScore", quizCount > 0 ? totalQuizScore / quizCount : 0);
                    studentData.put("completedQuizzes", quizCount);

                    performanceData.add(studentData);
                }
            }
        }

        return performanceData;
    }
}
