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
            // Initialize progress to 0, not completed
            progress.put(courseId, 0);

            // Don't initialize any lesson progress - it should be empty
            // Lesson progress will be created when students actually take quizzes
        }
    }

    public void dropCourse(String courseId) {
        enrolledCourses.remove(courseId);
        progress.remove(courseId);

        // Remove all lesson progress entries for this course
        // Since lesson progress keys are now stored as courseId+lessonId,
        // we need to remove all entries that start with the courseId
        if (lessonProgress != null) {
            lessonProgress.entrySet().removeIf(entry ->
                    entry.getKey().startsWith(courseId)
            );
        }
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

    /**
     * Helper method to get lesson progress using the composite key format
     * @param courseId the course ID
     * @param lessonId the lesson ID
     * @return the LessonProgress object or null if not found
     */
    public LessonProgress getLessonProgress(String courseId, String lessonId) {
        if (lessonProgress == null) {
            return null;
        }
        String progressKey = courseId + lessonId;
        return lessonProgress.get(progressKey);
    }

    /**
     * Helper method to update lesson progress using the composite key format
     * @param courseId the course ID
     * @param lessonId the lesson ID
     * @param lessonProgressObj the LessonProgress object to store
     */
    public void updateLessonProgress(String courseId, String lessonId, LessonProgress lessonProgressObj) {
        if (lessonProgress == null) {
            lessonProgress = new HashMap<>();
        }
        String progressKey = courseId + lessonId;
        lessonProgress.put(progressKey, lessonProgressObj);
    }

    /**
     * Count completed lessons for a specific course
     * @param courseId the course ID
     * @return number of completed lessons
     */
    public int countCompletedLessonsInCourse(String courseId) {
        if (lessonProgress == null) {
            return 0;
        }

        int completed = 0;
        for (Map.Entry<String, LessonProgress> entry : lessonProgress.entrySet()) {
            // Check if the key starts with the courseId and the lesson is completed
            if (entry.getKey().startsWith(courseId) &&
                    entry.getValue() != null &&
                    entry.getValue().isCompleted()) {
                completed++;
            }
        }
        return completed;
    }
}