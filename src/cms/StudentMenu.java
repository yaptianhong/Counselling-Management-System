/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cms;

import javax.swing.plaf.basic.BasicTabbedPaneUI;


public class StudentMenu extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StudentMenu.class.getName());
    private Student student;
    private FileManager fileManager;
    /**
     * Creates new form NewJFrame
     */
    public StudentMenu() {
        initComponents();
        setupDateTimeSpinners();
        loadImages();
        hideTabHeaders();
    }
    
    public StudentMenu(Student student, FileManager fileManager) {
        this();
        this.student = student;
        this.fileManager = fileManager;
        
        lblSidebarStudentName.setText(student.getName());
        
        loadMyAppointments();
        loadCounselors();
        loadQueueNumbers();
        loadCounselorProfiles();
        loadConsultationRecords();
        loadProfile();
    }
    
    private void loadImages() {

        setScaledIcon(lblLogoIcon, "/cms/CMS_Icon.png", lblLogoIcon.getWidth(), lblLogoIcon.getHeight());

        //setScaledIcon(lblUserIcon, "/cms/userIcon.png", lblUserIcon.getWidth(), lblUserIcon.getHeight());

    }
 
    private void setScaledIcon(javax.swing.JLabel label, String resourcePath, int width, int height) {

        java.net.URL imgUrl = getClass().getResource(resourcePath);

        if (imgUrl == null) {

            System.out.println("Image not found: " + resourcePath);

            return;

        }

        javax.swing.ImageIcon rawIcon = new javax.swing.ImageIcon(imgUrl);

        java.awt.Image scaledImg = rawIcon.getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);

        label.setIcon(new javax.swing.ImageIcon(scaledImg));

    }
    
    private void hideTabHeaders() {
    tabsContent.setUI(new BasicTabbedPaneUI() {
        @Override
        protected int calculateTabAreaHeight(
                int tabPlacement,
                int horizRunCount,
                int maxTabHeight) {
            return 0;
        }
    });
}
    
    private void loadMyAppointments() {
    javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel)
            tblMyAppointments.getModel();

    model.setRowCount(0);

    if (student == null || fileManager == null) {
        return;
    }

    for (Appointment appointment : student.viewMyHistory(fileManager)) {
        model.addRow(new Object[]{
            appointment.getAppointmentID(),
            appointment.getCounselorID(),
            appointment.getDate(),
            appointment.getTime(),
            appointment.getStatus(),
            appointment.getType(),
            appointment.getQueueNumber()
        });
    }
}
    
    private void loadCounselors() {
    javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel)
            tblCounselors.getModel();

    model.setRowCount(0);

    if (student == null || fileManager == null) {
        return;
    }

    for (Counselor counselor : student.viewCounselorProfiles(fileManager)) {
        model.addRow(new Object[]{
            counselor.getUserID(),
            counselor.getName()
        });
    }
}
    
    private void loadQueueNumbers() {
    javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel)
            tblQueueNumbers.getModel();

    model.setRowCount(0);

    if (student == null || fileManager == null) {
        return;
    }

    for (Appointment appointment : student.viewMyQueueNumbers(fileManager)) {
        model.addRow(new Object[]{
            appointment.getAppointmentID(),
            appointment.getCounselorID(),
            appointment.getDate(),
            appointment.getTime(),
            appointment.getQueueNumber()
        });
    }
}
    
    private void loadCounselorProfiles() {
    javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel)
            tblCounselorProfiles.getModel();

    model.setRowCount(0);

    if (student == null || fileManager == null) {
        return;
    }

    for (Counselor counselor : student.viewCounselorProfiles(fileManager)) {
        model.addRow(new Object[]{
            counselor.getUserID(),
            counselor.getName()
        });
    }
}
    
    private void loadConsultationRecords() {
    javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel)
            tblConsultationRecords.getModel();

    model.setRowCount(0);

    if (student == null || fileManager == null) {
        return;
    }

    for (ConsultationRecord record
            : student.viewMyConsultationRecords(fileManager)) {

        model.addRow(new Object[]{
            record.getRecordID(),
            record.getAppointmentID(),
            record.getNotes(),
            record.getRecommendations()
        });
    }
}
    
    private void loadProfile() {
    if (student == null) {
        return;
    }

    txtProfileID.setText(student.getUserID());
    txtProfileUsername.setText(student.getUsername());
    txtProfileName.setText(student.getName());
    txtCurrentPassword.setText(student.getPassword());
    
    txtNewPassword.setText("");
    txtConfirmPassword.setText("");
}
    
    private void setupDateTimeSpinners() {
    java.util.Date today =
            java.sql.Date.valueOf(java.time.LocalDate.now());

    // Date spinner: today onwards only
    spnBookDate.setModel(new javax.swing.SpinnerDateModel(
            today, today, null, java.util.Calendar.DAY_OF_MONTH
    ));

    spnBookDate.setEditor(
            new javax.swing.JSpinner.DateEditor(
                    spnBookDate, "yyyy-MM-dd"
            )
    );

    spnNewDate.setModel(new javax.swing.SpinnerDateModel(
            today, today, null, java.util.Calendar.DAY_OF_MONTH
    ));

    spnNewDate.setEditor(
            new javax.swing.JSpinner.DateEditor(
                    spnNewDate, "yyyy-MM-dd"
            )
    );

    // Time spinner: fixed valid appointment times
    String[] times = {
        "09:00", "09:15", "09:30", "09:45",
        "10:00", "10:15", "10:30", "10:45",
        "11:00", "11:15", "11:30", "11:45",
        "12:00", "12:15", "12:30", "12:45",
        "14:00", "14:15", "14:30", "14:45",
        "15:00", "15:15", "15:30", "15:45",
        "16:00", "16:15", "16:30", "16:45",
        "17:00", "17:15", "17:30", "17:45", "18:00"
    };

    spnBookTime.setModel(new javax.swing.SpinnerListModel(times));
    spnNewTime.setModel(new javax.swing.SpinnerListModel(times));
}

private String getSpinnerDate(javax.swing.JSpinner spinner) {
    return new java.text.SimpleDateFormat("yyyy-MM-dd")
            .format((java.util.Date) spinner.getValue());
}
 

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSideBar = new javax.swing.JPanel();
        lblLogoIcon = new javax.swing.JLabel();
        lblCMS = new javax.swing.JLabel();
        lblSidebarStudentName = new javax.swing.JLabel();
        lblSidebarRole = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnNavBook = new javax.swing.JButton();
        btnNavAppointments = new javax.swing.JButton();
        btnNavQueue = new javax.swing.JButton();
        btnNavCounselors = new javax.swing.JButton();
        btnNavProfile = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnNavRecords = new javax.swing.JButton();
        pnlMainContent = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        lblWelcomeInfo = new javax.swing.JLabel();
        tabsContent = new javax.swing.JTabbedPane();
        pnlDashboard = new javax.swing.JPanel();
        pnlOverview = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtOverview = new javax.swing.JTextArea();
        lblOverview = new javax.swing.JLabel();
        pnlStudentModules = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtModules = new javax.swing.JTextArea();
        pnlBookAppointment = new javax.swing.JPanel();
        lblBookTitle = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblCounselors = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        lblBookCounselorID = new javax.swing.JLabel();
        lblBookDate = new javax.swing.JLabel();
        lblBookTime = new javax.swing.JLabel();
        txtBookCounselorID = new javax.swing.JTextField();
        btnBookAppointment = new javax.swing.JButton();
        btnRefresh1 = new javax.swing.JButton();
        spnBookDate = new javax.swing.JSpinner();
        spnBookTime = new javax.swing.JSpinner();
        pnlMyAppointments = new javax.swing.JPanel();
        pnlManageAppointment = new javax.swing.JPanel();
        lblSelectedAppointmentID = new javax.swing.JLabel();
        lblNewTime = new javax.swing.JLabel();
        lblNewDate = new javax.swing.JLabel();
        btnReschedule = new javax.swing.JButton();
        btnCancelAppointment = new javax.swing.JButton();
        txtAppointmentID = new javax.swing.JTextField();
        btnRefresh3 = new javax.swing.JButton();
        spnNewDate = new javax.swing.JSpinner();
        spnNewTime = new javax.swing.JSpinner();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblMyAppointments = new javax.swing.JTable();
        lblMyAppointments = new javax.swing.JLabel();
        pnlQueueNumbers = new javax.swing.JPanel();
        lblQueueNumbers = new javax.swing.JLabel();
        lblQueueInfo = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblQueueNumbers = new javax.swing.JTable();
        btnRefresh = new javax.swing.JButton();
        pnlCounselorProfiles = new javax.swing.JPanel();
        lblCounselorProfiles = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblCounselorProfiles = new javax.swing.JTable();
        btnRefresh2 = new javax.swing.JButton();
        pnlConsultationRecords = new javax.swing.JPanel();
        lblConsultationRecords = new javax.swing.JLabel();
        btnRefresh4 = new javax.swing.JButton();
        lblRecordInfo = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblConsultationRecords = new javax.swing.JTable();
        pnlMyProfile = new javax.swing.JPanel();
        lblMyProfile = new javax.swing.JLabel();
        lblProfileID = new javax.swing.JLabel();
        lblProfileUsername = new javax.swing.JLabel();
        txtProfileUsername = new javax.swing.JTextField();
        lblProfileName = new javax.swing.JLabel();
        txtProfileID = new javax.swing.JTextField();
        txtProfileName = new javax.swing.JTextField();
        lblProfilePassword = new javax.swing.JLabel();
        btnChangePassword = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JPasswordField();
        txtCurrentPassword = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Counseling Management System - Student");
        setResizable(false);

        pnlSideBar.setBackground(new java.awt.Color(20, 32, 61));
        pnlSideBar.setPreferredSize(new java.awt.Dimension(200, 650));

        lblLogoIcon.setBackground(new java.awt.Color(204, 204, 204));

        lblCMS.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblCMS.setForeground(new java.awt.Color(255, 255, 255));
        lblCMS.setText("CMS");

        lblSidebarStudentName.setForeground(new java.awt.Color(255, 255, 255));
        lblSidebarStudentName.setText("Student Name");

        lblSidebarRole.setForeground(new java.awt.Color(255, 255, 255));
        lblSidebarRole.setText("Student");

        btnDashboard.setText("Dashboard");
        btnDashboard.addActionListener(this::btnDashboardActionPerformed);

        btnNavBook.setText("Book Appointment");
        btnNavBook.addActionListener(this::btnNavBookActionPerformed);

        btnNavAppointments.setText("My Appointments");
        btnNavAppointments.addActionListener(this::btnNavAppointmentsActionPerformed);

        btnNavQueue.setText("Queue Numbers");
        btnNavQueue.addActionListener(this::btnNavQueueActionPerformed);

        btnNavCounselors.setText("Counselor Profiles");
        btnNavCounselors.addActionListener(this::btnNavCounselorsActionPerformed);

        btnNavProfile.setText("My Profile");
        btnNavProfile.addActionListener(this::btnNavProfileActionPerformed);

        btnLogout.setText("Logout");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        btnNavRecords.setText("Consultation Records");
        btnNavRecords.addActionListener(this::btnNavRecordsActionPerformed);

        javax.swing.GroupLayout pnlSideBarLayout = new javax.swing.GroupLayout(pnlSideBar);
        pnlSideBar.setLayout(pnlSideBarLayout);
        pnlSideBarLayout.setHorizontalGroup(
            pnlSideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSideBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSideBarLayout.createSequentialGroup()
                        .addGroup(pnlSideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlSideBarLayout.createSequentialGroup()
                                .addComponent(lblLogoIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblCMS))
                            .addComponent(lblSidebarRole, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSidebarStudentName))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlSideBarLayout.createSequentialGroup()
                        .addGroup(pnlSideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnNavBook, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(btnNavAppointments, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(btnNavQueue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNavCounselors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNavRecords, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNavProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 24, Short.MAX_VALUE))))
        );
        pnlSideBarLayout.setVerticalGroup(
            pnlSideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSideBarLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(pnlSideBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLogoIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlSideBarLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lblCMS, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(lblSidebarStudentName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSidebarRole)
                .addGap(18, 18, 18)
                .addComponent(btnDashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavBook)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavAppointments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavQueue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavCounselors)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavRecords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNavProfile)
                .addGap(105, 105, 105)
                .addComponent(btnLogout)
                .addContainerGap(120, Short.MAX_VALUE))
        );

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setText("WELCOME, STUDENT!");

        lblWelcomeInfo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblWelcomeInfo.setText("Use the menu on the left to manage your appointments and profile");

        pnlDashboard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlDashboard.setToolTipText("");
        pnlDashboard.setPreferredSize(new java.awt.Dimension(300, 200));

        pnlOverview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtOverview.setEditable(false);
        txtOverview.setColumns(20);
        txtOverview.setRows(5);
        txtOverview.setText("Welcome to the Counseling Management System.\n\nUse the navigation menu to book and manage\nyour counseling appointments.");
        jScrollPane2.setViewportView(txtOverview);

        lblOverview.setText("Overview");

        javax.swing.GroupLayout pnlOverviewLayout = new javax.swing.GroupLayout(pnlOverview);
        pnlOverview.setLayout(pnlOverviewLayout);
        pnlOverviewLayout.setHorizontalGroup(
            pnlOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(pnlOverviewLayout.createSequentialGroup()
                        .addComponent(lblOverview)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlOverviewLayout.setVerticalGroup(
            pnlOverviewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOverviewLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOverview)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlStudentModules.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setText(" STUDENT MODULES");

        txtModules.setEditable(false);
        txtModules.setColumns(20);
        txtModules.setRows(5);
        txtModules.setText("1. Book Online Appointment\n2. View My Appointments\n3. Reschedule / Cancel Appointment\n4. View Queue Number\n5. View Counselor Profiles\n6. Change Password");
        jScrollPane1.setViewportView(txtModules);

        javax.swing.GroupLayout pnlStudentModulesLayout = new javax.swing.GroupLayout(pnlStudentModules);
        pnlStudentModules.setLayout(pnlStudentModulesLayout);
        pnlStudentModulesLayout.setHorizontalGroup(
            pnlStudentModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStudentModulesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStudentModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlStudentModulesLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStudentModulesLayout.setVerticalGroup(
            pnlStudentModulesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStudentModulesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlDashboardLayout = new javax.swing.GroupLayout(pnlDashboard);
        pnlDashboard.setLayout(pnlDashboardLayout);
        pnlDashboardLayout.setHorizontalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDashboardLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlStudentModules, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOverview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        pnlDashboardLayout.setVerticalGroup(
            pnlDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDashboardLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(pnlStudentModules, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOverview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );

        tabsContent.addTab("Dashboard", pnlDashboard);

        lblBookTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblBookTitle.setText("BOOK ONLINE APPOINTMENT");

        tblCounselors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Counselor ID", "Counselor Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCounselors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCounselorsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tblCounselors);

        lblBookCounselorID.setText("Selected Counselor ID:");

        lblBookDate.setText("Date (YYYY-MM-DD):");

        lblBookTime.setText("Time (HH:MM):");

        txtBookCounselorID.setEditable(false);
        txtBookCounselorID.addActionListener(this::txtBookCounselorIDActionPerformed);

        btnBookAppointment.setText("Book Online Appointment");
        btnBookAppointment.addActionListener(this::btnBookAppointmentActionPerformed);

        btnRefresh1.setText("Refresh Data");
        btnRefresh1.addActionListener(this::btnRefresh1ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBookDate)
                    .addComponent(lblBookTime)
                    .addComponent(lblBookCounselorID))
                .addGap(101, 101, 101)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtBookCounselorID, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnBookDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnBookTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRefresh1)
                .addGap(18, 18, 18)
                .addComponent(btnBookAppointment)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBookCounselorID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBookCounselorID))
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBookDate)
                    .addComponent(spnBookDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBookTime)
                    .addComponent(spnBookTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBookAppointment)
                    .addComponent(btnRefresh1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlBookAppointmentLayout = new javax.swing.GroupLayout(pnlBookAppointment);
        pnlBookAppointment.setLayout(pnlBookAppointmentLayout);
        pnlBookAppointmentLayout.setHorizontalGroup(
            pnlBookAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(pnlBookAppointmentLayout.createSequentialGroup()
                .addGap(241, 241, 241)
                .addComponent(lblBookTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(265, Short.MAX_VALUE))
        );
        pnlBookAppointmentLayout.setVerticalGroup(
            pnlBookAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBookAppointmentLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(lblBookTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabsContent.addTab("Book Appointment", pnlBookAppointment);

        pnlMyAppointments.setPreferredSize(new java.awt.Dimension(769, 530));

        lblSelectedAppointmentID.setText("Selected Appointment ID:");

        lblNewTime.setText("New Time (HH:MM):");

        lblNewDate.setText("New Date (YYYY-MM-DD):");

        btnReschedule.setText("Reschedule Appointment");
        btnReschedule.addActionListener(this::btnRescheduleActionPerformed);

        btnCancelAppointment.setText("Cancel Appointment");
        btnCancelAppointment.addActionListener(this::btnCancelAppointmentActionPerformed);

        txtAppointmentID.setEditable(false);
        txtAppointmentID.addActionListener(this::txtAppointmentIDActionPerformed);

        btnRefresh3.setText("Refresh Data");
        btnRefresh3.addActionListener(this::btnRefresh3ActionPerformed);

        javax.swing.GroupLayout pnlManageAppointmentLayout = new javax.swing.GroupLayout(pnlManageAppointment);
        pnlManageAppointment.setLayout(pnlManageAppointmentLayout);
        pnlManageAppointmentLayout.setHorizontalGroup(
            pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlManageAppointmentLayout.createSequentialGroup()
                .addGap(110, 110, 110)
                .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlManageAppointmentLayout.createSequentialGroup()
                        .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblNewDate)
                            .addComponent(lblNewTime)
                            .addComponent(lblSelectedAppointmentID))
                        .addGap(100, 100, 100)
                        .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAppointmentID, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .addGroup(pnlManageAppointmentLayout.createSequentialGroup()
                                .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spnNewDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(spnNewTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(pnlManageAppointmentLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRefresh3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnReschedule)))
                .addGap(18, 18, 18)
                .addComponent(btnCancelAppointment)
                .addContainerGap())
        );
        pnlManageAppointmentLayout.setVerticalGroup(
            pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlManageAppointmentLayout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSelectedAppointmentID)
                    .addComponent(txtAppointmentID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNewDate)
                    .addComponent(spnNewDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNewTime)
                    .addComponent(spnNewTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelAppointment)
                    .addGroup(pnlManageAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnReschedule)
                        .addComponent(btnRefresh3))))
        );

        tblMyAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Conselor ID", "Date", "Time", "Status", "Type", "Queue No."
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMyAppointments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMyAppointmentsMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tblMyAppointments);

        lblMyAppointments.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblMyAppointments.setText("My Appointment History");

        javax.swing.GroupLayout pnlMyAppointmentsLayout = new javax.swing.GroupLayout(pnlMyAppointments);
        pnlMyAppointments.setLayout(pnlMyAppointmentsLayout);
        pnlMyAppointmentsLayout.setHorizontalGroup(
            pnlMyAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMyAppointmentsLayout.createSequentialGroup()
                .addGap(268, 268, 268)
                .addComponent(lblMyAppointments)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMyAppointmentsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlManageAppointment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlMyAppointmentsLayout.setVerticalGroup(
            pnlMyAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMyAppointmentsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblMyAppointments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlManageAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(182, 182, 182))
        );

        tabsContent.addTab("My Appointments", pnlMyAppointments);

        lblQueueNumbers.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblQueueNumbers.setText("My Queue Numbers");

        lblQueueInfo.setText("Only appointments with an assigned queue number are displayed.");

        tblQueueNumbers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Counselor ID", "Date", "Time", "Queue No."
            }
        ));
        jScrollPane5.setViewportView(tblQueueNumbers);

        btnRefresh.setText("Refresh Data");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        javax.swing.GroupLayout pnlQueueNumbersLayout = new javax.swing.GroupLayout(pnlQueueNumbers);
        pnlQueueNumbers.setLayout(pnlQueueNumbersLayout);
        pnlQueueNumbersLayout.setHorizontalGroup(
            pnlQueueNumbersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQueueNumbersLayout.createSequentialGroup()
                .addGap(295, 295, 295)
                .addComponent(lblQueueNumbers)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlQueueNumbersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlQueueNumbersLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlQueueNumbersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlQueueNumbersLayout.createSequentialGroup()
                        .addComponent(lblQueueInfo)
                        .addGap(195, 195, 195))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlQueueNumbersLayout.createSequentialGroup()
                        .addComponent(btnRefresh)
                        .addGap(20, 20, 20))))
        );
        pnlQueueNumbersLayout.setVerticalGroup(
            pnlQueueNumbersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQueueNumbersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblQueueNumbers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblQueueInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabsContent.addTab("Queue Numbers", pnlQueueNumbers);

        lblCounselorProfiles.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblCounselorProfiles.setText("Counselor Profiles");

        tblCounselorProfiles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Counselor ID", "Counselor Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(tblCounselorProfiles);

        btnRefresh2.setText("Refresh Data");
        btnRefresh2.addActionListener(this::btnRefresh2ActionPerformed);

        javax.swing.GroupLayout pnlCounselorProfilesLayout = new javax.swing.GroupLayout(pnlCounselorProfiles);
        pnlCounselorProfiles.setLayout(pnlCounselorProfilesLayout);
        pnlCounselorProfilesLayout.setHorizontalGroup(
            pnlCounselorProfilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCounselorProfilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCounselorProfilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCounselorProfilesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRefresh2)))
                .addContainerGap())
            .addGroup(pnlCounselorProfilesLayout.createSequentialGroup()
                .addGap(288, 288, 288)
                .addComponent(lblCounselorProfiles)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlCounselorProfilesLayout.setVerticalGroup(
            pnlCounselorProfilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCounselorProfilesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCounselorProfiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnRefresh2)
                .addGap(25, 25, 25))
        );

        tabsContent.addTab("Counselor Profiles", pnlCounselorProfiles);

        lblConsultationRecords.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblConsultationRecords.setText("My Consultation Records");

        btnRefresh4.setText("Refresh Data");
        btnRefresh4.addActionListener(this::btnRefresh4ActionPerformed);

        lblRecordInfo.setText("Only records linked to your appointments are shown.");

        tblConsultationRecords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Record ID", "Appointment ID", "Notes", "Recommendations"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(tblConsultationRecords);

        javax.swing.GroupLayout pnlConsultationRecordsLayout = new javax.swing.GroupLayout(pnlConsultationRecords);
        pnlConsultationRecords.setLayout(pnlConsultationRecordsLayout);
        pnlConsultationRecordsLayout.setHorizontalGroup(
            pnlConsultationRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultationRecordsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConsultationRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlConsultationRecordsLayout.createSequentialGroup()
                        .addGap(0, 258, Short.MAX_VALUE)
                        .addGroup(pnlConsultationRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultationRecordsLayout.createSequentialGroup()
                                .addComponent(lblConsultationRecords)
                                .addGap(273, 273, 273))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultationRecordsLayout.createSequentialGroup()
                                .addComponent(btnRefresh4)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlConsultationRecordsLayout.createSequentialGroup()
                                .addComponent(lblRecordInfo)
                                .addGap(235, 235, 235))))
                    .addComponent(jScrollPane7)))
        );
        pnlConsultationRecordsLayout.setVerticalGroup(
            pnlConsultationRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConsultationRecordsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblConsultationRecords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRecordInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                .addGap(15, 15, 15)
                .addComponent(btnRefresh4)
                .addContainerGap())
        );

        tabsContent.addTab("Consultation Records", pnlConsultationRecords);

        lblMyProfile.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblMyProfile.setText("My Profile");

        lblProfileID.setText("Student ID:");

        lblProfileUsername.setText("Username:");

        txtProfileUsername.setEditable(false);

        lblProfileName.setText("Full Name:");

        txtProfileName.setEditable(false);

        lblProfilePassword.setText("Current Password:");

        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(this::btnChangePasswordActionPerformed);

        jLabel1.setText("New Password:");

        jLabel2.setText("Confirm New Password:");

        javax.swing.GroupLayout pnlMyProfileLayout = new javax.swing.GroupLayout(pnlMyProfile);
        pnlMyProfile.setLayout(pnlMyProfileLayout);
        pnlMyProfileLayout.setHorizontalGroup(
            pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMyProfileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMyProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(720, 720, 720))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMyProfileLayout.createSequentialGroup()
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlMyProfileLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(txtProfileUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMyProfileLayout.createSequentialGroup()
                        .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMyProfileLayout.createSequentialGroup()
                                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlMyProfileLayout.createSequentialGroup()
                                        .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlMyProfileLayout.createSequentialGroup()
                                                    .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblProfileUsername)
                                                        .addComponent(lblProfileID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                    .addGap(23, 23, 23))
                                                .addGroup(pnlMyProfileLayout.createSequentialGroup()
                                                    .addComponent(lblProfileName)
                                                    .addGap(33, 33, 33)))
                                            .addGroup(pnlMyProfileLayout.createSequentialGroup()
                                                .addComponent(lblProfilePassword)
                                                .addGap(10, 10, 10)))
                                        .addGap(13, 13, 13))
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(69, 69, 69))
                            .addGroup(pnlMyProfileLayout.createSequentialGroup()
                                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnChangePassword))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtProfileID, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtProfileName, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtNewPassword)
                            .addComponent(txtConfirmPassword)
                            .addComponent(txtCurrentPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))))
                .addGap(638, 638, 638))
        );
        pnlMyProfileLayout.setVerticalGroup(
            pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMyProfileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMyProfile)
                .addGap(18, 18, 18)
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfileID)
                    .addComponent(txtProfileID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfileUsername)
                    .addComponent(txtProfileUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfileName)
                    .addComponent(txtProfileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblProfilePassword)
                    .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlMyProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnChangePassword)
                .addContainerGap(181, Short.MAX_VALUE))
        );

        tabsContent.addTab("My Profile", pnlMyProfile);

        javax.swing.GroupLayout pnlMainContentLayout = new javax.swing.GroupLayout(pnlMainContent);
        pnlMainContent.setLayout(pnlMainContentLayout);
        pnlMainContentLayout.setHorizontalGroup(
            pnlMainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainContentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWelcomeInfo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMainContentLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tabsContent, javax.swing.GroupLayout.PREFERRED_SIZE, 779, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlMainContentLayout.setVerticalGroup(
            pnlMainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainContentLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(lblWelcome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWelcomeInfo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(tabsContent, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlSideBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMainContent, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMainContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(pnlSideBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        tabsContent.setSelectedIndex(0);
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void tblCounselorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCounselorsMouseClicked
        // TODO add your handling code here:
        int selectedRow = tblCounselors.getSelectedRow();

        if (selectedRow != -1) {
            txtBookCounselorID.setText(
                tblCounselors.getValueAt(selectedRow, 0).toString()
            );
        }
    }//GEN-LAST:event_tblCounselorsMouseClicked

    private void txtBookCounselorIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBookCounselorIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBookCounselorIDActionPerformed

    private void btnBookAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookAppointmentActionPerformed
        // TODO add your handling code here:
        String counselorID = txtBookCounselorID.getText().trim();
        String date = getSpinnerDate(spnBookDate);
        String time = spnBookTime.getValue().toString();

        if (counselorID.isEmpty() || date.isEmpty() || time.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select a counselor and enter date and time."
            );
            return;
        }

        Appointment appointment = student.bookOnlineAppointment(
            counselorID,
            date,
            time,
            fileManager
        );

        if (appointment != null) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Appointment booked successfully.\nAppointment ID: "
                + "Appointment ID: " + appointment.getAppointmentID()
            );

            loadMyAppointments();

            txtBookCounselorID.setText("");

            spnBookDate.setValue(
                    java.sql.Date.valueOf(java.time.LocalDate.now())
            );

            spnBookTime.setValue("09:00");

            loadMyAppointments();
            loadQueueNumbers();
            tabsContent.setSelectedIndex(2);

        } else {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Booking failed. Check the counselor, date and time."
            );
        }
    }//GEN-LAST:event_btnBookAppointmentActionPerformed

    private void btnRescheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRescheduleActionPerformed
        // TODO add your handling code here:
        String appointmentID = txtAppointmentID.getText().trim();
        String newDate = getSpinnerDate(spnNewDate);
        String newTime = spnNewTime.getValue().toString();

        if (appointmentID.isEmpty() || newDate.isEmpty() || newTime.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select an appointment and enter a new date and time."
            );
            return;
        }

        boolean success = student.rescheduleAppointment(
            appointmentID,
            newDate,
            newTime,
            fileManager
        );

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Appointment rescheduled successfully."
            );

            loadMyAppointments();

            txtAppointmentID.setText("");

            spnNewDate.setValue(
                    java.sql.Date.valueOf(java.time.LocalDate.now())
            );

            spnNewTime.setValue("09:00");

        } else {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Reschedule failed. Please check the appointment details."
            );
        }
    }//GEN-LAST:event_btnRescheduleActionPerformed

    private void btnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelAppointmentActionPerformed
        // TODO add your handling code here:
        String appointmentID = txtAppointmentID.getText().trim();

        if (appointmentID.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select an appointment first."
            );
            return;
        }

        int choice = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel appointment " + appointmentID + "?",
            "Confirm Cancellation",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = student.cancelAppointment(
            appointmentID,
            fileManager
        );

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Appointment cancelled successfully."
            );

            loadMyAppointments();

            txtAppointmentID.setText("");

            spnNewDate.setValue(
                    java.sql.Date.valueOf(java.time.LocalDate.now())
            );

            spnNewTime.setValue("09:00");

        } else {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Cancellation failed."
            );
        }
    }//GEN-LAST:event_btnCancelAppointmentActionPerformed

    private void txtAppointmentIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAppointmentIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAppointmentIDActionPerformed

    private void tblMyAppointmentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMyAppointmentsMouseClicked
        // TODO add your handling code here:
        int selectedRow = tblMyAppointments.getSelectedRow();

        if (selectedRow != -1) {
            txtAppointmentID.setText(
                tblMyAppointments.getValueAt(selectedRow, 0).toString()
            );
            
            spnNewDate.setValue(
                java.sql.Date.valueOf(
                        tblMyAppointments.getValueAt(selectedRow, 2).toString()
                )   
            );

            spnNewTime.setValue(
                    tblMyAppointments.getValueAt(selectedRow, 3).toString()
            );
        }

    }//GEN-LAST:event_tblMyAppointmentsMouseClicked

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        loadMyAppointments();
        loadCounselors();
        loadQueueNumbers();
        loadCounselorProfiles();
        loadConsultationRecords();

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Data refreshed successfully."
        );
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnRefresh1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh1ActionPerformed
        // TODO add your handling code here:
        loadMyAppointments();
        loadCounselors();
        loadQueueNumbers();
        loadCounselorProfiles();
        loadConsultationRecords();

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Data refreshed successfully."
        );
    }//GEN-LAST:event_btnRefresh1ActionPerformed

    private void btnRefresh2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh2ActionPerformed
        // TODO add your handling code here:
        loadMyAppointments();
        loadCounselors();
        loadQueueNumbers();
        loadCounselorProfiles();
        loadConsultationRecords();

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Data refreshed successfully."
        );
    }//GEN-LAST:event_btnRefresh2ActionPerformed

    private void btnRefresh3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRefresh3ActionPerformed

    private void btnRefresh4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh4ActionPerformed
        // TODO add your handling code here:
        loadMyAppointments();
        loadCounselors();
        loadQueueNumbers();
        loadCounselorProfiles();
        loadConsultationRecords();

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Data refreshed successfully."
        );
    }//GEN-LAST:event_btnRefresh4ActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        // TODO add your handling code here:
        String currentPassword = txtCurrentPassword.getText().trim();
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        boolean success = student.changeMyPassword(
            currentPassword,
            newPassword,
            confirmPassword,
            fileManager
        );

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Password changed successfully."
            );

            loadProfile();
        } else {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Password change failed. Please check your current password and confirmation password."
            );
        }
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    private void btnNavBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavBookActionPerformed
        tabsContent.setSelectedIndex(1);
    }//GEN-LAST:event_btnNavBookActionPerformed

    private void btnNavAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavAppointmentsActionPerformed
        tabsContent.setSelectedIndex(2);
    }//GEN-LAST:event_btnNavAppointmentsActionPerformed

    private void btnNavQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavQueueActionPerformed
        tabsContent.setSelectedIndex(3);
    }//GEN-LAST:event_btnNavQueueActionPerformed

    private void btnNavCounselorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavCounselorsActionPerformed
        tabsContent.setSelectedIndex(4);
    }//GEN-LAST:event_btnNavCounselorsActionPerformed

    private void btnNavRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavRecordsActionPerformed
        tabsContent.setSelectedIndex(5);
    }//GEN-LAST:event_btnNavRecordsActionPerformed

    private void btnNavProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNavProfileActionPerformed
        tabsContent.setSelectedIndex(6);
    }//GEN-LAST:event_btnNavProfileActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
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
    }//GEN-LAST:event_btnLogoutActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new StudentMenu().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBookAppointment;
    private javax.swing.JButton btnCancelAppointment;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNavAppointments;
    private javax.swing.JButton btnNavBook;
    private javax.swing.JButton btnNavCounselors;
    private javax.swing.JButton btnNavProfile;
    private javax.swing.JButton btnNavQueue;
    private javax.swing.JButton btnNavRecords;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRefresh1;
    private javax.swing.JButton btnRefresh2;
    private javax.swing.JButton btnRefresh3;
    private javax.swing.JButton btnRefresh4;
    private javax.swing.JButton btnReschedule;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel lblBookCounselorID;
    private javax.swing.JLabel lblBookDate;
    private javax.swing.JLabel lblBookTime;
    private javax.swing.JLabel lblBookTitle;
    private javax.swing.JLabel lblCMS;
    private javax.swing.JLabel lblConsultationRecords;
    private javax.swing.JLabel lblCounselorProfiles;
    private javax.swing.JLabel lblLogoIcon;
    private javax.swing.JLabel lblMyAppointments;
    private javax.swing.JLabel lblMyProfile;
    private javax.swing.JLabel lblNewDate;
    private javax.swing.JLabel lblNewTime;
    private javax.swing.JLabel lblOverview;
    private javax.swing.JLabel lblProfileID;
    private javax.swing.JLabel lblProfileName;
    private javax.swing.JLabel lblProfilePassword;
    private javax.swing.JLabel lblProfileUsername;
    private javax.swing.JLabel lblQueueInfo;
    private javax.swing.JLabel lblQueueNumbers;
    private javax.swing.JLabel lblRecordInfo;
    private javax.swing.JLabel lblSelectedAppointmentID;
    private javax.swing.JLabel lblSidebarRole;
    private javax.swing.JLabel lblSidebarStudentName;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JLabel lblWelcomeInfo;
    private javax.swing.JPanel pnlBookAppointment;
    private javax.swing.JPanel pnlConsultationRecords;
    private javax.swing.JPanel pnlCounselorProfiles;
    private javax.swing.JPanel pnlDashboard;
    private javax.swing.JPanel pnlMainContent;
    private javax.swing.JPanel pnlManageAppointment;
    private javax.swing.JPanel pnlMyAppointments;
    private javax.swing.JPanel pnlMyProfile;
    private javax.swing.JPanel pnlOverview;
    private javax.swing.JPanel pnlQueueNumbers;
    private javax.swing.JPanel pnlSideBar;
    private javax.swing.JPanel pnlStudentModules;
    private javax.swing.JSpinner spnBookDate;
    private javax.swing.JSpinner spnBookTime;
    private javax.swing.JSpinner spnNewDate;
    private javax.swing.JSpinner spnNewTime;
    private javax.swing.JTabbedPane tabsContent;
    private javax.swing.JTable tblConsultationRecords;
    private javax.swing.JTable tblCounselorProfiles;
    private javax.swing.JTable tblCounselors;
    private javax.swing.JTable tblMyAppointments;
    private javax.swing.JTable tblQueueNumbers;
    private javax.swing.JTextField txtAppointmentID;
    private javax.swing.JTextField txtBookCounselorID;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtCurrentPassword;
    private javax.swing.JTextArea txtModules;
    private javax.swing.JPasswordField txtNewPassword;
    private javax.swing.JTextArea txtOverview;
    private javax.swing.JTextField txtProfileID;
    private javax.swing.JTextField txtProfileName;
    private javax.swing.JTextField txtProfileUsername;
    // End of variables declaration//GEN-END:variables
}
