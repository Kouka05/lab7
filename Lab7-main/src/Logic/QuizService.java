package Logic;

import Database.JSONDatabaseManager;
import Model.*;

import java.util.ArrayList;

public class QuizService {

    public static boolean addQuizToLesson(String courseId, String lessonId, Quiz quiz) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                for (Lesson l : c.getLessons()) {
                    if (l.getLessonId().equals(lessonId)) {
                        l.setQuiz(quiz);
                        JSONDatabaseManager.saveCourses(courses);
                        System.out.println("Quiz added successfully to lesson: " + lessonId);
                        return true;
                    }
                }
            }
        }
        System.out.println("Failed to add quiz to lesson: " + lessonId);
        return false;
    }

    public static boolean submitQuiz(String studentId, String courseId, String lessonId, ArrayList<Integer> answers) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();

        Student student = null;
        Course course = null;
        Lesson lesson = null;

        // Find student
        for (User u : users) {
            if (u instanceof Student && u.getUserId().equals(studentId)) {
                student = (Student) u;
                break;
            }
        }
        if (student == null) {
            System.out.println("Student with id: " + studentId + " was not found");
            return false;
        }

        // Verify student is enrolled in the course
        if (!student.getEnrolledCourses().contains(courseId)) {
            System.out.println("Student " + studentId + " is not enrolled in course " + courseId);
            return false;
        }

        // Find course
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                course = c;
                break;
            }
        }
        if (course == null) {
            System.out.println("Course with id: " + courseId + " was not found");
            return false;
        }

        // Find lesson
        for (Lesson l : course.getLessons()) {
            if (l.getLessonId().equals(lessonId)) {
                lesson = l;
                break;
            }
        }
        if (lesson == null) {
            System.out.println("Lesson with id: " + lessonId + " was not found");
            return false;
        }

        Quiz quiz = lesson.getQuiz();
        if (quiz == null || quiz.getQuestions().isEmpty()) {
            System.out.println("No quiz found for this lesson");
            return false;
        }

        // Validate that answers size matches questions size
        if (answers.size() != quiz.getQuestions().size()) {
            System.out.println("Number of answers (" + answers.size() + ") doesn't match number of questions (" + quiz.getQuestions().size() + ")");
            return false;
        }

        // Calculate score
        int questionNum = quiz.getQuestions().size();
        int rightAnswerCount = 0;

        for (int i = 0; i < questionNum; i++) {
            int studentAnswerIndex = answers.get(i);
            Question correctQuestion = quiz.getQuestions().get(i);

            // Validate answer index is within bounds
            if (studentAnswerIndex >= 0 && studentAnswerIndex < correctQuestion.getOption().size()) {
                if (studentAnswerIndex == correctQuestion.getCorrectIndex()) {
                    rightAnswerCount++;
                }
            } else {
                System.out.println("Invalid answer index for question " + i + ": " + studentAnswerIndex);
            }
        }

        float score = 0;
        try {
            score = (float) (rightAnswerCount * 100) / questionNum;
            System.out.println("Quiz score calculated: " + rightAnswerCount + "/" + questionNum + " = " + score + "%");
        } catch (ArithmeticException e) {
            System.out.println("There are no questions in the quiz");
            return false;
        }

        // Update quiz attempts and score in the course object
        quiz.setScore(score);
        quiz.setCompleted(true);
        quiz.setAttemps(quiz.getAttemps() + 1);

        // Update student's lesson progress
        boolean progressUpdated = updateStudentLessonProgress(student, courseId, lessonId, (int) score, users);

        if (progressUpdated) {
            // Save both courses (with updated quiz) and users (with updated progress)
            JSONDatabaseManager.saveCourses(courses);
            JSONDatabaseManager.saveUsers(users);
            System.out.println("Quiz submitted successfully. Score: " + score + "%");
            return true;
        } else {
            System.out.println("Failed to update lesson progress");
            return false;
        }
    }

    private static boolean updateStudentLessonProgress(Student student, String courseId, String lessonId, int quizScore, ArrayList<User> users) {
        try {
            // Get or create lesson progress
            LessonProgress lessonProgress = student.getLessonProgress()
                    .computeIfAbsent(courseId+lessonId, k -> new LessonProgress());

            // Update the quiz score - this automatically handles completion if score >= 50
            lessonProgress.setQuizScore(quizScore);

            // Update course progress based on completed lessons
            Course course = CourseService.getCourseById(courseId);
            if (course != null) {
                int totalLessons = course.getLessons().size();
                if (totalLessons > 0) {
                    int completedLessons = countCompletedLessons(student, course);
                    int newProgress = (completedLessons * 100) / totalLessons;
                    student.updateProgress(courseId, newProgress);
                    
                    // Check if course is completed for certificate
                    if (newProgress == 100) {
                        generateCertificate(student, courseId);
                    }
                }
            }

            System.out.println("Lesson progress updated for student " + student.getUserId() + 
                             " - Score: " + quizScore + "%, Attempts: " + lessonProgress.getAttempts() + 
                             ", Completed: " + lessonProgress.isCompleted());
            return true;
        } catch (Exception e) {
            System.out.println("Error updating student lesson progress: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

    public static Quiz getQuizResults(String studentId, String courseId, String lessonId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                for (Lesson l : c.getLessons()) {
                    if (l.getLessonId().equals(lessonId)) {
                        return l.getQuiz();
                    }
                }
            }
        }
        return null;
    }
}
