package Model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Student extends User {
    private ArrayList<String> enrolledCourses;
    private Map<String, Integer> progress;
    private Map<String, LessonProgress> lessonProgress;
    private ArrayList<Certificate> certificates;

    public Student() {
        super();
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
        this.lessonProgress = new HashMap<>();
        this.certificates = new ArrayList<>();
    }

    public Student(String userId, String username, String email, String passwordHash, int role) {
        super(userId, username, email, passwordHash, role);
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
        this.lessonProgress = new HashMap<>();
        this.certificates = new ArrayList<>();
    }

    public ArrayList<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(ArrayList<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses != null ? enrolledCourses : new ArrayList<>();
    }

    public Map<String, Integer> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, Integer> progress) {
        this.progress = progress != null ? progress : new HashMap<>();
    }

    public Map<String, LessonProgress> getLessonProgress() {
        return lessonProgress;
    }

    public void setLessonProgress(Map<String, LessonProgress> lessonProgress) {
        this.lessonProgress = lessonProgress != null ? lessonProgress : new HashMap<>();
    }

    public ArrayList<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(ArrayList<Certificate> certificates) {
        this.certificates = certificates != null ? certificates : new ArrayList<>();
    }

    public void enrollCourse(String courseId) {
        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
            progress.put(courseId, 0);
        }
    }

    public void dropCourse(String courseId) {
        enrolledCourses.remove(courseId);
        progress.remove(courseId);
    }

    public void updateProgress(String courseId, int value) {
        if (enrolledCourses.contains(courseId)) {
            progress.put(courseId, Math.min(100, Math.max(0, value)));
        }
    }

    public void addCertificate(Certificate certificate) {
        if (certificate != null && !certificates.contains(certificate)) {
            certificates.add(certificate);
        }
    }
}
