package Model;

public class LessonProgress {

    private boolean completed;   // did the student finish the lesson?
    private Integer quizScore;   // null if not taken

    public LessonProgress() {
        this.completed = false;
        this.quizScore = null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean setCompleted(boolean completed) {
        if(quizScore==null)
            return false;
        else {
            this.completed = completed;
            return true;
        }
    }

    public Integer getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(Integer quizScore) {
        this.quizScore = quizScore;
    }
}
