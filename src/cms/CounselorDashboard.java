/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;


public class CounselorDashboard extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CounselorDashboard.class.getName());

    private Counselor currentCounselor;
    private FileManager fileManager;
    
    private DefaultTableModel schedulemodel = new DefaultTableModel();
    private DefaultTableModel appointmentmodel = new DefaultTableModel();
    private DefaultTableModel recordsmodel = new DefaultTableModel();
    
    private String scheduleColumnName[] = {"Schedule ID", "Day", "Start Time", "End Time", "Status"};
    private String appointmentColumnName[] = {"Appt ID", "Student ID", "Date", "Time", "Type", "Status"};
    private String recordsColumnName[] = {"Record ID", "Appt ID", "Student Name", "Notes", "Recommendations"};

    
    /**
     * Creates new form CounselorDashboard
     */
    public CounselorDashboard() {
        
        initTableModels();
        initComponents();
        
    }
    
    public CounselorDashboard(Counselor counselor, FileManager fileManager) {
        this.currentCounselor = counselor;
        this.fileManager = (fileManager != null) ? fileManager : new FileManager();
        
        setTitle(currentCounselor.getDashboardTitle() + " - " + currentCounselor.getName());
        
        initComponents();
        initTableModels();
        loadCounselorProfile();
        loadCounselorData();
        loadCounselorFirstName();
    }
    
    private void initTableModels() {
        schedulemodel.setColumnIdentifiers(scheduleColumnName);
        appointmentmodel.setColumnIdentifiers(appointmentColumnName);
        recordsmodel.setColumnIdentifiers(recordsColumnName);
        
    }

    
    private void loadCounselorProfile() {
        if (currentCounselor != null) {
            useridTf.setText(currentCounselor.getUserID());
            useridTf.setEditable(false);

            usernameTf.setText(currentCounselor.getUsername());
            usernameTf.setEditable(false);

            nameTf.setText(currentCounselor.getName());
            nameTf.setEditable(false); // Names are typically managed by Admin

            passwordTf.setText(currentCounselor.getPassword());
        }
    }
    
    private void loadCounselorFirstName(){
       if(currentCounselor != null)  {
           String fullName = currentCounselor.getName();
           String firstName = extractFirstName(fullName);
           
           slidebarnameTf.setText(firstName);
           welcomenameTf.setText("Welcome, " + firstName + "!");
       }    
    }
    
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Counselor";
        }

        String[] parts = fullName.trim().split("\\s+");

        // If the name starts with a title like "Dr.", "Mr.", "Ms.", keep the title + first name
        if (parts.length > 1 && (parts[0].equalsIgnoreCase("Dr.") || 
                                 parts[0].equalsIgnoreCase("Mr.") || 
                                 parts[0].equalsIgnoreCase("Ms.") || 
                                 parts[0].equalsIgnoreCase("Prof."))) {
            return parts[0] + " " + parts[1];
        }
        
        return parts[0];
    }
           
    public void loadCounselorData() {
        if (currentCounselor == null || fileManager == null) return;

        // 1. Load Schedule
        schedulemodel.setRowCount(0);
        ArrayList<Schedule> mySchedules = currentCounselor.viewMyRoster(fileManager);
        for (Schedule s : mySchedules) {
            schedulemodel.addRow(new Object[]{
                s.getScheduleID(), s.getDay(), s.getStartTime(), s.getEndTime(), s.getStatus()
            });
        }

        // 2. Load Appointments with status filter
        appointmentmodel.setRowCount(0);
        ArrayList<Appointment> myAppts = currentCounselor.viewMyAppointments(fileManager);

        String selectedStatus = (String) filterstatusComboBox.getSelectedItem();
            
        LocalDateTime now = LocalDateTime.now();
 
        for (Appointment a : myAppts) {
            if (selectedStatus != null && !selectedStatus.equals("All")) {
                if (!a.getStatus().equals(selectedStatus)) {
                    continue;
                }
            }

            // Skip appointments whose date/time has already passed
            try {
                LocalDate apptDate = LocalDate.parse(a.getDate());
                LocalTime apptTime = LocalTime.parse(a.getTime());
                LocalDateTime apptDateTime = LocalDateTime.of(apptDate, apptTime);
                if (apptDateTime.isBefore(now)) {
                    continue;
                }
            } catch (Exception e) {
                // If date/time is malformed, still show it so it isn't silently hidden
                System.out.println("Could not parse date/time for appointment " + a.getAppointmentID());
            }

            appointmentmodel.addRow(new Object[]{
                a.getAppointmentID(), a.getStudentID(), a.getDate(), a.getTime(), a.getType(), a.getStatus()
            });
        }

        // 4. Load Consultation Records
        loadConsultationRecords();
    }
    
    private void loadConsultationRecords() {
        recordsmodel.setRowCount(0);
        if (currentCounselor == null || fileManager == null) return;

        ArrayList<ConsultationRecord> myRecords = currentCounselor.getRecordsForStudent("ALL", fileManager);
        for (ConsultationRecord record : myRecords) {
            String studentName = getStudentName(record.getAppointmentID());
            recordsmodel.addRow(new Object[]{
                record.getRecordID(), record.getAppointmentID(), studentName, record.getNotes(), record.getRecommendations()
            });
        }
    } 
    
    
    
    private String getStudentName(String appointmentId) {
        ArrayList<Appointment> myAppts = currentCounselor.viewMyAppointments(fileManager);
        for (Appointment a : myAppts) {
            if (a.getAppointmentID().equals(appointmentId)) {
                ArrayList<User> allUsers = fileManager.getAllUsers();
                for (User u : allUsers) {
                    if (u.getUserID().equals(a.getStudentID()) && u instanceof Student) {
                        return u.getName();
                    }
                }
                return a.getStudentID();
            }
        }
        return "Unknown";
    }
    
    private void updateAppointmentStatus(String newStatus) {
        int selectedRow = appointmentTbl.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please select an appointment from the table.");
            return;
        }

        String apptID = appointmentmodel.getValueAt(selectedRow, 0).toString();
        ArrayList<Appointment> allAppts = fileManager.getAllAppointments();

        for (Appointment a : allAppts) {
            if (a.getAppointmentID().equals(apptID)) {
                a.setStatus(newStatus);
                break;
            }
        }

        fileManager.saveAllAppointments(allAppts);
        loadCounselorData();
        javax.swing.JOptionPane.showMessageDialog(this, "Appointment " + apptID + " marked as " + newStatus + ".");
    }
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        slidebarPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        slidebarnameTf = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        scheduleBtn = new javax.swing.JButton();
        appointmentBtn = new javax.swing.JButton();
        recordsBtn = new javax.swing.JButton();
        profileBtn = new javax.swing.JButton();
        logoutBtn = new javax.swing.JButton();
        dashboardBtn = new javax.swing.JButton();
        contentPanel = new javax.swing.JPanel();
        dashboardCard = new javax.swing.JPanel();
        welcomenameTf = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        scheduleCard = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        myscheduleTbl = new javax.swing.JTable();
        welcomenameTf1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        schdRefreshBtn = new javax.swing.JButton();
        appointmentCard = new javax.swing.JPanel();
        appointmentTitleLbl = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        filterstatusComboBox = new javax.swing.JComboBox<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        appointmentTbl = new javax.swing.JTable();
        apptAcceptBtn = new javax.swing.JButton();
        apptCancelBtn = new javax.swing.JButton();
        apptRefreshBtn = new javax.swing.JButton();
        recordsCard = new javax.swing.JPanel();
        recordsTitleLbl = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        recordsTbl = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        useridLbl1 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        recordIDTf = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        recommendationsTA = new javax.swing.JTextArea();
        recommendationLbl = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        sessionNotesTA = new javax.swing.JTextArea();
        recordsRefreshBtn1 = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        newRecordsBtn = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        appointmentIDTf = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        studentnameTf = new javax.swing.JTextField();
        profileCard = new javax.swing.JPanel();
        recordsTitleLbl1 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        useridLbl = new javax.swing.JLabel();
        nameLbl = new javax.swing.JLabel();
        usernameLbl = new javax.swing.JLabel();
        passwordLbl = new javax.swing.JLabel();
        usernameTf = new javax.swing.JTextField();
        passwordTf = new javax.swing.JTextField();
        useridTf = new javax.swing.JTextField();
        nameTf = new javax.swing.JTextField();
        changepwdBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(240, 244, 248));
        setResizable(false);

        mainPanel.setBackground(new java.awt.Color(240, 244, 248));
        mainPanel.setPreferredSize(new java.awt.Dimension(1000, 650));
        mainPanel.setLayout(new java.awt.BorderLayout());

        slidebarPanel.setBackground(new java.awt.Color(10, 25, 47));
        slidebarPanel.setPreferredSize(new java.awt.Dimension(200, 625));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cms/images/CMS_Icon_70x70.png"))); // NOI18N
        jLabel1.setText("CMS");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jLabel1.setIconTextGap(10);

        slidebarnameTf.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        slidebarnameTf.setForeground(new java.awt.Color(255, 255, 255));
        slidebarnameTf.setText("Dr. Smith");

        jLabel3.setBackground(new java.awt.Color(148, 163, 184));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(148, 163, 184));
        jLabel3.setText("Counselor");

        scheduleBtn.setBackground(new java.awt.Color(30, 41, 59));
        scheduleBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        scheduleBtn.setForeground(new java.awt.Color(226, 232, 240));
        scheduleBtn.setText("My Schedule");
        scheduleBtn.setPreferredSize(new java.awt.Dimension(170, 36));
        scheduleBtn.addActionListener(this::scheduleBtnActionPerformed);

        appointmentBtn.setBackground(new java.awt.Color(30, 41, 59));
        appointmentBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        appointmentBtn.setForeground(new java.awt.Color(226, 232, 240));
        appointmentBtn.setText("Appointments");
        appointmentBtn.setPreferredSize(new java.awt.Dimension(170, 36));
        appointmentBtn.addActionListener(this::appointmentBtnActionPerformed);

        recordsBtn.setBackground(new java.awt.Color(30, 41, 59));
        recordsBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        recordsBtn.setForeground(new java.awt.Color(226, 232, 240));
        recordsBtn.setText("Consultation Records");
        recordsBtn.setPreferredSize(new java.awt.Dimension(170, 36));
        recordsBtn.addActionListener(this::recordsBtnActionPerformed);

        profileBtn.setBackground(new java.awt.Color(30, 41, 59));
        profileBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        profileBtn.setForeground(new java.awt.Color(226, 232, 240));
        profileBtn.setText("My Profile");
        profileBtn.setPreferredSize(new java.awt.Dimension(170, 36));
        profileBtn.addActionListener(this::profileBtnActionPerformed);

        logoutBtn.setBackground(new java.awt.Color(220, 38, 38));
        logoutBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        logoutBtn.setForeground(new java.awt.Color(255, 255, 255));
        logoutBtn.setText("Log out");
        logoutBtn.setPreferredSize(new java.awt.Dimension(170, 36));
        logoutBtn.addActionListener(this::logoutBtnActionPerformed);

        dashboardBtn.setBackground(new java.awt.Color(37, 99, 235));
        dashboardBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        dashboardBtn.setForeground(new java.awt.Color(255, 255, 255));
        dashboardBtn.setText("Dashboard");
        dashboardBtn.setPreferredSize(new java.awt.Dimension(170, 36));
        dashboardBtn.addActionListener(this::dashboardBtnActionPerformed);

        javax.swing.GroupLayout slidebarPanelLayout = new javax.swing.GroupLayout(slidebarPanel);
        slidebarPanel.setLayout(slidebarPanelLayout);
        slidebarPanelLayout.setHorizontalGroup(
            slidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(slidebarPanelLayout.createSequentialGroup()
                .addGroup(slidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(slidebarPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(slidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(slidebarnameTf)
                            .addComponent(jLabel3)))
                    .addGroup(slidebarPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addGroup(slidebarPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(slidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(slidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(scheduleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(recordsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(profileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(logoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(appointmentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        slidebarPanelLayout.setVerticalGroup(
            slidebarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(slidebarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(slidebarnameTf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(35, 35, 35)
                .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scheduleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(appointmentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(recordsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(profileBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(105, 105, 105))
        );

        mainPanel.add(slidebarPanel, java.awt.BorderLayout.WEST);

        contentPanel.setPreferredSize(new java.awt.Dimension(800, 650));
        contentPanel.setLayout(new java.awt.CardLayout());

        dashboardCard.setBackground(new java.awt.Color(240, 244, 248));
        dashboardCard.setAutoscrolls(true);
        dashboardCard.setName(""); // NOI18N
        dashboardCard.setPreferredSize(new java.awt.Dimension(800, 650));

        welcomenameTf.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        welcomenameTf.setForeground(new java.awt.Color(29, 78, 216));
        welcomenameTf.setText("Welcome, Counselor!");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(100, 116, 139));
        jLabel6.setText("Use the menu on the left to manage your schedule and appointments.");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(380, 320));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(29, 78, 216));
        jLabel7.setText("Counselor Modules");

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setText("1. Manage Schedule & Slots\n2. Confirm / Cancel Student Appointments\n3. View Consultation Records\n4. Create New Session Records\n5. Update Counselor Profile");
        jScrollPane2.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(320, 320));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(29, 78, 216));
        jLabel8.setText("Overview");

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jTextArea2.setText("Welcome to the Counseling Management System.\n\n• View your daily schedule roster.\n• Manage student session requests.\n• Log notes and recommendations after sessions.\n\nSelect a module from the sidebar to begin.");
        jScrollPane3.setViewportView(jTextArea2);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout dashboardCardLayout = new javax.swing.GroupLayout(dashboardCard);
        dashboardCard.setLayout(dashboardCardLayout);
        dashboardCardLayout.setHorizontalGroup(
            dashboardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardCardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(dashboardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboardCardLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6)
                    .addComponent(welcomenameTf))
                .addContainerGap(140, Short.MAX_VALUE))
        );
        dashboardCardLayout.setVerticalGroup(
            dashboardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardCardLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(welcomenameTf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addGroup(dashboardCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(239, Short.MAX_VALUE))
        );

        contentPanel.add(dashboardCard, "dashboardCard");

        scheduleCard.setBackground(new java.awt.Color(240, 244, 248));
        scheduleCard.setForeground(new java.awt.Color(29, 78, 216));
        scheduleCard.setName(""); // NOI18N
        scheduleCard.setPreferredSize(new java.awt.Dimension(800, 650));

        myscheduleTbl.setModel(schedulemodel);
        jScrollPane1.setViewportView(myscheduleTbl);

        welcomenameTf1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        welcomenameTf1.setForeground(new java.awt.Color(29, 78, 216));
        welcomenameTf1.setText("My Schedule Roster");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(100, 116, 139));
        jLabel9.setText("View your schedule roster");

        schdRefreshBtn.setBackground(new java.awt.Color(100, 116, 139));
        schdRefreshBtn.setForeground(new java.awt.Color(255, 255, 255));
        schdRefreshBtn.setText("Refresh");
        schdRefreshBtn.addActionListener(this::schdRefreshBtnActionPerformed);

        javax.swing.GroupLayout scheduleCardLayout = new javax.swing.GroupLayout(scheduleCard);
        scheduleCard.setLayout(scheduleCardLayout);
        scheduleCardLayout.setHorizontalGroup(
            scheduleCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scheduleCardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scheduleCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(scheduleCardLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(schdRefreshBtn))
                    .addComponent(welcomenameTf1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 750, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(116, 116, 116))
        );
        scheduleCardLayout.setVerticalGroup(
            scheduleCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scheduleCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(welcomenameTf1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(scheduleCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(schdRefreshBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(89, Short.MAX_VALUE))
        );

        contentPanel.add(scheduleCard, "scheduleCard");

        appointmentCard.setBackground(new java.awt.Color(240, 244, 248));
        appointmentCard.setName(""); // NOI18N
        appointmentCard.setPreferredSize(new java.awt.Dimension(800, 650));

        appointmentTitleLbl.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        appointmentTitleLbl.setForeground(new java.awt.Color(29, 78, 216));
        appointmentTitleLbl.setText("Manage My Appointments");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(100, 116, 139));
        jLabel13.setText("Review, accept, or reschedule incoming consultation requests.");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(100, 116, 139));
        jLabel2.setText("Filter Status: ");

        filterstatusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Pending", "Confirmed", "Cancelled", "Completed" }));
        filterstatusComboBox.setPreferredSize(new java.awt.Dimension(130, 28));
        filterstatusComboBox.addActionListener(this::filterstatusComboBoxActionPerformed);

        jScrollPane4.setPreferredSize(new java.awt.Dimension(750, 320));

        appointmentTbl.setModel(appointmentmodel);
        jScrollPane4.setViewportView(appointmentTbl);

        apptAcceptBtn.setBackground(new java.awt.Color(16, 185, 129));
        apptAcceptBtn.setForeground(new java.awt.Color(255, 255, 255));
        apptAcceptBtn.setText("Accept Appointment");
        apptAcceptBtn.addActionListener(this::apptAcceptBtnActionPerformed);

        apptCancelBtn.setBackground(new java.awt.Color(220, 38, 38));
        apptCancelBtn.setForeground(new java.awt.Color(255, 255, 255));
        apptCancelBtn.setText("Cancel / Decline");
        apptCancelBtn.addActionListener(this::apptCancelBtnActionPerformed);

        apptRefreshBtn.setBackground(new java.awt.Color(100, 116, 139));
        apptRefreshBtn.setForeground(new java.awt.Color(255, 255, 255));
        apptRefreshBtn.setText("Refresh");
        apptRefreshBtn.addActionListener(this::apptRefreshBtnActionPerformed);

        javax.swing.GroupLayout appointmentCardLayout = new javax.swing.GroupLayout(appointmentCard);
        appointmentCard.setLayout(appointmentCardLayout);
        appointmentCardLayout.setHorizontalGroup(
            appointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appointmentCardLayout.createSequentialGroup()
                .addGroup(appointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(appointmentCardLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(appointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(appointmentTitleLbl)
                            .addGroup(appointmentCardLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(filterstatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(appointmentCardLayout.createSequentialGroup()
                        .addGap(150, 150, 150)
                        .addComponent(apptAcceptBtn)
                        .addGap(18, 18, 18)
                        .addComponent(apptCancelBtn)
                        .addGap(18, 18, 18)
                        .addComponent(apptRefreshBtn)))
                .addContainerGap(116, Short.MAX_VALUE))
        );
        appointmentCardLayout.setVerticalGroup(
            appointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appointmentCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(appointmentTitleLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addGroup(appointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(filterstatusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(appointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(apptAcceptBtn)
                    .addComponent(apptCancelBtn)
                    .addComponent(apptRefreshBtn))
                .addContainerGap(166, Short.MAX_VALUE))
        );

        contentPanel.add(appointmentCard, "appointmentCard");

        recordsCard.setBackground(new java.awt.Color(240, 244, 248));
        recordsCard.setName(""); // NOI18N
        recordsCard.setPreferredSize(new java.awt.Dimension(800, 650));

        recordsTitleLbl.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        recordsTitleLbl.setForeground(new java.awt.Color(29, 78, 216));
        recordsTitleLbl.setText("Consultation Records & Session Notes");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(100, 116, 139));
        jLabel14.setText("View student history and log post-session notes.");

        jScrollPane5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        jScrollPane5.setPreferredSize(new java.awt.Dimension(450, 380));
        jScrollPane5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jScrollPane5MouseClicked(evt);
            }
        });

        recordsTbl.setModel(recordsmodel);
        recordsTbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                recordsTblMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(recordsTbl);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        useridLbl1.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        useridLbl1.setForeground(new java.awt.Color(29, 78, 216));
        useridLbl1.setText("Record Details");

        jLabel18.setForeground(new java.awt.Color(55, 65, 81));
        jLabel18.setText("Record ID: ");

        recordIDTf.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        recordIDTf.addActionListener(this::recordIDTfActionPerformed);

        jLabel19.setForeground(new java.awt.Color(55, 65, 81));
        jLabel19.setText("Session Notes: ");

        recommendationsTA.setColumns(20);
        recommendationsTA.setRows(5);
        jScrollPane6.setViewportView(recommendationsTA);

        recommendationLbl.setForeground(new java.awt.Color(55, 65, 81));
        recommendationLbl.setText("Recommendations: ");

        sessionNotesTA.setColumns(20);
        sessionNotesTA.setRows(5);
        jScrollPane7.setViewportView(sessionNotesTA);

        recordsRefreshBtn1.setBackground(new java.awt.Color(100, 116, 139));
        recordsRefreshBtn1.setForeground(new java.awt.Color(255, 255, 255));
        recordsRefreshBtn1.setText("Refresh");
        recordsRefreshBtn1.addActionListener(this::recordsRefreshBtn1ActionPerformed);

        updateBtn.setBackground(new java.awt.Color(37, 99, 235));
        updateBtn.setForeground(new java.awt.Color(255, 255, 255));
        updateBtn.setText("Update");
        updateBtn.addActionListener(this::updateBtnActionPerformed);

        clearBtn.setBackground(new java.awt.Color(220, 38, 38));
        clearBtn.setForeground(new java.awt.Color(255, 255, 255));
        clearBtn.setText("Clear");
        clearBtn.addActionListener(this::clearBtnActionPerformed);

        jLabel20.setForeground(new java.awt.Color(55, 65, 81));
        jLabel20.setText("Select a record above to edit.");

        newRecordsBtn.setBackground(new java.awt.Color(37, 99, 235));
        newRecordsBtn.setForeground(new java.awt.Color(255, 255, 255));
        newRecordsBtn.setText("New Records");
        newRecordsBtn.addActionListener(this::newRecordsBtnActionPerformed);

        jLabel21.setForeground(new java.awt.Color(55, 65, 81));
        jLabel21.setText("Appointment ID: ");

        appointmentIDTf.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        appointmentIDTf.addActionListener(this::appointmentIDTfActionPerformed);

        jLabel4.setForeground(new java.awt.Color(55, 65, 81));
        jLabel4.setText("Student Name: ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(recordIDTf, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addGap(18, 18, 18)
                                .addComponent(appointmentIDTf, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel20)
                        .addGap(112, 112, 112))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(useridLbl1)
                                        .addGap(217, 217, 217)
                                        .addComponent(newRecordsBtn))
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(14, 14, 14)
                                .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(recordsRefreshBtn1))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(110, 110, 110)
                                        .addComponent(studentnameTf, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel19))
                                .addGap(42, 42, 42)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(recommendationLbl)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useridLbl1)
                    .addComponent(recordsRefreshBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newRecordsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(recordIDTf, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(appointmentIDTf, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(studentnameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(recommendationLbl, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout recordsCardLayout = new javax.swing.GroupLayout(recordsCard);
        recordsCard.setLayout(recordsCardLayout);
        recordsCardLayout.setHorizontalGroup(
            recordsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(recordsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel14)
                    .addComponent(recordsTitleLbl)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(147, Short.MAX_VALUE))
        );
        recordsCardLayout.setVerticalGroup(
            recordsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(recordsCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recordsTitleLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(88, 88, 88))
        );

        contentPanel.add(recordsCard, "recordsCard");

        profileCard.setBackground(new java.awt.Color(240, 244, 248));
        profileCard.setForeground(new java.awt.Color(240, 244, 248));
        profileCard.setName(""); // NOI18N
        profileCard.setPreferredSize(new java.awt.Dimension(800, 650));

        recordsTitleLbl1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        recordsTitleLbl1.setForeground(new java.awt.Color(29, 78, 216));
        recordsTitleLbl1.setText("My Profile & Account Settings");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(100, 116, 139));
        jLabel15.setText("View and update your personal account information.");

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        useridLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        useridLbl.setForeground(new java.awt.Color(29, 78, 216));
        useridLbl.setText("User ID: ");

        nameLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        nameLbl.setForeground(new java.awt.Color(29, 78, 216));
        nameLbl.setText("Name: ");

        usernameLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        usernameLbl.setForeground(new java.awt.Color(29, 78, 216));
        usernameLbl.setText("Username: ");

        passwordLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        passwordLbl.setForeground(new java.awt.Color(29, 78, 216));
        passwordLbl.setText("Password: ");

        usernameTf.setEditable(false);

        passwordTf.addActionListener(this::passwordTfActionPerformed);

        useridTf.setEditable(false);
        useridTf.addActionListener(this::useridTfActionPerformed);

        nameTf.setEditable(false);

        changepwdBtn.setBackground(new java.awt.Color(37, 99, 235));
        changepwdBtn.setForeground(new java.awt.Color(255, 255, 255));
        changepwdBtn.setText("Change Password");
        changepwdBtn.addActionListener(this::changepwdBtnActionPerformed);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usernameLbl)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLbl)
                            .addComponent(useridLbl)
                            .addComponent(passwordLbl))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useridTf, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(usernameTf)
                            .addComponent(nameTf)
                            .addComponent(passwordTf)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(changepwdBtn)))
                .addGap(27, 27, 27))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useridLbl)
                    .addComponent(useridTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLbl)
                    .addComponent(usernameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLbl)
                    .addComponent(nameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLbl)
                    .addComponent(passwordTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(changepwdBtn)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout profileCardLayout = new javax.swing.GroupLayout(profileCard);
        profileCard.setLayout(profileCardLayout);
        profileCardLayout.setHorizontalGroup(
            profileCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profileCardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(profileCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(recordsTitleLbl1)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(525, Short.MAX_VALUE))
        );
        profileCardLayout.setVerticalGroup(
            profileCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(profileCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(recordsTitleLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(285, Short.MAX_VALUE))
        );

        contentPanel.add(profileCard, "profileCard");

        mainPanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void scheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scheduleBtnActionPerformed
        // TODO add your handling code here:
        java.awt.CardLayout cl = (java.awt.CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "scheduleCard");
        setActiveButton(scheduleBtn);
    }//GEN-LAST:event_scheduleBtnActionPerformed

    private void appointmentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appointmentBtnActionPerformed
        // TODO add your handling code here:
        java.awt.CardLayout cl = (java.awt.CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "appointmentCard");
        setActiveButton(appointmentBtn);
    }//GEN-LAST:event_appointmentBtnActionPerformed

    private void recordsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordsBtnActionPerformed
        // TODO add your handling code here:
        java.awt.CardLayout cl = (java.awt.CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "recordsCard");
        setActiveButton(recordsBtn);
        loadConsultationRecords();
    }//GEN-LAST:event_recordsBtnActionPerformed

    private void profileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileBtnActionPerformed
        // TODO add your handling code here:
        java.awt.CardLayout cl = (java.awt.CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "profileCard");
        setActiveButton(profileBtn);
    }//GEN-LAST:event_profileBtnActionPerformed

    private void logoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutBtnActionPerformed
        // TODO add your handling code here:
       int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Open the login page
        new LoginPage().setVisible(true);

        // Close this window
        this.dispose();
    }//GEN-LAST:event_logoutBtnActionPerformed

    private void dashboardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardBtnActionPerformed
        // TODO add your handling code here:
        java.awt.CardLayout cl = (java.awt.CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "dashboardCard");
        setActiveButton(dashboardBtn);
    }//GEN-LAST:event_dashboardBtnActionPerformed

    private void filterstatusComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterstatusComboBoxActionPerformed
        // TODO add your handling code here:
        loadCounselorData();
    }//GEN-LAST:event_filterstatusComboBoxActionPerformed

    private void apptAcceptBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apptAcceptBtnActionPerformed
        // TODO add your handling code here:
         updateAppointmentStatus("Confirmed");
        
        loadCounselorData();
    }//GEN-LAST:event_apptAcceptBtnActionPerformed

    private void apptCancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apptCancelBtnActionPerformed
        // TODO add your handling code here:
        updateAppointmentStatus("Cancelled");
        
        loadCounselorData();
    }//GEN-LAST:event_apptCancelBtnActionPerformed
       
    private void apptRefreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apptRefreshBtnActionPerformed
        // TODO add your handling code here:
        loadCounselorData();
    }//GEN-LAST:event_apptRefreshBtnActionPerformed

    private void schdRefreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schdRefreshBtnActionPerformed
        // TODO add your handling code here:
        loadCounselorData();
    }//GEN-LAST:event_schdRefreshBtnActionPerformed

    private void passwordTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordTfActionPerformed

    private void changepwdBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changepwdBtnActionPerformed
        // TODO add your handling code here:
        String newPassword = passwordTf.getText().trim();
        if (newPassword.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Password field cannot be empty.");
            return;
        }

        currentCounselor.setPassword(newPassword);

        // Update users file
        ArrayList<User> allUsers = fileManager.getAllUsers();
        for (User u : allUsers) {
            if (u.getUserID().equals(currentCounselor.getUserID())) {
                u.setPassword(newPassword);
                break;
            }
        }
        fileManager.saveAllUsers(allUsers);
        javax.swing.JOptionPane.showMessageDialog(this, "Password updated successfully!");
    }//GEN-LAST:event_changepwdBtnActionPerformed

    private void useridTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useridTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useridTfActionPerformed

    private void recordIDTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordIDTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_recordIDTfActionPerformed

    private void recordsRefreshBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordsRefreshBtn1ActionPerformed
        // TODO add your handling code here:
        loadConsultationRecords();
    }//GEN-LAST:event_recordsRefreshBtn1ActionPerformed

    private void newRecordsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRecordsBtnActionPerformed
        // TODO add your handling code here:
        NewRecord recordForm = new NewRecord(currentCounselor, fileManager, this);
        recordForm.setVisible(true);
    }//GEN-LAST:event_newRecordsBtnActionPerformed

    private void appointmentIDTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appointmentIDTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_appointmentIDTfActionPerformed

    private void recordsTblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_recordsTblMouseClicked
        // TODO add your handling code here:
        int row = recordsTbl.getSelectedRow();
        if (row == -1) return;

        recordIDTf.setText(recordsmodel.getValueAt(row, 0).toString());
        appointmentIDTf.setText(recordsmodel.getValueAt(row, 1).toString());
        studentnameTf.setText(recordsmodel.getValueAt(row, 2).toString());
        sessionNotesTA.setText(recordsmodel.getValueAt(row, 3).toString()); 
        recommendationsTA.setText(recordsmodel.getValueAt(row, 4).toString()); 
    }//GEN-LAST:event_recordsTblMouseClicked

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        // TODO add your handling code here:
        String recordID = recordIDTf.getText().trim();
        if (recordID.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please select a record from the table first.");
            return;
        }

        ArrayList<ConsultationRecord> allRecords = fileManager.getAllRecords();
        boolean found = false;
        for (ConsultationRecord r : allRecords) {
            if (r.getRecordID().equals(recordID)) {
                r.setNotes(sessionNotesTA.getText().trim());
                r.setRecommendations(recommendationsTA.getText().trim());
                found = true;
                break;
            }
        }

        if (!found) {
            javax.swing.JOptionPane.showMessageDialog(this, "Record not found.");
            return;
        }

        fileManager.saveAllRecords(allRecords);
        javax.swing.JOptionPane.showMessageDialog(this, "Record updated successfully!");
        loadConsultationRecords();
    }//GEN-LAST:event_updateBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        String recordID = recordIDTf.getText().trim();
        if (recordID.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please select a record from the table first.");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "This will permanently clear the notes and recommendations for record " + recordID + ". Continue?",
                "Confirm Clear",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        ArrayList<ConsultationRecord> allRecords = fileManager.getAllRecords();
        boolean found = false;
        for (ConsultationRecord r : allRecords) {
            if (r.getRecordID().equals(recordID)) {
                r.setNotes("");
                r.setRecommendations("");
                found = true;
                break;
            }
        }

        if (!found) {
            javax.swing.JOptionPane.showMessageDialog(this, "Record not found.");
            return;
        }

        fileManager.saveAllRecords(allRecords);
        loadConsultationRecords();

        // Also clear the on-screen form fields since the record is now empty
        recordIDTf.setText("");
        sessionNotesTA.setText("");
        recommendationsTA.setText("");

        javax.swing.JOptionPane.showMessageDialog(this, "Record cleared successfully.");
        
    }//GEN-LAST:event_clearBtnActionPerformed

    private void jScrollPane5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane5MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jScrollPane5MouseClicked
    
    private void setActiveButton(javax.swing.JButton activeButton) {
        javax.swing.JButton[] buttons = {dashboardBtn, scheduleBtn, appointmentBtn, recordsBtn, profileBtn};
        for (javax.swing.JButton btn : buttons) {
            if (btn == activeButton) {
                btn.setBackground(new java.awt.Color(37, 99, 235)); // Royal Blue Active
                btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            } else {
                btn.setBackground(new java.awt.Color(30, 41, 59)); // Dark Slate Inactive
                btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            }
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new CounselorDashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton appointmentBtn;
    private javax.swing.JPanel appointmentCard;
    private javax.swing.JTextField appointmentIDTf;
    private javax.swing.JTable appointmentTbl;
    private javax.swing.JLabel appointmentTitleLbl;
    private javax.swing.JButton apptAcceptBtn;
    private javax.swing.JButton apptCancelBtn;
    private javax.swing.JButton apptRefreshBtn;
    private javax.swing.JButton changepwdBtn;
    private javax.swing.JButton clearBtn;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JButton dashboardBtn;
    private javax.swing.JPanel dashboardCard;
    private javax.swing.JComboBox<String> filterstatusComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JButton logoutBtn;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTable myscheduleTbl;
    private javax.swing.JLabel nameLbl;
    private javax.swing.JTextField nameTf;
    private javax.swing.JButton newRecordsBtn;
    private javax.swing.JLabel passwordLbl;
    private javax.swing.JTextField passwordTf;
    private javax.swing.JButton profileBtn;
    private javax.swing.JPanel profileCard;
    private javax.swing.JLabel recommendationLbl;
    private javax.swing.JTextArea recommendationsTA;
    private javax.swing.JTextField recordIDTf;
    private javax.swing.JButton recordsBtn;
    private javax.swing.JPanel recordsCard;
    private javax.swing.JButton recordsRefreshBtn1;
    private javax.swing.JTable recordsTbl;
    private javax.swing.JLabel recordsTitleLbl;
    private javax.swing.JLabel recordsTitleLbl1;
    private javax.swing.JButton schdRefreshBtn;
    private javax.swing.JButton scheduleBtn;
    private javax.swing.JPanel scheduleCard;
    private javax.swing.JTextArea sessionNotesTA;
    private javax.swing.JPanel slidebarPanel;
    private javax.swing.JLabel slidebarnameTf;
    private javax.swing.JTextField studentnameTf;
    private javax.swing.JButton updateBtn;
    private javax.swing.JLabel useridLbl;
    private javax.swing.JLabel useridLbl1;
    private javax.swing.JTextField useridTf;
    private javax.swing.JLabel usernameLbl;
    private javax.swing.JTextField usernameTf;
    private javax.swing.JLabel welcomenameTf;
    private javax.swing.JLabel welcomenameTf1;
    // End of variables declaration//GEN-END:variables
}
