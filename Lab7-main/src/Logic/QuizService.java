package Logic;

import Database.JSONDatabaseManager;
import Model.Course;
import Model.Lesson;
import Model.Quiz;

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
                        System.out.println("The quiz added successfully");
                        return true;
                    }
                }
            }
        }
        System.out.println("The quiz didn't added successfully");
        return false;
    }

}
