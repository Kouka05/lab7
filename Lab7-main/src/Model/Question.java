package Model;

import java.util.ArrayList;

public class Question {
    private String question;
    private ArrayList<String> option;
    private int correctIndex;

    public Question(String question, ArrayList<String> option, int correctIndex) {
        this.question = question;
        this.option = option;
        this.correctIndex = correctIndex;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getOption() {
        return option;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOption(ArrayList<String> option) {
        this.option = option;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }
}

