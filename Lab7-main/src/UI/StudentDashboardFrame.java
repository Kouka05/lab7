package UI;

import Database.JSONDatabaseManager;
import Logic.AuthenticationService;
import Logic.CourseService;
import Logic.LessonService;
import Logic.StudentService;
import Logic.QuizService;
import Model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class StudentDashboardFrame extends JFrame {
    private JTable coursesTable;
    private JTable availableCoursesTable;
    private JTable certificatesTable;
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
        tabbedPane.addTab("Certificates", createCertificatesPanel());
        tabbedPane.addTab("Progress", createProgressPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void refreshCurrentStudent() {
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        for (User user : users) {
            if (user.getUserId().equals(currentStudent.getUserId()) && user instanceof Student) {
                Student updatedStudent = (Student) user;

                // Preserve the current progress bar reference if it exists
                if (this.currentStudent != null && overallProgressBar != null) {
                    int oldProgress = overallProgressBar.getValue();
                    // You might want to compare with new progress and update if different
                }

                this.currentStudent = updatedStudent;
                AuthenticationService.updateLoggedInUser(currentStudent);

                // Ensure all maps are initialized
                if (currentStudent.getProgress() == null) {
                    currentStudent.setProgress(new HashMap<>());
                }
                if (currentStudent.getLessonProgress() == null) {
                    currentStudent.setLessonProgress(new HashMap<>());
                }
                if (currentStudent.getCertificates() == null) {
                    currentStudent.setCertificates(new ArrayList<>());
                }

                System.out.println("Student data refreshed. Progress: " +
                        currentStudent.getProgress().toString());
                break;
            }
        }
    }

    private void loadStudentData() {
        refreshCurrentStudent();
        welcomeLabel.setText("Welcome, " + currentStudent.getUsername() + "!");
        refreshEnrolledCoursesTable();
        refreshAvailableCoursesTable();
        refreshCertificatesTable();
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
        JButton unenrollButton = new JButton("Unenroll");
        JButton refreshButton = new JButton("Refresh");

        styleActionButton(viewLessonsButton);
        styleActionButton(unenrollButton);
        styleActionButton(refreshButton);

        viewLessonsButton.addActionListener(e -> viewCourseLessons());
        unenrollButton.addActionListener(e -> unenrollFromCourse());
        refreshButton.addActionListener(e -> refreshEnrolledCoursesTable());

        buttonPanel.add(viewLessonsButton);
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

    private JPanel createCertificatesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("My Certificates");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Certificate ID", "Course", "Issue Date", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        certificatesTable = new JTable(model);
        certificatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(certificatesTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton viewCertificateButton = new JButton("View Certificate");
        JButton refreshButton = new JButton("Refresh");

        styleActionButton(viewCertificateButton);
        styleActionButton(refreshButton);

        viewCertificateButton.addActionListener(e -> viewCertificate());
        refreshButton.addActionListener(e -> refreshCertificatesTable());

        buttonPanel.add(viewCertificateButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Main progress panel
        JPanel mainProgressPanel = new JPanel();
        mainProgressPanel.setLayout(new BoxLayout(mainProgressPanel, BoxLayout.Y_AXIS));
        mainProgressPanel.setBackground(Color.WHITE);

        // Overall Progress Section
        JPanel overallProgressPanel = new JPanel(new BorderLayout());
        overallProgressPanel.setBackground(Color.WHITE);
        overallProgressPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204)),
                "Overall Learning Progress"
        ));

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setValue(0);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setFont(new Font("Arial", Font.BOLD, 14));
        overallProgressBar.setForeground(new Color(0, 150, 0));
        overallProgressBar.setBackground(new Color(220, 220, 220));
        overallProgressBar.setPreferredSize(new Dimension(300, 30));

        JLabel progressValueLabel = new JLabel("0%");
        progressValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        progressValueLabel.setForeground(new Color(0, 102, 204));

        overallProgressPanel.add(overallProgressBar, BorderLayout.CENTER);
        overallProgressPanel.add(progressValueLabel, BorderLayout.EAST);

        // Course-wise Progress Section
        JPanel coursesProgressPanel = new JPanel();
        coursesProgressPanel.setLayout(new BoxLayout(coursesProgressPanel, BoxLayout.Y_AXIS));
        coursesProgressPanel.setBackground(Color.WHITE);
        coursesProgressPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 204)),
                "Course Progress"
        ));

        // Create a panel to hold course progress bars
        JPanel coursesContainer = new JPanel();
        coursesContainer.setLayout(new BoxLayout(coursesContainer, BoxLayout.Y_AXIS));
        coursesContainer.setBackground(Color.WHITE);

        JScrollPane coursesScroll = new JScrollPane(coursesContainer);
        coursesScroll.setPreferredSize(new Dimension(500, 200));
        coursesScroll.setBorder(BorderFactory.createEmptyBorder());

        coursesProgressPanel.add(coursesScroll);

        // Refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Progress");
        styleActionButton(refreshButton);

        refreshButton.addActionListener(e -> {
            refreshCurrentStudent();
            updateProgressDisplay(progressValueLabel, coursesContainer);
        });

        buttonPanel.add(refreshButton);

        mainProgressPanel.add(overallProgressPanel);
        mainProgressPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainProgressPanel.add(coursesProgressPanel);
        mainProgressPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainProgressPanel.add(buttonPanel);

        panel.add(mainProgressPanel, BorderLayout.CENTER);

        // Initial update
        updateProgressDisplay(progressValueLabel, coursesContainer);

        return panel;
    }

    private void updateProgressDisplay(JLabel progressValueLabel, JPanel coursesContainer) {
        // Calculate overall progress
        int totalProgress = 0;
        int courseCount = currentStudent.getEnrolledCourses().size();

        if (courseCount > 0) {
            for (int progress : currentStudent.getProgress().values()) {
                totalProgress += progress;
            }
            int averageProgress = totalProgress / courseCount;
            overallProgressBar.setValue(averageProgress);
            progressValueLabel.setText(averageProgress + "%");
        } else {
            overallProgressBar.setValue(0);
            progressValueLabel.setText("0%");
        }

        // Update course-wise progress
        updateCourseProgressBars(coursesContainer);
    }

    private void updateCourseProgressBars(JPanel coursesContainer) {
        coursesContainer.removeAll();

        ArrayList<Course> enrolledCourses = StudentService.getEnrolledCourses(currentStudent.getUserId());

        if (enrolledCourses.isEmpty()) {
            JLabel noCoursesLabel = new JLabel("No enrolled courses");
            noCoursesLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noCoursesLabel.setForeground(Color.GRAY);
            coursesContainer.add(noCoursesLabel);
        } else {
            for (Course course : enrolledCourses) {
                JPanel coursePanel = new JPanel(new BorderLayout());
                coursePanel.setBackground(Color.WHITE);
                coursePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                coursePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                // Course name and progress label
                JPanel infoPanel = new JPanel(new BorderLayout());
                infoPanel.setBackground(Color.WHITE);

                JLabel courseNameLabel = new JLabel(course.getTitle());
                courseNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

                int progress = currentStudent.getProgress().getOrDefault(course.getCourseId(), 0);
                JLabel progressLabel = new JLabel(progress + "%");
                progressLabel.setFont(new Font("Arial", Font.BOLD, 14));
                progressLabel.setForeground(new Color(0, 102, 204));

                infoPanel.add(courseNameLabel, BorderLayout.WEST);
                infoPanel.add(progressLabel, BorderLayout.EAST);

                // Progress bar
                JProgressBar courseProgressBar = new JProgressBar(0, 100);
                courseProgressBar.setValue(progress);
                courseProgressBar.setStringPainted(true);
                courseProgressBar.setForeground(getProgressColor(progress));
                courseProgressBar.setBackground(new Color(220, 220, 220));
                courseProgressBar.setString(progress + "%");

                // Lesson completion details
                JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                detailsPanel.setBackground(Color.WHITE);

                int totalLessons = course.getLessons().size();
                int completedLessons = countCompletedLessonsInCourse(course);

                JLabel detailsLabel = new JLabel(
                        String.format("Lessons: %d/%d completed", completedLessons, totalLessons)
                );
                detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                detailsLabel.setForeground(Color.DARK_GRAY);
                detailsPanel.add(detailsLabel);

                coursePanel.add(infoPanel, BorderLayout.NORTH);
                coursePanel.add(courseProgressBar, BorderLayout.CENTER);
                coursePanel.add(detailsPanel, BorderLayout.SOUTH);

                coursesContainer.add(coursePanel);
                coursesContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        coursesContainer.revalidate();
        coursesContainer.repaint();
    }

    private int countCompletedLessonsInCourse(Course course) {
        int completed = 0;
        for (Lesson lesson : course.getLessons()) {
            // Use the same key format as in StudentService: courseId + lessonId
            String progressKey = course.getCourseId() + lesson.getLessonId();
            LessonProgress progress = currentStudent.getLessonProgress().get(progressKey);
            if (progress != null && progress.isCompleted()) {
                completed++;
            }
        }
        return completed;
    }

    private Color getProgressColor(int progress) {
        if (progress < 30) {
            return new Color(220, 20, 60); // Red
        } else if (progress < 70) {
            return new Color(255, 165, 0); // Orange
        } else {
            return new Color(50, 205, 50); // Green
        }
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
                    "View Lessons"
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

    private void refreshCertificatesTable() {
        DefaultTableModel model = (DefaultTableModel) certificatesTable.getModel();
        model.setRowCount(0);

        ArrayList<Certificate> certificates = StudentService.getStudentCertificates(currentStudent.getUserId());

        for (Certificate certificate : certificates) {
            Course course = CourseService.getCourseById(certificate.getCourseId());
            String courseName = (course != null) ? course.getTitle() : "Unknown Course";

            model.addRow(new Object[]{
                    certificate.getCertificateId(),
                    courseName,
                    certificate.getIssueDate(),
                    "View"
            });
        }
    }

    private void refreshProgress() {
        // This method is called from loadStudentData to update the progress bar
        // The actual progress bar update is now handled in updateProgressDisplay
        // We need to ensure the Progress tab gets updated when student data changes

        // If the progress panel components are initialized, update them
        if (overallProgressBar != null) {
            Component[] components = getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof JTabbedPane) {
                    JTabbedPane tabbedPane = (JTabbedPane) comp;
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        Component tabComponent = tabbedPane.getComponentAt(i);
                        if (tabComponent instanceof JPanel) {
                            // Look for the progress panel and refresh it
                            refreshProgressPanel((JPanel) tabComponent);
                        }
                    }
                }
            }
        }
    }

    private void refreshProgressPanel(JPanel panel) {
        // Recursively look for progress bar components and update them
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JProgressBar) {
                JProgressBar progressBar = (JProgressBar) comp;
                if (progressBar == overallProgressBar) {
                    // Update the overall progress bar
                    int totalProgress = 0;
                    int courseCount = currentStudent.getEnrolledCourses().size();

                    if (courseCount > 0) {
                        for (int progress : currentStudent.getProgress().values()) {
                            totalProgress += progress;
                        }
                        int averageProgress = totalProgress / courseCount;
                        progressBar.setValue(averageProgress);

                        // Update the label if it exists
                        Container parent = progressBar.getParent();
                        if (parent != null) {
                            for (Component child : parent.getComponents()) {
                                if (child instanceof JLabel) {
                                    JLabel label = (JLabel) child;
                                    if (label.getText().contains("%")) {
                                        label.setText(averageProgress + "%");
                                    }
                                }
                            }
                        }
                    } else {
                        progressBar.setValue(0);
                    }
                }
            } else if (comp instanceof JPanel) {
                refreshProgressPanel((JPanel) comp);
            }
        }
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
            lessonsDialog.setSize(900, 700);
            lessonsDialog.setLocationRelativeTo(this);

            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (int i = 0; i < lessons.size(); i++) {
                Lesson lesson = lessons.get(i);

                // Always get fresh progress data for this specific lesson using the correct key format
                String progressKey = courseId + lesson.getLessonId();
                LessonProgress progress = currentStudent.getLessonProgress().get(progressKey);

                String status = "";
                if (progress != null) {
                    if (progress.isCompleted()) {
                        status = " ✓ (Score: " + progress.getQuizScore() + "%, Attempts: " + progress.getAttempts() + ")";
                    } else if (progress.getQuizScore() != null) {
                        status = " ✗ (Score: " + progress.getQuizScore() + "%, Attempts: " + progress.getAttempts() + ")";
                    }
                }

                // Check if lesson is accessible
                boolean accessible = StudentService.isLessonAccessible(currentStudent, courseId, lesson.getLessonId());
                String accessibleIcon = accessible ? "" : " 🔒";

                listModel.addElement((i + 1) + ". " + lesson.getTitle() + status + accessibleIcon);
            }

            JList<String> lessonsList = new JList<>(listModel);
            lessonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Custom cell renderer to show locked lessons as disabled
            lessonsList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (index >= 0 && index < lessons.size()) {
                        Lesson lesson = lessons.get(index);
                        boolean accessible = StudentService.isLessonAccessible(currentStudent, courseId, lesson.getLessonId());

                        if (!accessible && index > 0) {
                            c.setEnabled(false);
                            c.setBackground(Color.LIGHT_GRAY);
                            setForeground(Color.DARK_GRAY);
                        } else {
                            c.setEnabled(true);
                            if (isSelected) {
                                c.setBackground(new Color(0, 102, 204));
                                setForeground(Color.WHITE);
                            } else {
                                c.setBackground(Color.WHITE);
                                setForeground(Color.BLACK);
                            }
                        }

                        // Show completed lessons with green text
                        String progressKey = courseId + lesson.getLessonId();
                        LessonProgress progress = currentStudent.getLessonProgress().get(progressKey);
                        if (progress != null && progress.isCompleted()) {
                            setForeground(new Color(0, 128, 0)); // Green for completed
                        } else if (progress != null && progress.getQuizScore() != null) {
                            setForeground(new Color(255, 140, 0)); // Orange for attempted but not passed
                        }
                    }
                    return c;
                }
            });

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

            contentPanel.add(contentAreaPanel, BorderLayout.NORTH);
            contentPanel.add(resourcesPanel, BorderLayout.CENTER);
            contentPanel.add(quizPanel, BorderLayout.SOUTH);

            lessonsList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int index = lessonsList.getSelectedIndex();
                    if (index >= 0) {
                        Lesson selectedLesson = lessons.get(index);

                        // Check if lesson is accessible
                        boolean accessible = StudentService.isLessonAccessible(currentStudent, courseId, selectedLesson.getLessonId());
                        if (!accessible && index > 0) {
                            JOptionPane.showMessageDialog(lessonsDialog,
                                    "You must complete the previous lesson and pass its quiz first!\n" +
                                            "Please complete Lesson " + index + " with a score of 50% or higher to unlock this lesson.",
                                    "Lesson Locked", JOptionPane.WARNING_MESSAGE);

                            // Clear content for locked lessons
                            contentArea.setText("This lesson is locked. Complete the previous lesson to unlock.");
                            resourcesArea.setText("");
                            quizPanel.removeAll();
                            quizPanel.add(new JLabel("Lesson locked. Complete previous lesson first."));
                            quizPanel.revalidate();
                            quizPanel.repaint();
                            return;
                        }

                        // Load lesson content for accessible lessons
                        contentArea.setText(selectedLesson.getContent());

                        StringBuilder resourcesText = new StringBuilder();
                        for (String resource : selectedLesson.getResources()) {
                            resourcesText.append("• ").append(resource).append("\n");
                        }
                        if (resourcesText.length() == 0) {
                            resourcesText.append("No resources available for this lesson.");
                        }
                        resourcesArea.setText(resourcesText.toString());

                        // Update quiz panel
                        quizPanel.removeAll();
                        Quiz lessonQuiz = selectedLesson.getQuiz();
                        String progressKey = courseId + selectedLesson.getLessonId();
                        LessonProgress progress = currentStudent.getLessonProgress().get(progressKey);

                        if (lessonQuiz != null && !lessonQuiz.getQuestions().isEmpty()) {
                            // Check if already completed
                            if (progress != null && progress.isCompleted()) {
                                JPanel completedPanel = new JPanel(new BorderLayout());
                                JLabel completedLabel = new JLabel(
                                        "<html><b>Quiz Completed!</b><br>" +
                                                "Score: " + progress.getQuizScore() + "%<br>" +
                                                "Attempts: " + progress.getAttempts() + "<br>" +
                                                "Status: PASSED ✓</html>"
                                );
                                completedLabel.setForeground(new Color(0, 128, 0));
                                completedPanel.add(completedLabel, BorderLayout.CENTER);

                                JButton retakeButton = new JButton("Retake Quiz");
                                retakeButton.addActionListener(ev -> {
                                    showQuizForLesson(lessonQuiz, courseId, selectedLesson, lessonsDialog, true);
                                });
                                completedPanel.add(retakeButton, BorderLayout.SOUTH);

                                quizPanel.add(completedPanel);
                            }
                            // Check if attempted but not passed
                            else if (progress != null && progress.getQuizScore() != null) {
                                JPanel attemptedPanel = new JPanel(new BorderLayout());
                                JLabel attemptedLabel = new JLabel(
                                        "<html><b>Quiz Attempted</b><br>" +
                                                "Score: " + progress.getQuizScore() + "%<br>" +
                                                "Attempts: " + progress.getAttempts() + "<br>" +
                                                "Status: Need 50% to pass - Please retry</html>"
                                );
                                attemptedLabel.setForeground(new Color(255, 140, 0));
                                attemptedPanel.add(attemptedLabel, BorderLayout.CENTER);

                                JButton retakeButton = new JButton("Retake Quiz");
                                retakeButton.addActionListener(ev -> {
                                    showQuizForLesson(lessonQuiz, courseId, selectedLesson, lessonsDialog, false);
                                });
                                attemptedPanel.add(retakeButton, BorderLayout.SOUTH);

                                quizPanel.add(attemptedPanel);
                            }
                            // Not attempted yet
                            else {
                                JPanel newQuizPanel = new JPanel(new BorderLayout());
                                JLabel newQuizLabel = new JLabel("This lesson has a quiz. Click below to start.");
                                newQuizPanel.add(newQuizLabel, BorderLayout.NORTH);

                                JButton startQuizButton = new JButton("Start Quiz");
                                startQuizButton.addActionListener(ev -> {
                                    showQuizForLesson(lessonQuiz, courseId, selectedLesson, lessonsDialog, false);
                                });
                                newQuizPanel.add(startQuizButton, BorderLayout.CENTER);

                                quizPanel.add(newQuizPanel);
                            }
                        } else {
                            // No quiz available
                            if (progress != null && progress.isCompleted()) {
                                JLabel completedLabel = new JLabel("Lesson completed ✓");
                                completedLabel.setForeground(new Color(0, 128, 0));
                                quizPanel.add(completedLabel);
                            } else {
                                quizPanel.add(new JLabel("No quiz available for this lesson."));

                                // Add manual completion button for lessons without quizzes
                                JButton completeButton = new JButton("Mark as Completed");
                                completeButton.addActionListener(ev -> {
                                    int confirm = JOptionPane.showConfirmDialog(lessonsDialog,
                                            "Mark this lesson as completed?",
                                            "Confirm Completion", JOptionPane.YES_NO_OPTION);

                                    if (confirm == JOptionPane.YES_OPTION) {
                                        boolean success = StudentService.updateLessonProgress(
                                                currentStudent.getUserId(), courseId, selectedLesson.getLessonId(), 100);

                                        if (success) {
                                            JOptionPane.showMessageDialog(lessonsDialog,
                                                    "Lesson marked as completed!", "Success",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                            refreshCurrentStudent();
                                            lessonsDialog.dispose();
                                            viewCourseLessons(); // Refresh
                                        }
                                    }
                                });
                                quizPanel.add(completeButton);
                            }
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
            splitPane.setDividerLocation(300);

            lessonsDialog.add(splitPane, BorderLayout.CENTER);
            lessonsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a course first", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showQuizForLesson(Quiz quiz, String courseId, Lesson lesson, JDialog parentDialog, boolean isRetake) {
        JDialog quizDialog = new JDialog(parentDialog, "Quiz - " + lesson.getTitle(), true);
        quizDialog.setSize(600, 500);
        quizDialog.setLocationRelativeTo(parentDialog);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Quiz: " + lesson.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (isRetake) {
            JLabel retakeLabel = new JLabel("This is a retake. Your previous attempts will be saved.");
            retakeLabel.setForeground(Color.BLUE);
            mainPanel.add(retakeLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        ArrayList<JComboBox<String>> answerBoxes = new ArrayList<>();
        int questionNumber = 1;

        for (Question question : quiz.getQuestions()) {
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
            questionPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            questionPanel.setBackground(Color.WHITE);

            JLabel questionLabel = new JLabel("<html><b>" + questionNumber + ". " + question.getQuestion() + "</b></html>");
            questionPanel.add(questionLabel);
            questionPanel.add(Box.createRigidArea(new Dimension(0, 5)));

            JComboBox<String> answerCombo = new JComboBox<>();
            answerCombo.addItem("-- Select Answer --");
            for (String option : question.getOption()) {
                answerCombo.addItem(option);
            }
            questionPanel.add(answerCombo);
            answerBoxes.add(answerCombo);

            mainPanel.add(questionPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            questionNumber++;
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Submit Quiz");
        JButton cancelButton = new JButton("Cancel");

        submitButton.addActionListener(e -> {
            // Validate that all questions are answered
            for (int i = 0; i < answerBoxes.size(); i++) {
                if (answerBoxes.get(i).getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(quizDialog,
                            "Please answer question " + (i + 1), "Incomplete Quiz",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Collect answers
            ArrayList<Integer> answers = new ArrayList<>();
            for (JComboBox<String> comboBox : answerBoxes) {
                answers.add(comboBox.getSelectedIndex() - 1); // Subtract 1 because of the "Select Answer" option
            }

            // Show loading message
           // JOptionPane.showMessageDialog(quizDialog, "Submitting quiz...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);

            // Submit quiz
            boolean success = QuizService.submitQuiz(currentStudent.getUserId(), courseId, lesson.getLessonId(), answers);

            if (success) {
                // Refresh the current student data from the database
                refreshCurrentStudent();

                // Get the updated progress to show score using the correct method signature

                LessonProgress updatedProgress = currentStudent.getLessonProgress().get(courseId+lesson.getLessonId());
                System.out.println(currentStudent.getLessonProgress());

                System.out.println(updatedProgress);

                String message;
                if (updatedProgress != null && updatedProgress.isCompleted()) {
                    message = "Congratulations! You passed the quiz!\n" +
                            "Score: " + updatedProgress.getQuizScore() + "%\n" +
                            "Attempts: " + updatedProgress.getAttempts() + "\n\n" +
                            "You can now proceed to the next lesson.";
                } else if (updatedProgress != null) {
                    message = "Quiz submitted!\n" +
                            "Score: " + updatedProgress.getQuizScore() + "%\n" +
                            "Attempts: " + updatedProgress.getAttempts() + "\n\n" +
                            "You need 50% to pass. Please retry to unlock the next lesson.";
                } else {
                    message = "Quiz submitted but progress data is not available.\n" +
                            "Please check your progress in the lessons list.";
                }

                JOptionPane.showMessageDialog(quizDialog, message, "Quiz Submitted",
                        updatedProgress != null && updatedProgress.isCompleted() ?
                                JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

                quizDialog.dispose();
                parentDialog.dispose();
                viewCourseLessons(); // Refresh the lessons view
            } else {
                JOptionPane.showMessageDialog(quizDialog,
                        "Failed to submit quiz. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> quizDialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        quizDialog.add(scrollPane);
        quizDialog.setVisible(true);
    }

    private void viewCertificate() {
        int selectedRow = certificatesTable.getSelectedRow();
        if (selectedRow >= 0) {
            String certificateId = (String) certificatesTable.getValueAt(selectedRow, 0);
            String courseName = (String) certificatesTable.getValueAt(selectedRow, 1);

            JDialog certificateDialog = new JDialog(this, "Certificate - " + courseName, true);
            certificateDialog.setSize(500, 400);
            certificateDialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Certificate of Completion", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

            JTextArea detailsArea = new JTextArea();
            detailsArea.setEditable(false);
            detailsArea.setFont(new Font("Arial", Font.PLAIN, 14));
            detailsArea.setText(
                    "This certifies that\n\n" +
                            currentStudent.getUsername() + "\n\n" +
                            "has successfully completed the course\n\n" +
                            courseName + "\n\n" +
                            "Certificate ID: " + certificateId + "\n" +
                            "Issue Date: " + certificatesTable.getValueAt(selectedRow, 2)
            );
            detailsArea.setAlignmentX(JTextArea.CENTER_ALIGNMENT);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> certificateDialog.dispose());

            panel.add(titleLabel, BorderLayout.NORTH);
            panel.add(detailsArea, BorderLayout.CENTER);
            panel.add(closeButton, BorderLayout.SOUTH);

            certificateDialog.add(panel);
            certificateDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a certificate first", "No Selection", JOptionPane.WARNING_MESSAGE);
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