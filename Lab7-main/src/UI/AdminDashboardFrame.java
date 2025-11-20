package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import Logic.AdminService;
import Model.*;
import Database.JSONDatabaseManager;

public class AdminDashboardFrame extends JFrame{
    private JTable coursesTable;
    private JLabel welcomeLabel;
    private Admin currentAdmin;
    public AdminDashboardFrame(User user)
    {
        if(user instanceof Admin)
            this.currentAdmin = (Admin) user;
        else
        {
            System.out.println("this user is not an admin! ");
            JOptionPane.showMessageDialog(this,"Error: user is not an admin!","Error",JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        initializeUI();
        loadAdminData();
    }
    private void initializeUI()
    {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200,800);
        setLocationRelativeTo(null);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        JPanel headerPanel = createHeaderPanel();
        main.add(headerPanel,BorderLayout.NORTH);

        JPanel coursesPanel = createCoursesPanel();
        main.add(coursesPanel,BorderLayout.CENTER);

        add(main);
    }
    private JPanel createHeaderPanel()
    {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.BLACK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,10));
        headerPanel.setPreferredSize(new Dimension(getWidth(),60));
        welcomeLabel = new JLabel("Welcome admin!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD,18));
        welcomeLabel.setForeground(Color.WHITE);
        JButton logoutButton = new JButton("Logout");
        styleHeaderButton(logoutButton);
        logoutButton.addActionListener(e->{
            dispose();
            new LoginFrame().setVisible(true);
        });
        headerPanel.add(welcomeLabel,BorderLayout.WEST);
        headerPanel.add(logoutButton,BorderLayout.EAST);
        return headerPanel;
    }
    private JPanel createCoursesPanel()
    {
        JPanel coursePanel = new JPanel(new BorderLayout());
        coursePanel.setBackground(Color.WHITE);
        coursePanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        String [] col = {"Course ID","Course Name","Instructor", "Enrolled students","Lessons","status"};
        DefaultTableModel model = new DefaultTableModel(col,0)
        {
            public boolean isCellEditable(int row,int column) {return false;}
        };
        coursesTable = new JTable(model);
        coursesTable.setSelectionBackground(Color.GRAY);
        coursesTable.setSelectionForeground(Color.WHITE);
        coursesTable.setBackground(Color.DARK_GRAY);
        coursesTable.setForeground(Color.WHITE);
        coursesTable.getTableHeader().setBackground(Color.BLACK);
        coursesTable.getTableHeader().setForeground(Color.WHITE);
        coursesTable.setRowHeight(30);
        JScrollPane js = new JScrollPane(coursesTable);
        coursePanel.add(js,BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.BLACK);
        JButton approved = new JButton("Approve");
        JButton rejected = new JButton("Reject");
        JButton refreshed = new JButton("Refresh");
        styleActionButton(approved);
        styleActionButton(rejected);
        styleActionButton(refreshed);
        approved.addActionListener(e-> approveSelectedCourse());
        rejected.addActionListener(e-> rejectSelectedCourse());
        refreshed.addActionListener(e-> refreshCourseTable());
        buttonPanel.add(approved);
        buttonPanel.add(rejected);
        buttonPanel.add(refreshed);
        coursePanel.add(buttonPanel,BorderLayout.SOUTH);
        return coursePanel;
    }

    private void loadAdminData()
    {
        welcomeLabel.setText("Welcome, " + currentAdmin.getUsername()+"!");
        refreshCourseTable();
    }
    private void approveSelectedCourse()
    {
        int selected = coursesTable.getSelectedRow();
        if(selected>=0)
        {
            String courseId = (String) coursesTable.getValueAt(selected,0);
            boolean flag = AdminService.approveCourse(courseId);
            if(flag)
            {
                JOptionPane.showMessageDialog(this,"Course approved successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
                refreshCourseTable();
            }
            else
            {
                JOptionPane.showMessageDialog(this,"Course is not pending!","Failed",JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this,"Please select a course first","No selection",JOptionPane.WARNING_MESSAGE);
        }
    }
    private void rejectSelectedCourse()
    {
        int selected = coursesTable.getSelectedRow();
        if(selected>=0)
        {
            String courseId = (String) coursesTable.getValueAt(selected,0);
            boolean flag = AdminService.rejectCourse(courseId);
            if(flag)
            {
                JOptionPane.showMessageDialog(this,"Course rejected successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
                refreshCourseTable();
            }
            else
            {
                JOptionPane.showMessageDialog(this,"Course is not pending!","Failed",JOptionPane.ERROR_MESSAGE);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this,"Please select a course first","No selection",JOptionPane.WARNING_MESSAGE);
        }
    }
    private void refreshCourseTable()
    {
        DefaultTableModel model = (DefaultTableModel) coursesTable.getModel();
        model.setRowCount(0);

        ArrayList<Course> courses = JSONDatabaseManager.loadCourses();
        ArrayList<User> users = JSONDatabaseManager.loadUsers();
        for(Course c: courses)
        {
            String InstructorName = "Nil";
            for(User user:users)
            {
                if(user.getUserId().equals(c.getInstructorId()))
                {
                    InstructorName = user.getUsername();
                    break;
                }
            }
            model.addRow(new Object[]
                    {
                            c.getCourseId(),
                            c.getTitle(),
                            InstructorName,
                            c.getStudents().size(),
                            c.getLessons().size(),
                            c.getApprovalStatusString()
                    });
        }
    }
    private void styleHeaderButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }
    private void styleActionButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
}
