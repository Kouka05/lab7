package Logic;

import Database.JSONDatabaseManager;
import Model.*;

import java.util.ArrayList;

public class AdminService {
    public static ArrayList<Course> getPendingCourses()
    {
        ArrayList<Course> courses = new ArrayList<>();
        for(Course c:JSONDatabaseManager.loadCourses())
        {
            if(c.getApprovalStatus()==c.PENDING)
            courses.add(c);
        }
        return courses;
    }
    public static boolean approveCourse(String courseId)
    {
        ArrayList<Course> courses = getPendingCourses();
        for(Course c :courses)
        {
            if(c.getCourseId().equals(courseId))
            {
                if(!(c.getApprovalStatus()==Course.PENDING))
                    return false;
                c.setApprovalStatus(Course.APPROVED);
                updateCourses(courses);
                return true;
            }
        }
        return false;
    }
    public static boolean rejectCourse(String courseId)
    {
        ArrayList<Course> courses = getPendingCourses();
        for(Course c :courses)
        {
            if(c.getCourseId().equals(courseId))
            {
                if(!(c.getApprovalStatus()==Course.PENDING))
                    return false;
                c.setApprovalStatus(Course.REJECTED);
                updateCourses(courses);
                return true;
            }
        }
        return false;
    }
    public static void updateCourses(ArrayList<Course> courses)
    {
        JSONDatabaseManager.saveCourses(courses);
    }
}
