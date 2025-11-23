package Model;

import java.util.ArrayList;

public class Question {
    private String question;
    private ArrayList<String> options;
    private int correctIndex;

    // Default constructor for JSON serialization
    public Question() {
        this.options = new ArrayList<>();
    }

    public Question(String question, ArrayList<String> options, int correctIndex) {
        this.question = question;
        this.options = options != null ? options : new ArrayList<>();
        this.correctIndex = correctIndex;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getOption() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOption(ArrayList<String> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }
}
