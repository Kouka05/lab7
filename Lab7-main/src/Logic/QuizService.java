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
                        System.out.println("The quiz added successfully");
                        return true;
                    }
                }
            }
        }
        System.out.println("The quiz didn't added successfully");
        return false;
    }
    public static boolean submitQuiz(String studentId, String courseId, String lessonId, ArrayList<Integer> answers){
        ArrayList<Course> courses=JSONDatabaseManager.loadCourses();
        ArrayList<User> users=JSONDatabaseManager.loadUsers();
        Student student = null;
        Course course = null;
        Lesson lesson = null;
        for (User u: users){
            if (u.isStudent()&&u.getUserId().equals(studentId)){
                student = (Student) u;
                break;
            }
        }
        if (student==null){
            System.out.println("student of id:"+ studentId +"was not found");
            return false;
        }
for (Course c: courses){
    if (c.getCourseId().equals(courseId)){
        course = c;
        break;
    }
}
if (course==null){
    System.out.println("course with id" + courseId+ "was not found");
    return false;
}
for (Lesson l : course.getLessons()){
    if (l.getLessonId().equals(lessonId)){
        lesson = l;
        break;
    }
}
if (lesson==null){
    System.out.println("lesson with id:"+ lessonId + "was not found");
    return false;
}
Quiz correctquiz = lesson.getQuiz();
if (correctquiz==null||correctquiz.getQuestions().isEmpty()){
    System.out.println("no quiz found for this lesson");
    return false;
}
int questionNum=correctquiz.getQuestions().size();
int rightAnswerCount = 0;
for (int i =0;i<questionNum;i++){
    int studentAnswerIndex= answers.get(i);
    Question correctQuestion = correctquiz.getQuestions().get(i);
    if (studentAnswerIndex == correctQuestion.getCorrectIndex()){
        rightAnswerCount++;
    }
}
float score=0;
try {
    score = (rightAnswerCount * 100) / questionNum;
    correctquiz.setScore(score);
    correctquiz.setCompleted(true);
    correctquiz.setAttemps(correctquiz.getAttemps()+1);
}catch (ArithmeticException e){
    System.out.println("there is no question to begain with");
    return false;
}
int totalLesson = course.getLessons().size();
int currentProgress = student.getProgress().getOrDefault(courseId,0);
int progressPerLesson = 100/totalLesson;
if (score>=50){
    int newProgress = Math.min(100,currentProgress+progressPerLesson);
    student.updateProgress(courseId,newProgress);
}
JSONDatabaseManager.saveCourses(courses);
JSONDatabaseManager.saveUsers(users);

 return true;   }
}
