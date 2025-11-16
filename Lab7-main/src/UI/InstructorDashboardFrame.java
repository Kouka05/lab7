package UI;

import Logic.AuthenticationService;
import Logic.CourseService;
import Logic.LessonService;
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

        manageCourseButton.addActionListener(e -> manageSelectedCourse());
        addLessonButton.addActionListener(e -> addLessonToCourse());
        viewStudentsButton.addActionListener(e -> viewCourseStudents());
        viewLessonsButton.addActionListener(e -> viewCourseLessons());
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

        ArrayList<Course> instructorCourses = CourseService.getCourseByInstructor(currentInstructor.getUserId());

        for (Course course : instructorCourses) {
            model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getStudents().size(),
                    course.getLessons().size(),
                    "Active"
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
            lessonDialog.setLayout(new BorderLayout());
            lessonDialog.setSize(500, 500);
            lessonDialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
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

            mainPanel.add(titlePanel, BorderLayout.NORTH);
            mainPanel.add(contentPanel, BorderLayout.CENTER);
            mainPanel.add(resourcesPanel, BorderLayout.SOUTH);

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
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

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
        lessonsDialog.setSize(700, 600);
        lessonsDialog.setLocationRelativeTo(this);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Lesson lesson : lessons) {
            listModel.addElement(lesson.getTitle() + " (ID: " + lesson.getLessonId() + ")");
        }

        JList<String> lessonsList = new JList<>(listModel);
        lessonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(lessonsList);

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
        titlePanel.add(new JLabel("Lesson Title:"), BorderLayout.NORTH);
        JTextField titleField = new JTextField();
        titlePanel.add(titleField, BorderLayout.CENTER);

        JPanel contentAreaPanel = new JPanel(new BorderLayout(5, 5));
        contentAreaPanel.add(new JLabel("Lesson Content:"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentAreaPanel.add(contentScroll, BorderLayout.CENTER);

        JPanel resourcesPanel = new JPanel(new BorderLayout(5, 5));
        resourcesPanel.add(new JLabel("Resources (one per line):"), BorderLayout.NORTH);
        JTextArea resourcesArea = new JTextArea(3, 30);
        resourcesArea.setLineWrap(true);
        JScrollPane resourcesScroll = new JScrollPane(resourcesArea);
        resourcesPanel.add(resourcesScroll, BorderLayout.CENTER);

        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(contentAreaPanel, BorderLayout.CENTER);
        contentPanel.add(resourcesPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editButton = new JButton("Edit Lesson");
        JButton deleteButton = new JButton("Delete Lesson");
        JButton closeButton = new JButton("Close");

        lessonsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = lessonsList.getSelectedIndex();
                if (index >= 0) {
                    Lesson selectedLesson = lessons.get(index);
                    titleField.setText(selectedLesson.getTitle());
                    contentArea.setText(selectedLesson.getContent());

                    StringBuilder resourcesText = new StringBuilder();
                    for (String resource : selectedLesson.getResources()) {
                        resourcesText.append(resource).append("\n");
                    }
                    resourcesArea.setText(resourcesText.toString());
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
                } else {
                    JOptionPane.showMessageDialog(lessonsDialog, "Failed to update lesson", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(lessonsDialog, "Please select a lesson to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
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
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);

        if (!lessons.isEmpty()) {
            lessonsList.setSelectedIndex(0);
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, contentPanel);
        splitPane.setDividerLocation(250);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        lessonsDialog.add(mainPanel);
        lessonsDialog.setVisible(true);
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
}