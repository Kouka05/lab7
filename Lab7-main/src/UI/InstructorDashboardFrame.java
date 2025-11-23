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

    // Add component references for lesson management
    private JTextField lessonTitleField;
    private JTextArea lessonContentArea;
    private JTextArea lessonResourcesArea;
    private JTable quizTable;
    private DefaultTableModel quizTableModel;

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

    private void initializeUI() {
        setTitle("E-Learning System - Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Courses", createCoursesPanel());
        tabbedPane.addTab("Students", createStudentsPanel());
        tabbedPane.addTab("Create Course", createCreateCoursePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadInstructorData() {
        welcomeLabel.setText("Welcome, " + currentInstructor.getUsername() + "!");
        refreshCoursesTable();
        refreshStudentsTable();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(204, 0, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        welcomeLabel = new JLabel("Welcome, Instructor!");
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

        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Course ID", "Course Name", "Enrolled Students", "Lessons", "Status"};
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

        JButton manageCourseButton = new JButton("Manage Course");
        JButton addLessonButton = new JButton("Add Lesson");
        JButton viewStudentsButton = new JButton("View Students");
        JButton viewLessonsButton = new JButton("View Lessons");
        JButton deleteCourseButton = new JButton("Delete Course");
        JButton refreshButton = new JButton("Refresh");

        styleActionButton(manageCourseButton);
        styleActionButton(addLessonButton);
        styleActionButton(viewStudentsButton);
        styleActionButton(viewLessonsButton);
        styleActionButton(deleteCourseButton);
        styleActionButton(refreshButton);

        manageCourseButton.addActionListener(e -> {
            if(getStatus())
                manageSelectedCourse();
        });
        addLessonButton.addActionListener(e -> {
            if(getStatus())
                addLessonToCourse();
        });
        viewStudentsButton.addActionListener(e -> {
            if(getStatus())
                viewCourseStudents();
        });
        viewLessonsButton.addActionListener(e ->{
            if(getStatus())
                viewCourseLessons();
        } );
        deleteCourseButton.addActionListener(e -> deleteSelectedCourse());
        refreshButton.addActionListener(e -> refreshCoursesTable());

        buttonPanel.add(manageCourseButton);
        buttonPanel.add(addLessonButton);
        buttonPanel.add(viewStudentsButton);
        buttonPanel.add(viewLessonsButton);
        buttonPanel.add(deleteCourseButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Student ID", "Student Name", "Email", "Course", "Progress", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        studentsTable = new JTable(model);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        studentsTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton removeStudentButton = new JButton("Remove Student from Course");
        JButton refreshButton = new JButton("Refresh Students");

        styleActionButton(removeStudentButton);
        styleActionButton(refreshButton);

        removeStudentButton.addActionListener(e -> removeStudentFromCourse());
        refreshButton.addActionListener(e -> refreshStudentsTable());

        buttonPanel.add(removeStudentButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void removeStudentFromCourse() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String studentId = (String) studentsTable.getValueAt(selectedRow, 0);
            String studentName = (String) studentsTable.getValueAt(selectedRow, 1);
            String courseName = (String) studentsTable.getValueAt(selectedRow, 3);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove " + studentName + " from " + courseName + "?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                String courseId = null;
                ArrayList<Course> instructorCourses = CourseService.getCourseByInstructor(currentInstructor.getUserId());
                for (Course course : instructorCourses) {
                    if (course.getTitle().equals(courseName)) {
                        courseId = course.getCourseId();
                        break;
                    }
                }

                if (courseId != null) {
                    boolean success = CourseService.removeStudentFromCourse(courseId, studentId);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                                "Student removed from course successfully", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshStudentsTable();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to remove student from course", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a student first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel createCreateCoursePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Create New Course");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(createLabel("Course Name:"), gbc);

        gbc.gridx = 1;
        JTextField courseNameField = createTextField();
        panel.add(courseNameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(createLabel("Description:"), gbc);

        gbc.gridx = 1;
        JTextArea descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton createButton = new JButton("Create Course");
        styleActionButton(createButton);
        createButton.addActionListener(e -> createNewCourse(
                courseNameField.getText(),
                descriptionArea.getText()
        ));
        panel.add(createButton, gbc);

        return panel;
    }

    private void refreshCoursesTable() {
        DefaultTableModel model = (DefaultTableModel) coursesTable.getModel();
        model.setRowCount(0);

        ArrayList<Course> instructorCourses =
                CourseService.getCourseByInstructor(currentInstructor.getUserId());

        for (Course course : instructorCourses) {

            String status;
            int approval = course.getApprovalStatus();

            if (approval == 1)      status = "Pending";
            else if (approval == 2) status = "Approved";
            else if (approval == 3) status = "Rejected";
            else                    status = "Unknown";

            model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getStudents().size(),
                    course.getLessons().size(),
                    status
            });
        }
    }

    private void refreshStudentsTable() {
        DefaultTableModel model = (DefaultTableModel) studentsTable.getModel();
        model.setRowCount(0);

        ArrayList<Course> instructorCourses = CourseService.getCourseByInstructor(currentInstructor.getUserId());
        ArrayList<User> allUsers = JSONDatabaseManager.loadUsers();

        for (Course course : instructorCourses) {
            for (String studentId : course.getStudents()) {
                for (User user : allUsers) {
                    if (user.getUserId().equals(studentId) && user instanceof Student) {
                        Student student = (Student) user;
                        int progress = student.getProgress().getOrDefault(course.getCourseId(), 0);
                        model.addRow(new Object[]{
                                student.getUserId(),
                                student.getUsername(),
                                student.getEmail(),
                                course.getTitle(),
                                progress + "%"
                        });
                        break;
                    }
                }
            }
        }
    }

    private void createNewCourse(String courseName, String description) {
        if (courseName.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = CourseService.createCourse(currentInstructor.getUserId(), courseName, description);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Course '" + courseName + "' created successfully!", "Course Created",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshCoursesTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "A course with this name already exists. Please choose a different name.",
                        "Duplicate Course", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void manageSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

            String[] options = {"Edit Course", "View Lessons", "View Students", "Cancel"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Manage course: " + courseName,
                    "Course Management",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (choice) {
                case 0:
                    editCourse(courseId, courseName);
                    break;
                case 1:
                    viewCourseLessons(courseId, courseName);
                    break;
                case 2:
                    viewCourseStudents(courseId, courseName);
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editCourse(String courseId, String courseName) {
        String newTitle = JOptionPane.showInputDialog(this, "Enter new course title:", courseName);
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            boolean success = CourseService.editCourse(courseId, newTitle, null);
            if (success) {
                JOptionPane.showMessageDialog(this, "Course updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCoursesTable();
            }
        }
    }

    private void addLessonToCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

            JDialog lessonDialog = new JDialog(this, "Add Lesson to " + courseName, true);
            lessonDialog.setSize(500, 500);
            lessonDialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
            titlePanel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
            JTextField titleField = new JTextField();
            titlePanel.add(titleField, BorderLayout.CENTER);

            JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
            contentPanel.add(new JLabel("Lesson Content:"), BorderLayout.NORTH);
            JTextArea contentArea = new JTextArea(8, 30);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            JScrollPane contentScroll = new JScrollPane(contentArea);
            contentPanel.add(contentScroll, BorderLayout.CENTER);

            JPanel resourcesPanel = new JPanel(new BorderLayout(5, 5));
            resourcesPanel.add(new JLabel("Resources (one per line, optional):"), BorderLayout.NORTH);
            JTextArea resourcesArea = new JTextArea(3, 30);
            resourcesArea.setLineWrap(true);
            JScrollPane resourcesScroll = new JScrollPane(resourcesArea);
            resourcesPanel.add(resourcesScroll, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton saveButton = new JButton("Save Lesson");
            JButton cancelButton = new JButton("Cancel");

            saveButton.addActionListener(e -> {
                String lessonTitle = titleField.getText().trim();
                String content = contentArea.getText().trim();

                ArrayList<String> resources = new ArrayList<>();
                String[] resourcesArray = resourcesArea.getText().split("\n");
                for (String resource : resourcesArray) {
                    if (!resource.trim().isEmpty()) {
                        resources.add(resource.trim());
                    }
                }

                if (lessonTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(lessonDialog, "Please enter a lesson title", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Lesson newLesson = new Lesson(null, lessonTitle, content, resources);
                boolean success = LessonService.addLesson(courseId, newLesson);
                if (success) {
                    JOptionPane.showMessageDialog(lessonDialog, "Lesson added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    lessonDialog.dispose();
                    refreshCoursesTable();
                } else {
                    JOptionPane.showMessageDialog(lessonDialog, "Failed to add lesson", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> lessonDialog.dispose());

            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);

            mainPanel.add(titlePanel);
            mainPanel.add(contentPanel);
            mainPanel.add(resourcesPanel);
            mainPanel.add(buttonPanel);

            lessonDialog.add(mainPanel);
            lessonDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewCourseStudents(String courseId, String courseName) {
        Course course = CourseService.getCourseById(courseId);
        if (course != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Students enrolled in '").append(courseName).append("':\n\n");

            ArrayList<User> allUsers = JSONDatabaseManager.loadUsers();
            for (String studentId : course.getStudents()) {
                for (User user : allUsers) {
                    if (user.getUserId().equals(studentId)) {
                        sb.append("• ").append(user.getUsername())
                                .append(" (").append(user.getEmail()).append(")\n");
                        break;
                    }
                }
            }

            if (course.getStudents().isEmpty()) {
                sb.append("No students enrolled yet.");
            }

            JOptionPane.showMessageDialog(this, sb.toString(), "Enrolled Students", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewCourseStudents() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);
            viewCourseStudents(courseId, courseName);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewCourseLessons() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);
            viewCourseLessons(courseId, courseName);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

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

        // Create the lesson content components directly
        lessonTitleField = new JTextField();
        lessonContentArea = new JTextArea(8, 30);
        lessonContentArea.setLineWrap(true);
        lessonContentArea.setWrapStyleWord(true);
        lessonResourcesArea = new JTextArea(3, 30);
        lessonResourcesArea.setLineWrap(true);

        // Create lesson content panel
        JPanel lessonContentPanel = createLessonContentPanel();
        
        // Create quiz panel
        JPanel quizPanel = createQuizPanel();

        JTabbedPane contentTabs = new JTabbedPane();
        contentTabs.addTab("Lesson Content", lessonContentPanel);
        contentTabs.addTab("Quiz Management", quizPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit Lesson");
        JButton deleteButton = new JButton("Delete Lesson");
        JButton manageQuizButton = new JButton("Manage Quiz");
        JButton closeButton = new JButton("Close");

        lessonsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = lessonsList.getSelectedIndex();
                if (index >= 0) {
                    Lesson selectedLesson = lessons.get(index);
                    
                    // Update lesson content
                    lessonTitleField.setText(selectedLesson.getTitle());
                    lessonContentArea.setText(selectedLesson.getContent());

                    StringBuilder resourcesText = new StringBuilder();
                    for (String resource : selectedLesson.getResources()) {
                        resourcesText.append(resource).append("\n");
                    }
                    lessonResourcesArea.setText(resourcesText.toString());

                    // Update quiz management tab
                    refreshQuizTable(selectedLesson);
                }
            }
        });

        editButton.addActionListener(e -> {
            int index = lessonsList.getSelectedIndex();
            if (index >= 0) {
                Lesson selectedLesson = lessons.get(index);
                String newTitle = lessonTitleField.getText().trim();
                String newContent = lessonContentArea.getText().trim();

                ArrayList<String> newResources = new ArrayList<>();
                String[] resourcesArray = lessonResourcesArea.getText().split("\n");
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
                            lessonTitleField.setText("");
                            lessonContentArea.setText("");
                            lessonResourcesArea.setText("");
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
        titlePanel.add(lessonTitleField, BorderLayout.CENTER);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.add(new JLabel("Lesson Content:"), BorderLayout.NORTH);
        JScrollPane contentScroll = new JScrollPane(lessonContentArea);
        contentPanel.add(contentScroll, BorderLayout.CENTER);

        // Resources Panel
        JPanel resourcesPanel = new JPanel(new BorderLayout(5, 5));
        resourcesPanel.add(new JLabel("Resources (one per line):"), BorderLayout.NORTH);
        JScrollPane resourcesScroll = new JScrollPane(lessonResourcesArea);
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
        quizTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        quizTable = new JTable(quizTableModel);
        JScrollPane tableScroll = new JScrollPane(quizTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh Quiz View");

        styleActionButton(refreshButton);

        refreshButton.addActionListener(e -> {
            int index = ((JList<String>) SwingUtilities.getAncestorOfClass(JList.class, quizTable)).getSelectedIndex();
            if (index >= 0) {
                // This would need access to the lessons list, which we don't have here
                // For now, just show a message
                JOptionPane.showMessageDialog(this, "Please use the 'Manage Quiz' button to modify quizzes", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(refreshButton);

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshQuizTable(Lesson lesson) {
        quizTableModel.setRowCount(0);

        Quiz quiz = lesson.getQuiz();
        if (quiz != null && quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                String options = String.join(" | ", question.getOption());
                String correctAnswer = question.getOption().get(question.getCorrectIndex());
                quizTableModel.addRow(new Object[]{
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

        addButton.addActionListener(e -> showQuestionDialog(null, tableModel));
        editButton.addActionListener(e -> {
            int selectedRow = questionsTable.getSelectedRow();
            if (selectedRow >= 0) {
                // For simplicity, we'll just remove and re-add
                tableModel.removeRow(selectedRow);
                showQuestionDialog(null, tableModel);
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

    private void showQuestionDialog(Question existingQuestion, DefaultTableModel tableModel) {
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
            
            // Refresh the quiz table in the parent dialog
            refreshQuizTable(lesson);
        } else {
            JOptionPane.showMessageDialog(quizDialog, "Failed to save quiz", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCourse() {
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + courseName + "?\nThis action cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = CourseService.deleteCourse(courseId);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Course deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        refreshCoursesTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete course", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 204, 204)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return field;
    }

    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(204, 0, 0));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(204, 0, 0));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
    
    private boolean getStatus(){
        int selectedRow = coursesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String courseId = (String) coursesTable.getValueAt(selectedRow, 0);
            String courseName = (String) coursesTable.getValueAt(selectedRow, 1);
            String status = (String) coursesTable.getValueAt(selectedRow, 4);

            if (status.equals("Rejected")) {
                JOptionPane.showMessageDialog(this, "Course Has Been Rejected By Admin!", "warning", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }
}
