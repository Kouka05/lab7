package Model;
import java.util.ArrayList;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private int approvalStatus = PENDING;
    public static final int PENDING = 1;
    public static final int APPROVED = 2;
    public static final int REJECTED = 3;
    private ArrayList<Lesson> lessons;
    private ArrayList<String> students;
    public Course() {
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }
    public Course(String courseId, String title, String description, String instructorId) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public ArrayList<Lesson> getLessons() { return lessons; }
    public void setLessons(ArrayList<Lesson> lessons) {
        this.lessons = lessons != null ? lessons : new ArrayList<>();
    }

    public ArrayList<String> getStudents() { return students; }
    public void setStudents(ArrayList<String> students) {
        this.students = students != null ? students : new ArrayList<>();
    }

    public int getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(int approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    public String getApprovalStatusString()
    {
        switch(this.approvalStatus)
        {
            case PENDING: return "Pending";
            case APPROVED: return "Approved";
            case REJECTED: return "Rejected";
            default: return "Unknown";
        }
    }
    public void addLesson(Lesson lesson) {
        if (lesson != null && !lessons.contains(lesson)) {
            lessons.add(lesson);
        }
    }
    public void editLesson(String lessonId, Lesson updatedLesson) {
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getLessonId().equals(lessonId)) {
                lessons.set(i, updatedLesson);
                break;
            }
        }
    }
    public void deleteLesson(String lessonId) {
        lessons.removeIf(lesson -> lesson.getLessonId().equals(lessonId));
    }
    public void addStudent(String studentId) {
        if (studentId != null && !students.contains(studentId)) {
            students.add(studentId);
        }
    }
    public void removeStudent(String studentId) {
        students.remove(studentId);
    }
}