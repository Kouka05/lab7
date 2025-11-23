package UI;

import Logic.AuthenticationService;
import Logic.CourseService;
import Logic.LessonService;
import Logic.QuizService;
import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import Database.JSONDatabaseManager;

public class InstructorDashboardFrame extends JFrame {
    private JTable coursesTable;
    private JTable studentsTable;
    private JLabel welcomeLabel;
    private Instructor currentInstructor;

    public InstructorDashboardFrame(User user) {
        if (user instanceof Instructor) {
            this.currentInstructor = (Instructor) user;
        } else {
            JOptionPane.showMessageDialog(this, "Error: User is not an instructor", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initializeUI();
        loadInstructorData();
    }

    // ... [Previous code remains the same until viewCourseLessons method] ...

    private void viewCourseLessons(String courseId, String courseName) {
        ArrayList<Lesson> lessons = LessonService.getLessons(courseId);

        if (lessons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No lessons available for this course.", "No Lessons", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog lessonsDialog = new JDialog(this, "Lessons for " + courseName, true);
        lessonsDialog.setLayout(new BorderLayout());
        lessonsDialog.setSize(900, 700);
        lessonsDialog.setLocationRelativeTo(this);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Lesson lesson : lessons) {
            String quizStatus = lesson.getQuiz() != null && !lesson.getQuiz().getQuestions().isEmpty() ? " (Quiz)" : "";
            listModel.addElement(lesson.getTitle() + " (ID: " + lesson.getLessonId() + ")" + quizStatus);
        }

        JList<String> lessonsList = new JList<>(listModel);
        lessonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(lessonsList);

        // Create tabbed pane for lesson content and quiz
        JTabbedPane contentTabs = new JTabbedPane();

        // Lesson Content Tab
        JPanel lessonContentPanel = createLessonContentPanel();
        contentTabs.addTab("Lesson Content", lessonContentPanel);

        // Quiz Management Tab
        JPanel quizPanel = createQuizPanel();
        contentTabs.addTab("Quiz Management", quizPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit Lesson");
        JButton deleteButton = new JButton("Delete Lesson");
        JButton manageQuizButton = new JButton("Manage Quiz");
        JButton closeButton = new JButton("Close");

        // Components for lesson content
        JTextField titleField = (JTextField) ((JPanel) ((JPanel) lessonContentPanel.getComponent(0)).getComponent(1)).getComponent(0);
        JTextArea contentArea = (JTextArea) ((JScrollPane) ((JPanel) ((JPanel) lessonContentPanel.getComponent(1)).getComponent(1)).getComponent(0)).getViewport().getView();
        JTextArea resourcesArea = (JTextArea) ((JScrollPane) ((JPanel) ((JPanel) lessonContentPanel.getComponent(2)).getComponent(1)).getComponent(0)).getViewport().getView();

        // Components for quiz management
        JTable quizTable = (JTable) ((JScrollPane) ((JPanel) quizPanel.getComponent(0)).getComponent(0)).getViewport().getView();
        DefaultTableModel quizTableModel = (DefaultTableModel) quizTable.getModel();

        lessonsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = lessonsList.getSelectedIndex();
                if (index >= 0) {
                    Lesson selectedLesson = lessons.get(index);
                    
                    // Update lesson content tab
                    titleField.setText(selectedLesson.getTitle());
                    contentArea.setText(selectedLesson.getContent());

                    StringBuilder resourcesText = new StringBuilder();
                    for (String resource : selectedLesson.getResources()) {
                        resourcesText.append(resource).append("\n");
                    }
                    resourcesArea.setText(resourcesText.toString());

                    // Update quiz management tab
                    refreshQuizTable(quizTableModel, selectedLesson);
                }
            }
        });

        editButton.addActionListener(e -> {
            int index = lessonsList.getSelectedIndex();
            if (index >= 0) {
                Lesson selectedLesson = lessons.get(index);
                String newTitle = titleField.getText().trim();
                String newContent = contentArea.getText().trim();

                ArrayList<String> newResources = new ArrayList<>();
                String[] resourcesArray = resourcesArea.getText().split("\n");
                for (String resource : resourcesArray) {
                    if (!resource.trim().isEmpty()) {
                        newResources.add(resource.trim());
                    }
                }

                if (newTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(lessonsDialog, "Lesson title cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success = LessonService.editLesson(courseId, selectedLesson.getLessonId(), newTitle, newContent, newResources);
                if (success) {
                    JOptionPane.showMessageDialog(lessonsDialog, "Lesson updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    listModel.set(index, newTitle + " (ID: " + selectedLesson.getLessonId() + ")");
                    lessons.get(index).setTitle(newTitle);
                } else {
                    JOptionPane.showMessageDialog(lessonsDialog, "Failed to update lesson", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(lessonsDialog, "Please select a lesson to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        manageQuizButton.addActionListener(e -> {
            int index = lessonsList.getSelectedIndex();
            if (index >= 0) {
                Lesson selectedLesson = lessons.get(index);
                manageQuizForLesson(courseId, selectedLesson, lessonsDialog);
            } else {
                JOptionPane.showMessageDialog(lessonsDialog, "Please select a lesson first", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int index = lessonsList.getSelectedIndex();
            if (index >= 0) {
                Lesson selectedLesson = lessons.get(index);
                int confirm = JOptionPane.showConfirmDialog(lessonsDialog,
                        "Are you sure you want to delete '" + selectedLesson.getTitle() + "'?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = LessonService.deleteLesson(courseId, selectedLesson.getLessonId());
                    if (success) {
                        JOptionPane.showMessageDialog(lessonsDialog, "Lesson deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        lessons.remove(index);
                        listModel.remove(index);

                        // Clear fields
                        if (!lessons.isEmpty()) {
                            lessonsList.setSelectedIndex(0);
                        } else {
                            titleField.setText("");
                            contentArea.setText("");
                            resourcesArea.setText("");
                            quizTableModel.setRowCount(0);
                        }
                    } else {
                        JOptionPane.showMessageDialog(lessonsDialog, "Failed to delete lesson", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(lessonsDialog, "Please select a lesson to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> lessonsDialog.dispose());

        buttonPanel.add(editButton);
        buttonPanel.add(manageQuizButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        if (!lessons.isEmpty()) {
            lessonsList.setSelectedIndex(0);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, contentTabs);
        splitPane.setDividerLocation(250);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        lessonsDialog.add(mainPanel);
        lessonsDialog.setVisible(true);
    }

    private JPanel createLessonContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
        JTextField titleField = new JTextField();
        titlePanel.add(titleField, BorderLayout.CENTER);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.add(new JLabel("Lesson Content:"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(8, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentPanel.add(contentScroll, BorderLayout.CENTER);

        // Resources Panel
        JPanel resourcesPanel = new JPanel(new BorderLayout(5, 5));
        resourcesPanel.add(new JLabel("Resources (one per line):"), BorderLayout.NORTH);
        JTextArea resourcesArea = new JTextArea(3, 30);
        resourcesArea.setLineWrap(true);
        JScrollPane resourcesScroll = new JScrollPane(resourcesArea);
        resourcesPanel.add(resourcesScroll, BorderLayout.CENTER);

        panel.add(titlePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(contentPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(resourcesPanel);

        return panel;
    }

    private JPanel createQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Quiz table
        String[] columns = {"Question", "Options", "Correct Answer"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable quizTable = new JTable(model);
        JScrollPane tableScroll = new JScrollPane(quizTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addQuestionButton = new JButton("Add Question");
        JButton editQuestionButton = new JButton("Edit Question");
        JButton deleteQuestionButton = new JButton("Delete Question");

        styleActionButton(addQuestionButton);
        styleActionButton(editQuestionButton);
        styleActionButton(deleteQuestionButton);

        addQuestionButton.addActionListener(e -> {
            // This will be handled in the manageQuizForLesson method
            JOptionPane.showMessageDialog(this, "Please use 'Manage Quiz' button to add questions", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        editQuestionButton.addActionListener(e -> {
            // This will be handled in the manageQuizForLesson method
            JOptionPane.showMessageDialog(this, "Please use 'Manage Quiz' button to edit questions", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        deleteQuestionButton.addActionListener(e -> {
            int selectedRow = quizTable.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this question?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    model.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a question first", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(addQuestionButton);
        buttonPanel.add(editQuestionButton);
        buttonPanel.add(deleteQuestionButton);

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshQuizTable(DefaultTableModel model, Lesson lesson) {
        model.setRowCount(0);

        Quiz quiz = lesson.getQuiz();
        if (quiz != null && quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                String options = String.join(" | ", question.getOption());
                String correctAnswer = question.getOption().get(question.getCorrectIndex());
                model.addRow(new Object[]{
                    question.getQuestion(),
                    options,
                    correctAnswer
                });
            }
        }
    }

    private void manageQuizForLesson(String courseId, Lesson lesson, JDialog parentDialog) {
        JDialog quizDialog = new JDialog(parentDialog, "Manage Quiz - " + lesson.getTitle(), true);
        quizDialog.setSize(600, 500);
        quizDialog.setLocationRelativeTo(parentDialog);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Questions table
        String[] columns = {"Question", "Options", "Correct Answer"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable questionsTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(questionsTable);

        // Load existing questions
        Quiz currentQuiz = lesson.getQuiz();
        if (currentQuiz != null && currentQuiz.getQuestions() != null) {
            for (Question question : currentQuiz.getQuestions()) {
                String options = String.join(" | ", question.getOption());
                String correctAnswer = question.getOption().get(question.getCorrectIndex());
                tableModel.addRow(new Object[]{
                    question.getQuestion(),
                    options,
                    correctAnswer
                });
            }
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Question");
        JButton editButton = new JButton("Edit Question");
        JButton deleteButton = new JButton("Delete Question");
        JButton saveButton = new JButton("Save Quiz");
        JButton cancelButton = new JButton("Cancel");

        styleActionButton(addButton);
        styleActionButton(editButton);
        styleActionButton(deleteButton);
        styleActionButton(saveButton);
        styleActionButton(cancelButton);

        addButton.addActionListener(e -> showQuestionDialog(null, tableModel, questionsTable));
        editButton.addActionListener(e -> {
            int selectedRow = questionsTable.getSelectedRow();
            if (selectedRow >= 0) {
                // For simplicity, we'll just remove and re-add
                // In a real application, you'd want to edit in place
                tableModel.removeRow(selectedRow);
                showQuestionDialog(null, tableModel, questionsTable);
            } else {
                JOptionPane.showMessageDialog(quizDialog, "Please select a question to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = questionsTable.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(quizDialog,
                        "Are you sure you want to delete this question?",
                        "Confirm Deletion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selectedRow);
                }
            } else {
                JOptionPane.showMessageDialog(quizDialog, "Please select a question first", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        saveButton.addActionListener(e -> {
            saveQuizToLesson(courseId, lesson, tableModel, quizDialog);
        });
        cancelButton.addActionListener(e -> quizDialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(new JLabel("Quiz Questions for: " + lesson.getTitle()), BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        quizDialog.add(mainPanel);
        quizDialog.setVisible(true);
    }

    private void showQuestionDialog(Question existingQuestion, DefaultTableModel tableModel, JTable questionsTable) {
        JDialog questionDialog = new JDialog(this, "Add/Edit Question", true);
        questionDialog.setSize(500, 400);
        questionDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Question field
        JPanel questionPanel = new JPanel(new BorderLayout(5, 5));
        questionPanel.add(new JLabel("Question:"), BorderLayout.NORTH);
        JTextArea questionArea = new JTextArea(3, 30);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        if (existingQuestion != null) {
            questionArea.setText(existingQuestion.getQuestion());
        }
        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionPanel.add(questionScroll, BorderLayout.CENTER);

        // Options fields
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        JTextField option1Field = new JTextField();
        JTextField option2Field = new JTextField();
        JTextField option3Field = new JTextField();
        JTextField option4Field = new JTextField();

        if (existingQuestion != null && existingQuestion.getOption().size() >= 4) {
            option1Field.setText(existingQuestion.getOption().get(0));
            option2Field.setText(existingQuestion.getOption().get(1));
            option3Field.setText(existingQuestion.getOption().get(2));
            option4Field.setText(existingQuestion.getOption().get(3));
        }

        optionsPanel.add(createOptionField("Option 1:", option1Field));
        optionsPanel.add(createOptionField("Option 2:", option2Field));
        optionsPanel.add(createOptionField("Option 3:", option3Field));
        optionsPanel.add(createOptionField("Option 4:", option4Field));

        // Correct answer selection
        JPanel correctAnswerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        correctAnswerPanel.add(new JLabel("Correct Answer:"));
        JComboBox<String> correctAnswerCombo = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
        if (existingQuestion != null) {
            correctAnswerCombo.setSelectedIndex(existingQuestion.getCorrectIndex());
        }
        correctAnswerPanel.add(correctAnswerCombo);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Question");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String questionText = questionArea.getText().trim();
            String option1 = option1Field.getText().trim();
            String option2 = option2Field.getText().trim();
            String option3 = option3Field.getText().trim();
            String option4 = option4Field.getText().trim();

            if (questionText.isEmpty() || option1.isEmpty() || option2.isEmpty()) {
                JOptionPane.showMessageDialog(questionDialog, "Please fill in at least the question and first two options", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ArrayList<String> options = new ArrayList<>();
            options.add(option1);
            options.add(option2);
            if (!option3.isEmpty()) options.add(option3);
            if (!option4.isEmpty()) options.add(option4);

            int correctIndex = correctAnswerCombo.getSelectedIndex();

            // Add to table
            String optionsDisplay = String.join(" | ", options);
            String correctAnswerDisplay = options.get(correctIndex);
            tableModel.addRow(new Object[]{questionText, optionsDisplay, correctAnswerDisplay});

            questionDialog.dispose();
        });

        cancelButton.addActionListener(e -> questionDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(questionPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(correctAnswerPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(buttonPanel);

        questionDialog.add(mainPanel);
        questionDialog.setVisible(true);
    }

    private JPanel createOptionField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void saveQuizToLesson(String courseId, Lesson lesson, DefaultTableModel tableModel, JDialog quizDialog) {
        // Create new quiz object
        Quiz quiz = new Quiz();
        
        // Convert table rows to questions
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String questionText = (String) tableModel.getValueAt(i, 0);
            String optionsDisplay = (String) tableModel.getValueAt(i, 1);
            String correctAnswerDisplay = (String) tableModel.getValueAt(i, 2);
            
            // Parse options
            String[] optionsArray = optionsDisplay.split(" \\| ");
            ArrayList<String> options = new ArrayList<>();
            for (String option : optionsArray) {
                options.add(option.trim());
            }
            
            // Find correct index
            int correctIndex = -1;
            for (int j = 0; j < options.size(); j++) {
                if (options.get(j).equals(correctAnswerDisplay)) {
                    correctIndex = j;
                    break;
                }
            }
            
            if (correctIndex == -1) {
                JOptionPane.showMessageDialog(quizDialog, "Error: Could not find correct answer in options", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Question question = new Question(questionText, options, correctIndex);
            quiz.addQuestion(question);
        }
        
        // Save quiz to lesson
        boolean success = QuizService.addQuizToLesson(courseId, lesson.getLessonId(), quiz);
        if (success) {
            JOptionPane.showMessageDialog(quizDialog, "Quiz saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            quizDialog.dispose();
        } else {
            JOptionPane.showMessageDialog(quizDialog, "Failed to save quiz", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ... [Rest of the previous code remains the same] ...
}
