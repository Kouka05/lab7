package Model;
import java.util.ArrayList;

public class Lesson {
    private String lessonId;
    private String title;
    private String content;
    private ArrayList<String> resources;

    public Lesson() {
        this.resources = new ArrayList<>();
    }

    public Lesson(String lessonId, String title, String content, ArrayList<String> resources) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = resources != null ? resources : new ArrayList<>();
    }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ArrayList<String> getResources() { return resources; }
    public void setResources(ArrayList<String> resources) {
        this.resources = resources != null ? resources : new ArrayList<>();
    }
}