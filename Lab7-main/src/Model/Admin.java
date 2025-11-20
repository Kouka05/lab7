package Model;
import Database.JSONDatabaseManager;

import java.util.ArrayList;

public class Admin extends User{
    public Admin(String userId, String username, String email, String passwordHash, int role) {
        super(userId, username, email, passwordHash, role);
    }
    public String reviewCourse(String CourseId)
    {
        for(Course c: JSONDatabaseManager.loadCourses())
        {
            if(c.getCourseId().equals(CourseId))
                return c.getApprovalStatusString();
        }
        return null;
    }
}
