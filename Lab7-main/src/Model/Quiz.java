package Model;

import java.util.ArrayList;

public class Quiz {
private ArrayList<Question> questions;
    private boolean completed=false;
    private int attemps;
    private int score;
public Quiz(){
    this.questions=new ArrayList<>();
}

    public Quiz(ArrayList<Question> questions, int score, int attemps, boolean completed) {
        this.questions = questions;
        this.score = score;
        this.attemps = attemps;
        this.completed = completed;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getScore() {
        return score;
    }

    public int getAttemps() {
        return attemps;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setAttemps(int attemps) {
        this.attemps = attemps;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public void addQuestion(Question question) {
        this.questions.add(question);
    }
}
