package Model;

public class LessonProgress {
    private boolean completed;
    private Integer quizScore;
    private int attempts;

    public LessonProgress() {
        this.completed = false;
        this.quizScore = null;
        this.attempts = 0;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {

        this.completed = completed;
    }

    public Integer getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(Integer quizScore) {
        this.quizScore = quizScore;
        this.attempts++;
        // Auto-complete if score is passing (>= 50)
        if (quizScore != null && quizScore >= 50) {
            this.completed = true;
        }
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public boolean isQuizPassed() {
        return quizScore != null && quizScore >= 50;
    }
}
