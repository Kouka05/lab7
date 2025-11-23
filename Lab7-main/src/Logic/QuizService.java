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
    public static boolean updateQuiz(String courseId, String lessonId, Quiz updatedQuiz) {
        return addQuizToLesson(courseId, lessonId, updatedQuiz);
    }

    public static Quiz getQuizForLesson(String courseId, String lessonId) {
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

    public static boolean deleteQuiz(String courseId, String lessonId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                for (Lesson l : c.getLessons()) {
                    if (l.getLessonId().equals(lessonId)) {
                        l.setQuiz(new Quiz()); // Reset to empty quiz
                        JSONDatabaseManager.saveCourses(courses);
                        System.out.println("Quiz deleted successfully from lesson: " + lessonId);
                        return true;
                    }
                }
            }
        }
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

        // Calculate score
        int questionNum = quiz.getQuestions().size();
        int rightAnswerCount = 0;
        
        for (int i = 0; i < questionNum; i++) {
            int studentAnswerIndex = answers.get(i);
            Question correctQuestion = quiz.getQuestions().get(i);
            if (studentAnswerIndex == correctQuestion.getCorrectIndex()) {
                rightAnswerCount++;
            }
        }

        float score = 0;
        try {
            score = (rightAnswerCount * 100) / questionNum;
        } catch (ArithmeticException e) {
            System.out.println("There are no questions in the quiz");
            return false;
        }

        // Update quiz attempts and score
        quiz.setScore(score);
        quiz.setCompleted(true);
        quiz.setAttemps(quiz.getAttemps() + 1);

        // Update student's lesson progress with the quiz score
        boolean progressUpdated = StudentService.updateLessonProgress(student, courseId, lessonId, (int) score);

        if (progressUpdated) {
            JSONDatabaseManager.saveCourses(courses);
            JSONDatabaseManager.saveUsers(users);
            System.out.println("Quiz submitted successfully. Score: " + score + "%");
            return true;
        } else {
            System.out.println("Failed to update lesson progress");
            return false;
        }
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
