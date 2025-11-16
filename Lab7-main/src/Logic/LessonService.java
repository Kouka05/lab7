package Logic;

import Database.JSONDatabaseManager;
import Model.Course;
import Model.Lesson;
import java.util.ArrayList;

public class LessonService {

    public static boolean addLesson(String courseId, Lesson lesson) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                if (lesson.getLessonId() == null || lesson.getLessonId().isEmpty()) {
                    String lessonId = generateNextLessonId(c.getLessons());
                    lesson.setLessonId(lessonId);
                }

                c.addLesson(lesson);
                JSONDatabaseManager.saveCourses(courses);
                System.out.println("Lesson added: " + lesson.getTitle() + " to course " + courseId);
                return true;
            }
        }
        System.out.println("Course not found: " + courseId);
        return false;
    }

    private static String generateNextLessonId(ArrayList<Lesson> lessons) {
        if (lessons.isEmpty()) {
            return "L1";
        }
        int maxId = 0;
        for (Lesson lesson : lessons) {
            try {
                String idStr = lesson.getLessonId().substring(1); // Remove "L" prefix
                int idNum = Integer.parseInt(idStr);
                if (idNum > maxId) {
                    maxId = idNum;
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid lesson ID format: " + lesson.getLessonId());
            }
        }

        return "L" + (maxId + 1);
    }

    public static boolean editLesson(String courseId, String lessonId, String newTitle, String newContent, ArrayList<String> newResources) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                for (Lesson lesson : c.getLessons()) {
                    if (lesson.getLessonId().equals(lessonId)) {
                        if (newTitle != null && !newTitle.isEmpty()) {
                            lesson.setTitle(newTitle);
                        }
                        if (newContent != null) {
                            lesson.setContent(newContent);
                        }
                        if (newResources != null) {
                            lesson.setResources(newResources);
                        }
                        JSONDatabaseManager.saveCourses(courses);
                        System.out.println("Lesson updated: " + lessonId);
                        return true;
                    }
                }
            }
        }
        System.out.println("Lesson not found: " + lessonId);
        return false;
    }

    public static boolean deleteLesson(String courseId, String lessonId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.deleteLesson(lessonId);
                JSONDatabaseManager.saveCourses(courses);
                System.out.println("Lesson deleted: " + lessonId);
                return true;
            }
        }
        System.out.println("Course not found: " + courseId);
        return false;
    }

    public static ArrayList<Lesson> getLessons(String courseId) {
        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                return c.getLessons();
            }
        }
        System.out.println("Course not found: " + courseId);
        return new ArrayList<>();
    }
}