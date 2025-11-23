package Model;

import java.util.ArrayList;

public class Quiz {
    private ArrayList<Question> questions;
    private boolean completed = false;
    private int attempts;
    private float score;

    public Quiz() {
        this.questions = new ArrayList<>();
    }

    public Quiz(ArrayList<Question> questions, float score, int attempts, boolean completed) {
        this.questions = questions;
        this.score = score;
        this.attempts = attempts;
        this.completed = completed;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public boolean isCompleted() {
        return completed;
    }

    public float getScore() {
        return score;
    }

    public int getAttemps() {
        return attempts;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setAttemps(int attempts) {
        this.attempts = attempts;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }
}
