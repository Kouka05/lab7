package UI;

import Database.JSONDatabaseManager;
import Logic.AuthenticationService;
import Logic.CourseService;
import Logic.LessonService;
import Logic.StudentService;
import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import Logic.*;
public class StudentDashboardFrame extends JFrame {
    private JTable coursesTable;
    private JTable availableCoursesTable;
    private JProgressBar overallProgressBar;
    private JLabel welcomeLabel;
    private Student currentStudent;

    public StudentDashboardFrame(User user) {
        if (user instanceof Student) {
            this.currentStudent = (Student) user;
        } else {
            JOptionPane.showMessageDialog(this, "Error: User is not a student", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initializeUI();
        loadStudentData();
    }

    private void initializeUI() {
        setTitle("E-Learning System - Student Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Courses", createCoursesPanel());
        tabbedPane.addTab("Available Courses", createAvailableCoursesPanel());
        tabbedPane.addTab("Progress", createProgressPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }
    private void refreshCurrentStudent() {
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        for (User user : users) {
            if (user.getUserId().equals(currentStudent.getUserId()) && user instanceof Student) {
                currentStudent = (Student) user;
                AuthenticationService.updateLoggedInUser(currentStudent);
                break;
            }
        }
    }
    private void loadStudentData() {
        refreshCurrentStudent();

        welcomeLabel.setText("Welcome, " + currentStudent.getUsername() + "!");

        refreshEnrolledCoursesTable();
        refreshAvailableCoursesTable();
        refreshProgress();
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        welcomeLabel = new JLabel("Welcome, Student!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        styleHeaderButton(logoutButton);
        logoutButton.addActionListener(e -> {
            AuthenticationService.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("My Enrolled Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Course ID", "Course Name", "Instructor", "Progress", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(model);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        coursesTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton viewLessonsButton = new JButton("View Lessons");
        JButton markCompleteButton = new JButton("Mark Lesson Complete");
        JButton unenrollButton = new JButton("Unenroll");
        JButton refreshButton = new JButton("Refresh");

        styleActionButton(viewLessonsButton);
        styleActionButton(markCompleteButton);
        styleActionButton(unenrollButton);
        styleActionButton(refreshButton);

        viewLessonsButton.addActionListener(e -> viewCourseLessons());
        markCompleteButton.addActionListener(e -> markLessonComplete());
        unenrollButton.addActionListener(e -> unenrollFromCourse());
        refreshButton.addActionListener(e -> refreshEnrolledCoursesTable());

        buttonPanel.add(viewLessonsButton);
        buttonPanel.add(markCompleteButton);
        buttonPanel.add(unenrollButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAvailableCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Available Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Course ID", "Course Name", "Instructor", "Description", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        availableCoursesTable = new JTable(model);
        availableCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(availableCoursesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton enrollButton = new JButton("Enroll in Selected Course");
        JButton refreshButton = new JButton("Refresh");

        styleActionButton(enrollButton);
        styleActionButton(refreshButton);

        enrollButton.addActionListener(e -> enrollInSelectedCourse());
        refreshButton.addActionListener(e -> refreshAvailableCoursesTable());

        buttonPanel.add(enrollButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel overallProgressPanel = new JPanel(new BorderLayout());
        overallProgressPanel.setBackground(Color.WHITE);
        overallProgressPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel progressLabel = new JLabel("Overall Learning Progress");
        progressLabel.setFont(new Font("Arial", Font.BOLD, 16));

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setValue(0);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setFont(new Font("Arial", Font.BOLD, 12));
        overallProgressBar.setForeground(new Color(0, 150, 0));

        overallProgressPanel.add(progressLabel, BorderLayout.NORTH);
        overallProgressPanel.add(overallProgressBar, BorderLayout.CENTER);

        panel.add(overallProgressPanel, BorderLayout.NORTH);

        return panel;
    }

    private void refreshEnrolledCoursesTable() {
        DefaultTableModel model = (DefaultTableModel) coursesTable.getModel();
        model.setRowCount(0);

        ArrayList<Course> enrolledCourses = StudentService.getEnrolledCourses(currentStudent.getUserId());

        for (Course course : enrolledCourses) {
            String instructorName = "Unknown Instructor";
            ArrayList<User> users = JSONDatabaseManager.loadUsers();
            for (User user : users) {
                if (user.getUserId().equals(course.getInstructorId())) {
                    instructorName = user.getUsername();
                    break;
                }
            }

            int progress = currentStudent.getProgress().getOrDefault(course.getCourseId(), 0);

            model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    instructorName,
                    progress + "%",
                    "View"
            });
        }
    }

    private void refreshAvailableCoursesTable() {
        DefaultTableModel model = (DefaultTableModel) availableCoursesTable.getModel();
        model.setRowCount(0);

        ArrayList<Course> allCourses = CourseService.getApprovedCourses();
        ArrayList<String> enrolledCourses = currentStudent.getEnrolledCourses();

        for (Course course : allCourses) {
            if (!enrolledCourses.contains(course.getCourseId())) {
                String instructorName = "Unknown Instructor";
                ArrayList<User> users = JSONDatabaseManager.loadUsers();
                for (User user : users) {
                    if (user.getUserId().equals(course.getInstructorId())) {
                        instructorName = user.getUsername();
                        break;
                    }
                }

                model.addRow(new Object[]{
                        course.getCourseId(),
                        course.getTitle(),
                        instructorName,
                        course.getDescription(),
                        "Enroll"
                });
            }
        }
    }

    private void refreshProgress() {
        int totalProgress = 0;
        int courseCount = currentStudent.getEnrolledCourses().size();

        for (int progress : currentStudent.getProgress().values()) {
            totalProgress += progress;
        }

        int averageProgress = courseCount > 0 ? totalProgress / courseCount : 0;
        overallProgressBar.setValue(averageProgress);
    }

    private void enrollInSelectedCourse() {
        int selectedRow = availableCoursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) availableCoursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) availableCoursesTable.getValueAt(selectedRow, 1);

            try {
                boolean success = StudentService.enroll(currentStudent.getUserId(), courseId);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Successfully enrolled in: " + courseName, "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadStudentData();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to enroll in course", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error enrolling in course: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a course first", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void unenrollFromCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to unenroll from " + courseName + "?",
                    "Confirm Unenrollment", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = StudentService.unenroll(currentStudent.getUserId(), courseId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Successfully unenrolled from: " + courseName, "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadStudentData();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to unenroll from course", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a course first", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewCourseLessons() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

            ArrayList<Lesson> lessons = LessonService.getLessons(courseId);

            if (lessons.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No lessons available for this course yet.", "No Lessons", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog lessonsDialog = new JDialog(this, "Lessons for " + courseName, true);
            lessonsDialog.setLayout(new BorderLayout());
            lessonsDialog.setSize(800, 600);
            lessonsDialog.setLocationRelativeTo(this);

            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (Lesson lesson : lessons) {
                listModel.addElement(lesson.getTitle());
            }

            JList<String> lessonsList = new JList<>(listModel);
            lessonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane listScroll = new JScrollPane(lessonsList);

            JPanel contentPanel = new JPanel(new BorderLayout());

            JPanel contentAreaPanel = new JPanel(new BorderLayout(5, 5));
            contentAreaPanel.add(new JLabel("Lesson Content:"), BorderLayout.NORTH);
            JTextArea contentArea = new JTextArea(10, 30);
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            JScrollPane contentScroll = new JScrollPane(contentArea);
            contentAreaPanel.add(contentScroll, BorderLayout.CENTER);

            JPanel resourcesPanel = new JPanel(new BorderLayout(5, 5));
            resourcesPanel.add(new JLabel("Resources:"), BorderLayout.NORTH);
            JTextArea resourcesArea = new JTextArea(3, 30);
            resourcesArea.setEditable(false);
            resourcesArea.setLineWrap(true);
            JScrollPane resourcesScroll = new JScrollPane(resourcesArea);
            resourcesPanel.add(resourcesScroll, BorderLayout.CENTER);

            JPanel quizPanel = new JPanel();
            quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
            quizPanel.setBorder(BorderFactory.createTitledBorder("Quiz"));
            JButton submitQuizButton = new JButton("Submit Quiz");

            contentPanel.add(contentAreaPanel, BorderLayout.NORTH);
            contentPanel.add(resourcesPanel, BorderLayout.CENTER);
            contentPanel.add(quizPanel, BorderLayout.SOUTH);

            lessonsList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int index = lessonsList.getSelectedIndex();
                    if (index >= 0) {
                        Lesson selectedLesson = lessons.get(index);
                        contentArea.setText(selectedLesson.getContent());

                        StringBuilder resourcesText = new StringBuilder();
                        for (String resource : selectedLesson.getResources()) {
                            resourcesText.append("• ").append(resource).append("\n");
                        }
                        if (resourcesText.length() == 0) {
                            resourcesText.append("No resources available for this lesson.");
                        }
                        resourcesArea.setText(resourcesText.toString());

                        quizPanel.removeAll();
                        Quiz lessonQuiz = selectedLesson.getQuiz();
                        if (lessonQuiz != null && !lessonQuiz.getQuestions().isEmpty()) {
                            ArrayList<JComboBox<String>> answerBoxes = new ArrayList<>();
                            int qNum = 1;
                            for (Question q : lessonQuiz.getQuestions()) {
                                JLabel qLabel = new JLabel(qNum + ". " + q.getQuestion());
                                quizPanel.add(qLabel);
                                JComboBox<String> comboBox = new JComboBox<>(q.getOption().toArray(new String[0]));
                                quizPanel.add(comboBox);
                                answerBoxes.add(comboBox);
                                qNum++;
                            }

                            submitQuizButton.addActionListener(ev -> {
                                ArrayList<Integer> answers = new ArrayList<>();
                                for (JComboBox<String> cb : answerBoxes) {
                                    answers.add(cb.getSelectedIndex());
                                }
                                boolean success = QuizService.submitQuiz(currentStudent.getUserId(), courseId, selectedLesson.getLessonId(), answers);
                                if (success) {
                                    JOptionPane.showMessageDialog(lessonsDialog, "Quiz submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(lessonsDialog, "Failed to submit quiz.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });

                            quizPanel.add(submitQuizButton);
                        } else {
                            quizPanel.add(new JLabel("No quiz available for this lesson."));
                        }

                        quizPanel.revalidate();
                        quizPanel.repaint();
                    }
                }
            });

            if (!lessons.isEmpty()) {
                lessonsList.setSelectedIndex(0);
            }

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, contentPanel);
            splitPane.setDividerLocation(200);

            lessonsDialog.add(splitPane, BorderLayout.CENTER);
            lessonsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void markLessonComplete() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course first", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
        ArrayList<Lesson> lessons = LessonService.getLessons(courseId);
        if (lessons.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No lessons available to mark as complete.",
                    "No Lessons",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ArrayList<Lesson> incompleteLessons = new ArrayList<>();
        if(currentStudent.)
        for (Lesson lesson : lessons) {
            if (!lesson.isCompleted()) {
                incompleteLessons.add(lesson);
            }
        }
        if (incompleteLessons.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All lessons are already completed!", "No Lessons", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] lessonTitles = incompleteLessons.stream()
                .map(Lesson::getTitle)
                .toArray(String[]::new);

        String selectedLesson = (String) JOptionPane.showInputDialog(this,
                "Select lesson to mark as complete:",
                "Mark Lesson Complete",
                JOptionPane.QUESTION_MESSAGE,
                null,
                lessonTitles,
                lessonTitles[0]);

        if (selectedLesson == null) return;
        String lessonId = null;
        for (Lesson lesson : incompleteLessons) {
            if (lesson.getTitle().equals(selectedLesson)) {
                lessonId = lesson.getLessonId();
                break;
            }
        }

        if (lessonId != null) {
            boolean success = StudentService.markLessonCompleted(currentStudent, lessonId,courseId);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Lesson marked as complete! Progress updated.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadStudentData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lesson was already completed or failed to update",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(0, 102, 204));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
}