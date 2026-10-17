/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cms;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


public class ReceptionistDashboard extends javax.swing.JFrame {
    
    private FileManager fileManager;
    private Receptionist receptionist;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ReceptionistDashboard.class.getName());

    /**
     * Creates new form ReceptionistDashboard - no-arg version used only by
     * the NetBeans GUI Builder's Design view.
     */
    public ReceptionistDashboard() {
        initComponents();
    }

    /**
     * Real constructor - called from LoginPage after a successful login,
     * passing in the actual logged-in Receptionist and the shared FileManager.
     */
    public ReceptionistDashboard(Receptionist receptionist, FileManager fileManager) {
    this.receptionist = receptionist;
    this.fileManager = (fileManager != null) ? fileManager : new FileManager();

    setTitle(this.receptionist.getDashboardTitle() + " - " + this.receptionist.getName());

    initComponents();
    
    populateAppointmentDateTimeCombos();
    
    loadCounselorCombo();
    loadAppointmentsTable();
    loadQueueData();
    loadCounselorsTable();
    
    setPreferredSize(new java.awt.Dimension(1000, 650));
    setMinimumSize(new java.awt.Dimension(1000, 650));
    setMaximumSize(new java.awt.Dimension(1000, 650));
    pack(); // applies preferred size
    setLocationRelativeTo(null); // centers the window
    setResizable(false); // prevents expansion

    tabbedMain.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
        @Override
        protected int calculateTabAreaHeight(int tabPlacement,int horizontalRunCount,int maximumTabHeight) {
            return 0;
        }
    });

    tabbedMain.setBorder(null);

    loadDashboardData();
    }
    
    private void loadStudentsTable() {
        DefaultTableModel model = (DefaultTableModel) tableStudents.getModel();
        model.setRowCount(0);

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Student) {
                model.addRow(new Object[]{
                    user.getUserID(),
                    user.getUsername(),
                    user.getName()
                });
            }
        }
    }
    
    private void clearStudentFields() {
        StdNameTf.setText("");
        UserNameTf.setText("");
        StdPasswordTf.setText("");
    }
    
    private void loadDashboardData() {
        int studentCount = 0;
        int counselorCount = 0;
        int appointmentCount = 0;
        int waitingCount = 0;

        String today = LocalDate.now().toString();

        for(User user : fileManager.getAllUsers()){
            if(user instanceof Student){
                studentCount++;
            }else if (user instanceof Counselor){
                counselorCount++;
            }
        }

        DefaultTableModel model = (DefaultTableModel) tableTodayQueue.getModel();

        model.setRowCount(0);

        // Count appointments and load today's waiting queue
        for(Appointment appointment : fileManager.getAllAppointments()){

            boolean isCancelled = appointment.getStatus().equalsIgnoreCase("Cancelled");

            // Count every non-cancelled appointment
            if (!isCancelled) {
                appointmentCount++;
            }

            boolean isToday = appointment.getDate().equals(today);

            boolean isWaiting = appointment.getStatus().equalsIgnoreCase("Pending")
                    || appointment.getStatus().equalsIgnoreCase("Waiting");

            // Show both Online and Walk-in appointments
            if(isToday && isWaiting && !isCancelled){
                waitingCount++;

                Object queueDisplay;

                // Online appointments initially have queue number 0
                if(appointment.getQueueNumber() == 0){
                    queueDisplay = "-";
                }else{
                    queueDisplay = appointment.getQueueNumber();
                }

                model.addRow(new Object[]{
                    queueDisplay,
                    findStudentName(appointment.getStudentID()),
                    appointment.getTime(),
                    appointment.getStatus()
                });
            }
        }

        labelStudentCount.setText(String.valueOf(studentCount));

        labelAppointmentCount.setText(String.valueOf(appointmentCount));

        labelWaitingCount.setText(String.valueOf(waitingCount));

        labelCounselorCount.setText(String.valueOf(counselorCount));
    }
    
    private void loadCounselorCombo() {
        CounselorCombo.removeAllItems();
        CounselorCombo.addItem("Select Counselor");

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Counselor) {
                CounselorCombo.addItem(user.getUserID() + " - " + user.getName());
        }
        }
    }
    
    private void loadAppointmentsTable() {
        DefaultTableModel model = (DefaultTableModel) AppTbl.getModel();

        model.setRowCount(0);

        for (Appointment appointment : fileManager.getAllAppointments()) {
            model.addRow(new Object[]{
                appointment.getAppointmentID(),
                appointment.getStudentID(),
                appointment.getCounselorID(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus(),
                appointment.getType(),
                appointment.getQueueNumber()
            });
        }
    }
    
    private void searchStudentsTable(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tableStudents.getModel();
        model.setRowCount(0);
        String searchText = keyword.trim().toLowerCase();

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Student) {
                boolean matchesID = user.getUserID().toLowerCase().contains(searchText);    
                boolean matchesUsername = user.getUsername().toLowerCase().contains(searchText);
                boolean matchesName = user.getName().toLowerCase().contains(searchText);

                if (matchesID || matchesUsername || matchesName) {
                    model.addRow(new Object[]{
                        user.getUserID(),
                        user.getUsername(),
                        user.getName()
                    });
                }
            }
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No matching student was found.", 
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void searchAppointmentsTable(String keyword) {
        DefaultTableModel model = (DefaultTableModel) AppTbl.getModel();
        model.setRowCount(0);
        keyword = keyword.toLowerCase().trim();

    for (Appointment appointment : fileManager.getAllAppointments()) {
        String appointmentID = appointment.getAppointmentID().toLowerCase();
        String studentID = appointment.getStudentID().toLowerCase();
        String counselorID = appointment.getCounselorID().toLowerCase();
        String status = appointment.getStatus().toLowerCase();
        String type = appointment.getType().toLowerCase();

        if (appointmentID.contains(keyword)
                || studentID.contains(keyword)
                || counselorID.contains(keyword)
                || status.contains(keyword)
                || type.contains(keyword)
                || appointment.getDate().contains(keyword)
                || appointment.getTime().contains(keyword)) {

            model.addRow(new Object[]{
                appointment.getAppointmentID(),
                appointment.getStudentID(),
                appointment.getCounselorID(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus(),
                appointment.getType(),
                appointment.getQueueNumber()
            });
        }
    }
}
    
    private String findStudentName(String studentID, ArrayList<User> users) {
        for (User user : users) {
            if (user instanceof Student && user.getUserID().equalsIgnoreCase(studentID)) {
                return user.getName();
            }
        }

        return studentID;
    }
    
    private void loadQueueData() {
        DefaultTableModel model = (DefaultTableModel) QueueTbl.getModel();

        model.setRowCount(0);

        int totalUpcoming = 0;
        int onlineCount = 0;
        int walkInCount = 0;
        int pendingCount = 0;

        LocalDate today = LocalDate.now();

        for (Appointment appointment : fileManager.getAllAppointments()){

            LocalDate appointmentDate;

            try {
                appointmentDate = LocalDate.parse(appointment.getDate());
            }catch (DateTimeParseException e){
                continue;
            }

            // Include today and all future dates
            boolean isUpcoming = !appointmentDate.isBefore(today);

            boolean isCancelled = appointment.getStatus().equalsIgnoreCase("Cancelled");

            // Show both Online and Walk-in appointments
            if (isUpcoming && !isCancelled){
                totalUpcoming++;
                
                if(appointment.getType().equalsIgnoreCase("Online")){

                    onlineCount++;

            }else if(appointment.getType().equalsIgnoreCase("Walk-in")){

                    walkInCount++;
            }

            if (appointment.getStatus().equalsIgnoreCase("Pending")|| appointment.getStatus().equalsIgnoreCase("Waiting")) {

                pendingCount++;
            }


                // Online appointments have no queue number
                Object queueDisplay;

                if (appointment.getQueueNumber() == 0){
                    queueDisplay = "-";
                }else {
                    queueDisplay = appointment.getQueueNumber();
                }

                model.addRow(new Object[]{
                    queueDisplay,
                    appointment.getAppointmentID(),
                    findStudentName(appointment.getStudentID()),
                    findCounselorName(appointment.getCounselorID()),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getType(),
                    appointment.getStatus()
});
            }
        }

        labelTotalQueue.setText(String.valueOf(totalUpcoming));
        labelOnlineQueue.setText(String.valueOf(onlineCount));
        labelWalkInQueue.setText(String.valueOf(walkInCount));
        labelPendingQueue.setText(String.valueOf(pendingCount));
        
        jLabel20.setText("Total Upcoming");
        jLabel21.setText("Online");
        jLabel22.setText("Walk-in");
        jLabel23.setText("Pending");
    }
    
    private String findStudentName(String studentID) {
        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Student && user.getUserID().equalsIgnoreCase(studentID)) {
                return user.getName();
            }
        }

        return studentID;
    }
    
    private String findCounselorName(String counselorID) {
        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Counselor && user.getUserID().equalsIgnoreCase(counselorID)) {
                return user.getName();
            }
        }

        return counselorID;
    }
    
    private void searchQueueTable(String keyword) {
        DefaultTableModel model = (DefaultTableModel) QueueTbl.getModel();

        model.setRowCount(0);

        keyword = keyword.toLowerCase().trim();
        LocalDate today = LocalDate.now();

        for (Appointment appointment : fileManager.getAllAppointments()) {
            
            LocalDate appointmentDate;
            
            try{
                appointmentDate = LocalDate.parse(appointment.getDate());
            }catch (DateTimeParseException e){
                continue;
            }

            boolean isUpcoming = !appointmentDate.isBefore(today);
            boolean isCancelled = appointment.getStatus().equalsIgnoreCase("Cancelled");
                
                if (!isUpcoming || isCancelled) {
                    continue;
            }

            String studentName = findStudentName(appointment.getStudentID());
            String counselorName = findCounselorName(appointment.getCounselorID());
            String queueNumber = String.valueOf(appointment.getQueueNumber());

            boolean matches = appointment.getAppointmentID().toLowerCase().contains(keyword) 
                    || appointment.getStudentID().toLowerCase().contains(keyword) 
                    || studentName.toLowerCase().contains(keyword)
                    || appointment.getCounselorID().toLowerCase().contains(keyword)
                    || counselorName.toLowerCase().contains(keyword)
                    || appointment.getDate().toLowerCase().contains(keyword)
                    || appointment.getTime().toLowerCase().contains(keyword)
                    || appointment.getStatus().toLowerCase().contains(keyword)
                    || queueNumber.contains(keyword);

            if (matches) {
                
                 Object queueDisplay;
                 
                if(appointment.getQueueNumber() == 0){
                    queueDisplay = "-";
                }else{
                    queueDisplay = appointment.getQueueNumber();
                }
            
                model.addRow(new Object[]{
                    queueDisplay,
                    appointment.getAppointmentID(),
                    findStudentName(appointment.getStudentID()),
                    findCounselorName(appointment.getCounselorID()),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getType(),
                    appointment.getStatus()
});
            }
        }
    }
    
    private void loadCounselorsTable() {
        DefaultTableModel model = (DefaultTableModel) CslTbl.getModel();

        model.setRowCount(0);

        int totalCounselors = 0;
        int availableCounselors = 0;

        String today = LocalDate.now().toString();

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Counselor) {
                totalCounselors++;

                int todayAppointments = 0;
                boolean currentlyBusy = false;

                for (Appointment appointment : appointments) {
                    boolean sameCounselor = appointment.getCounselorID().equalsIgnoreCase(user.getUserID());
                    boolean isToday = appointment.getDate().equals(today);
                    boolean isCancelled = appointment.getStatus().equalsIgnoreCase("Cancelled");

                    if (sameCounselor && isToday && !isCancelled) {
                        todayAppointments++;

                        if (appointment.getStatus().equalsIgnoreCase("Called")) {
                            currentlyBusy = true;
                        }
                    }
                }

                String availability;

                if (currentlyBusy) {
                    availability = "Busy";
                } else {
                    availability = "Available";
                    availableCounselors++;
                }

                model.addRow(new Object[]{
                    user.getUserID(),
                    user.getName(),
                    user.getUsername(),
                    todayAppointments,
                    availability
                });
            }
        }

        labelTotalCounselors.setText(String.valueOf(totalCounselors));
        labelAvailableCounselors.setText(String.valueOf(availableCounselors));
    }
    
    private void searchCounselorsTable(String keyword) {
        DefaultTableModel model = (DefaultTableModel) CslTbl.getModel();

        model.setRowCount(0);

        keyword = keyword.toLowerCase().trim();
        String today = LocalDate.now().toString();

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        for (User user : fileManager.getAllUsers()) {
            if (!(user instanceof Counselor)) {
                continue;
            }

            boolean matches = user.getUserID().toLowerCase().contains(keyword)
                    || user.getName().toLowerCase().contains(keyword)
                    || user.getUsername().toLowerCase().contains(keyword);

            if (!matches) {
                continue;
            }

            int todayAppointments = 0;
            boolean currentlyBusy = false;

            for (Appointment appointment : appointments) {
                boolean sameCounselor = appointment.getCounselorID().equalsIgnoreCase(user.getUserID());
                boolean isToday = appointment.getDate().equals(today);
                boolean isCancelled = appointment.getStatus().equalsIgnoreCase("Cancelled");

                    if (sameCounselor && isToday && !isCancelled) {
                        todayAppointments++;

                        if (appointment.getStatus().equalsIgnoreCase("Called")) {
                            currentlyBusy = true;
                        }
                    }
            }

            String availability = currentlyBusy ? "Busy" : "Available";

            model.addRow(new Object[]{
                user.getUserID(),
                user.getName(),
                user.getUsername(),
                todayAppointments,
                availability
            });
        }
    }
    
    private boolean hasCounselorTimeConflict(String counselorID, String date, String time, String excludedAppointmentID) {

        for (Appointment appointment : fileManager.getAllAppointments()) {

            // Ignore the appointment currently being rescheduled
            boolean isExcluded = excludedAppointmentID != null && appointment.getAppointmentID().equalsIgnoreCase(excludedAppointmentID);

            if (isExcluded) {
                continue;
            }

            boolean sameCounselor = appointment.getCounselorID().equalsIgnoreCase(counselorID);
            boolean sameDate = appointment.getDate().equals(date);
            boolean sameTime = appointment.getTime().equals(time);
            boolean isCancelled = appointment.getStatus() .equalsIgnoreCase("Cancelled");

            if (sameCounselor && sameDate && sameTime && !isCancelled) {
                return true;
            }
        }
        return false;
    }
    
    private int generateNextQueueNumber(String date) {
        int highestQueueNumber = 0;

        for (Appointment appointment : fileManager.getAllAppointments()){
            
            boolean sameDate = appointment.getDate().equals(date);
            boolean isWalkIn = appointment.getType().equalsIgnoreCase("Walk-in");
 
            if (sameDate && isWalkIn && appointment.getQueueNumber() > highestQueueNumber) {

                highestQueueNumber =appointment.getQueueNumber();
            }
        }

        return highestQueueNumber + 1;
    }
    
    private void populateAppointmentDateTimeCombos() {
        int currentYear = java.time.LocalDate.now().getYear();
        comboYear.removeAllItems();
        for (int y = currentYear; y <= currentYear + 50; y++) {
            comboYear.addItem(String.valueOf(y));
        }

        comboMonth.removeAllItems();
        for (int m = 1; m <= 12; m++) {
            comboMonth.addItem(String.format("%02d", m));
        }

        comboDate.removeAllItems();
        for (int d = 1; d <= 31; d++) {
            comboDate.addItem(String.format("%02d", d));
        }

        comboHour.removeAllItems();
        for (int h = 0; h < 24; h++) {
            comboHour.addItem(String.format("%02d", h));
        }

        comboMinute.removeAllItems();
        for (int m = 0; m < 60; m+= 15) {
            comboMinute.addItem(String.format("%02d", m));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSidebar = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        LogoutBtn = new javax.swing.JButton();
        DbdBtn = new javax.swing.JButton();
        StdBtn = new javax.swing.JButton();
        AppBtn = new javax.swing.JButton();
        QueBtn = new javax.swing.JButton();
        CslBtn = new javax.swing.JButton();
        tabbedMain = new javax.swing.JTabbedPane();
        panelDashboard = new javax.swing.JPanel();
        labelWelcome = new javax.swing.JLabel();
        labelSubtitle = new javax.swing.JLabel();
        panelStudentCard = new javax.swing.JPanel();
        labelStudentCount = new javax.swing.JLabel();
        labelStudentCardTitle = new javax.swing.JLabel();
        panelAppointmentCard = new javax.swing.JPanel();
        labelAppointmentCount = new javax.swing.JLabel();
        labelAppointmentCardTitle = new javax.swing.JLabel();
        panelWaitingCard = new javax.swing.JPanel();
        labelWaitingCount = new javax.swing.JLabel();
        labelWaitingCardTitle = new javax.swing.JLabel();
        panelCounselorCard = new javax.swing.JPanel();
        labelCounselorCount = new javax.swing.JLabel();
        labelCounselorsCardTitle = new javax.swing.JLabel();
        panelQuickActions = new javax.swing.JPanel();
        labelQuickActions = new javax.swing.JLabel();
        RegisterStudentBtn = new javax.swing.JButton();
        CreateWalkIBtn = new javax.swing.JButton();
        CreateOnlineBtn = new javax.swing.JButton();
        ViewAppointmentsBtn = new javax.swing.JButton();
        panelTodayQueue = new javax.swing.JPanel();
        labelTodayQueue = new javax.swing.JLabel();
        scrollTodayQueue = new javax.swing.JScrollPane();
        tableTodayQueue = new javax.swing.JTable();
        panelStudents = new javax.swing.JPanel();
        labelStudentManagement = new javax.swing.JLabel();
        labelStudentSubtitle = new javax.swing.JLabel();
        panelRegisterStudent = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        labelFullName = new javax.swing.JLabel();
        StdNameTf = new javax.swing.JTextField();
        labelUsername = new javax.swing.JLabel();
        UserNameTf = new javax.swing.JTextField();
        labelPassword = new javax.swing.JLabel();
        StdPasswordTf = new javax.swing.JPasswordField();
        SaveBtn = new javax.swing.JButton();
        ClearBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        SearchTf = new javax.swing.JTextField();
        SearchBtn = new javax.swing.JButton();
        RefreshBtn = new javax.swing.JButton();
        scrollStudents = new javax.swing.JScrollPane();
        tableStudents = new javax.swing.JTable();
        UpdateBtn = new javax.swing.JButton();
        DeleteBtn = new javax.swing.JButton();
        panelAppointments = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        panelCreateAppointment = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        AppStdTf = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        CounselorCombo = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        AppTypeCombo = new javax.swing.JComboBox<>();
        CreateAppBtn = new javax.swing.JButton();
        ClearAppBtn = new javax.swing.JButton();
        comboYear = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        comboMonth = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        comboDate = new javax.swing.JComboBox<>();
        comboHour = new javax.swing.JComboBox<>();
        jLabel30 = new javax.swing.JLabel();
        comboMinute = new javax.swing.JComboBox<>();
        panelAppointmentRecords = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        SearchAppTf = new javax.swing.JTextField();
        SearchAppBtn = new javax.swing.JButton();
        RefreshAppBtn = new javax.swing.JButton();
        scrollApp = new javax.swing.JScrollPane();
        AppTbl = new javax.swing.JTable();
        RescheduleBtn = new javax.swing.JButton();
        AssignBtn = new javax.swing.JButton();
        CancelBtn = new javax.swing.JButton();
        panelQueue = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        panelTotalQueueCard = new javax.swing.JPanel();
        labelTotalQueue = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        panelWaiting = new javax.swing.JPanel();
        labelOnlineQueue = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        panelCalledCard = new javax.swing.JPanel();
        labelWalkInQueue = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        panelCompletedCard = new javax.swing.JPanel();
        labelPendingQueue = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        panelQueueRecords = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        SearchQueTf = new javax.swing.JTextField();
        SearchQueBtn = new javax.swing.JButton();
        RefreshQueBtn = new javax.swing.JButton();
        scrollQueue = new javax.swing.JScrollPane();
        QueueTbl = new javax.swing.JTable();
        panelCounselors = new javax.swing.JPanel();
        labelCounselorTitle = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        panelTotalCounselorsCount = new javax.swing.JPanel();
        labelTotalCounselors = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        panelAvailableCounselorsCount = new javax.swing.JPanel();
        labelAvailableCounselors = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        SearchCslTf = new javax.swing.JTextField();
        SearchCslBtn = new javax.swing.JButton();
        RefreshCslBtn = new javax.swing.JButton();
        scrollCsl = new javax.swing.JScrollPane();
        CslTbl = new javax.swing.JTable();
        panelCounselorSchedule = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        labelSelectedCsl = new javax.swing.JLabel();
        scrollCslSchedule = new javax.swing.JScrollPane();
        CslScheduleTbl = new javax.swing.JTable();
        ViewScheduleBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setMinimumSize(new java.awt.Dimension(1000, 650));
        setSize(new java.awt.Dimension(0, 0));

        panelSidebar.setBackground(new java.awt.Color(20, 32, 61));
        panelSidebar.setPreferredSize(new java.awt.Dimension(220, 650));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cms/images/CMS_Icon_70x70.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("CMS");

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Rachel Receptionist");

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Receptionist");

        LogoutBtn.setBackground(new java.awt.Color(37, 99, 235));
        LogoutBtn.setText("Logout");
        LogoutBtn.addActionListener(this::LogoutBtnActionPerformed);

        DbdBtn.setBackground(new java.awt.Color(248, 250, 252));
        DbdBtn.setForeground(new java.awt.Color(0, 0, 0));
        DbdBtn.setText("Dashboard");
        DbdBtn.addActionListener(this::DbdBtnActionPerformed);

        StdBtn.setBackground(new java.awt.Color(248, 250, 252));
        StdBtn.setForeground(new java.awt.Color(0, 0, 0));
        StdBtn.setText("Students");
        StdBtn.addActionListener(this::StdBtnActionPerformed);

        AppBtn.setBackground(new java.awt.Color(248, 250, 252));
        AppBtn.setForeground(new java.awt.Color(0, 0, 0));
        AppBtn.setText("Appointments");
        AppBtn.addActionListener(this::AppBtnActionPerformed);

        QueBtn.setBackground(new java.awt.Color(248, 250, 252));
        QueBtn.setForeground(new java.awt.Color(0, 0, 0));
        QueBtn.setText("Queue");
        QueBtn.addActionListener(this::QueBtnActionPerformed);

        CslBtn.setBackground(new java.awt.Color(248, 250, 252));
        CslBtn.setForeground(new java.awt.Color(0, 0, 0));
        CslBtn.setText("Counselors");
        CslBtn.addActionListener(this::CslBtnActionPerformed);

        javax.swing.GroupLayout panelSidebarLayout = new javax.swing.GroupLayout(panelSidebar);
        panelSidebar.setLayout(panelSidebarLayout);
        panelSidebarLayout.setHorizontalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(37, 37, 37))
            .addComponent(AppBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(StdBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(DbdBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(QueBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(CslBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSidebarLayout.createSequentialGroup()
                .addContainerGap(102, Short.MAX_VALUE)
                .addGroup(panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSidebarLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(15, 15, 15))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSidebarLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(31, 31, 31))))
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(LogoutBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelSidebarLayout.setVerticalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGroup(panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSidebarLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSidebarLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel1)))
                .addGap(16, 16, 16)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DbdBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(StdBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(AppBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(QueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(CslBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77)
                .addComponent(LogoutBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(176, Short.MAX_VALUE))
        );

        tabbedMain.setBackground(new java.awt.Color(255, 255, 255));
        tabbedMain.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        tabbedMain.setPreferredSize(new java.awt.Dimension(1102, 650));
        tabbedMain.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tabbedMainFocusLost(evt);
            }
        });

        panelDashboard.setBackground(new java.awt.Color(248, 255, 252));
        panelDashboard.setPreferredSize(new java.awt.Dimension(772, 600));

        labelWelcome.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelWelcome.setForeground(new java.awt.Color(42, 74, 230));
        labelWelcome.setText("Welcome, Receptionist!");

        labelSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelSubtitle.setForeground(new java.awt.Color(71, 85, 105));
        labelSubtitle.setText("Manage students, appointments, counselors and queue numbers");

        panelStudentCard.setBackground(new java.awt.Color(255, 255, 255));
        panelStudentCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelStudentCard.setPreferredSize(new java.awt.Dimension(190, 110));

        labelStudentCount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelStudentCount.setForeground(new java.awt.Color(37, 99, 235));
        labelStudentCount.setText("0");

        labelStudentCardTitle.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelStudentCardTitle.setForeground(new java.awt.Color(71, 85, 105));
        labelStudentCardTitle.setText("Students");

        javax.swing.GroupLayout panelStudentCardLayout = new javax.swing.GroupLayout(panelStudentCard);
        panelStudentCard.setLayout(panelStudentCardLayout);
        panelStudentCardLayout.setHorizontalGroup(
            panelStudentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStudentCardLayout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(labelStudentCount)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelStudentCardLayout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addComponent(labelStudentCardTitle)
                .addGap(36, 36, 36))
        );
        panelStudentCardLayout.setVerticalGroup(
            panelStudentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStudentCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelStudentCount)
                .addGap(18, 18, 18)
                .addComponent(labelStudentCardTitle)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panelAppointmentCard.setBackground(new java.awt.Color(255, 255, 255));
        panelAppointmentCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelAppointmentCard.setPreferredSize(new java.awt.Dimension(190, 110));

        labelAppointmentCount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelAppointmentCount.setForeground(new java.awt.Color(22, 163, 74));
        labelAppointmentCount.setText("0");

        labelAppointmentCardTitle.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelAppointmentCardTitle.setForeground(new java.awt.Color(71, 85, 105));
        labelAppointmentCardTitle.setText("Appointments");

        javax.swing.GroupLayout panelAppointmentCardLayout = new javax.swing.GroupLayout(panelAppointmentCard);
        panelAppointmentCard.setLayout(panelAppointmentCardLayout);
        panelAppointmentCardLayout.setHorizontalGroup(
            panelAppointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAppointmentCardLayout.createSequentialGroup()
                .addGroup(panelAppointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelAppointmentCardLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(labelAppointmentCount))
                    .addGroup(panelAppointmentCardLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(labelAppointmentCardTitle)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelAppointmentCardLayout.setVerticalGroup(
            panelAppointmentCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAppointmentCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelAppointmentCount)
                .addGap(18, 18, 18)
                .addComponent(labelAppointmentCardTitle)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panelWaitingCard.setBackground(new java.awt.Color(255, 255, 255));
        panelWaitingCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelWaitingCard.setPreferredSize(new java.awt.Dimension(190, 110));

        labelWaitingCount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelWaitingCount.setForeground(new java.awt.Color(234, 88, 12));
        labelWaitingCount.setText("0");

        labelWaitingCardTitle.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelWaitingCardTitle.setForeground(new java.awt.Color(71, 85, 105));
        labelWaitingCardTitle.setText("Waiting");

        javax.swing.GroupLayout panelWaitingCardLayout = new javax.swing.GroupLayout(panelWaitingCard);
        panelWaitingCard.setLayout(panelWaitingCardLayout);
        panelWaitingCardLayout.setHorizontalGroup(
            panelWaitingCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWaitingCardLayout.createSequentialGroup()
                .addContainerGap(45, Short.MAX_VALUE)
                .addGroup(panelWaitingCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelWaitingCardLayout.createSequentialGroup()
                        .addComponent(labelWaitingCardTitle)
                        .addGap(40, 40, 40))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelWaitingCardLayout.createSequentialGroup()
                        .addComponent(labelWaitingCount)
                        .addGap(59, 59, 59))))
        );
        panelWaitingCardLayout.setVerticalGroup(
            panelWaitingCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWaitingCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelWaitingCount)
                .addGap(18, 18, 18)
                .addComponent(labelWaitingCardTitle)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panelCounselorCard.setBackground(new java.awt.Color(255, 255, 255));
        panelCounselorCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelCounselorCard.setPreferredSize(new java.awt.Dimension(190, 110));

        labelCounselorCount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelCounselorCount.setForeground(new java.awt.Color(124, 58, 237));
        labelCounselorCount.setText("0");

        labelCounselorsCardTitle.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelCounselorsCardTitle.setForeground(new java.awt.Color(71, 85, 105));
        labelCounselorsCardTitle.setText("Counselors");

        javax.swing.GroupLayout panelCounselorCardLayout = new javax.swing.GroupLayout(panelCounselorCard);
        panelCounselorCard.setLayout(panelCounselorCardLayout);
        panelCounselorCardLayout.setHorizontalGroup(
            panelCounselorCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCounselorCardLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(labelCounselorCount)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCounselorCardLayout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addComponent(labelCounselorsCardTitle)
                .addGap(31, 31, 31))
        );
        panelCounselorCardLayout.setVerticalGroup(
            panelCounselorCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCounselorCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelCounselorCount)
                .addGap(18, 18, 18)
                .addComponent(labelCounselorsCardTitle)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panelQuickActions.setBackground(new java.awt.Color(255, 255, 255));
        panelQuickActions.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelQuickActions.setPreferredSize(new java.awt.Dimension(390, 390));

        labelQuickActions.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        labelQuickActions.setForeground(new java.awt.Color(42, 74, 230));
        labelQuickActions.setText("QUICK ACTIONS");

        RegisterStudentBtn.setBackground(new java.awt.Color(255, 255, 255));
        RegisterStudentBtn.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        RegisterStudentBtn.setForeground(new java.awt.Color(30, 41, 59));
        RegisterStudentBtn.setText("Register New Student");
        RegisterStudentBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        RegisterStudentBtn.setPreferredSize(new java.awt.Dimension(340, 60));
        RegisterStudentBtn.addActionListener(this::RegisterStudentBtnActionPerformed);

        CreateWalkIBtn.setBackground(new java.awt.Color(255, 255, 255));
        CreateWalkIBtn.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        CreateWalkIBtn.setForeground(new java.awt.Color(30, 41, 59));
        CreateWalkIBtn.setText("Create Walk-in Appointment");
        CreateWalkIBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        CreateWalkIBtn.setPreferredSize(new java.awt.Dimension(340, 60));
        CreateWalkIBtn.addActionListener(this::CreateWalkIBtnActionPerformed);

        CreateOnlineBtn.setBackground(new java.awt.Color(255, 255, 255));
        CreateOnlineBtn.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        CreateOnlineBtn.setForeground(new java.awt.Color(30, 41, 59));
        CreateOnlineBtn.setText("Create Online Appointment");
        CreateOnlineBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        CreateOnlineBtn.setPreferredSize(new java.awt.Dimension(340, 60));
        CreateOnlineBtn.addActionListener(this::CreateOnlineBtnActionPerformed);

        ViewAppointmentsBtn.setBackground(new java.awt.Color(255, 255, 255));
        ViewAppointmentsBtn.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        ViewAppointmentsBtn.setForeground(new java.awt.Color(30, 41, 59));
        ViewAppointmentsBtn.setText("View All Appointments");
        ViewAppointmentsBtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ViewAppointmentsBtn.setPreferredSize(new java.awt.Dimension(340, 60));
        ViewAppointmentsBtn.addActionListener(this::ViewAppointmentsBtnActionPerformed);

        javax.swing.GroupLayout panelQuickActionsLayout = new javax.swing.GroupLayout(panelQuickActions);
        panelQuickActions.setLayout(panelQuickActionsLayout);
        panelQuickActionsLayout.setHorizontalGroup(
            panelQuickActionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelQuickActionsLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelQuickActionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ViewAppointmentsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelQuickActions)
                    .addComponent(RegisterStudentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelQuickActionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(CreateOnlineBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                        .addComponent(CreateWalkIBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelQuickActionsLayout.setVerticalGroup(
            panelQuickActionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelQuickActionsLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(labelQuickActions)
                .addGap(18, 18, 18)
                .addComponent(RegisterStudentBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CreateWalkIBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CreateOnlineBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ViewAppointmentsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelTodayQueue.setBackground(new java.awt.Color(255, 255, 255));
        panelTodayQueue.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelTodayQueue.setPreferredSize(new java.awt.Dimension(520, 390));

        labelTodayQueue.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        labelTodayQueue.setForeground(new java.awt.Color(42, 74, 230));
        labelTodayQueue.setText("TODAY'S QUEUE");

        tableTodayQueue.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Queue", "Student", "Time", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableTodayQueue.getTableHeader().setReorderingAllowed(false);
        scrollTodayQueue.setViewportView(tableTodayQueue);

        javax.swing.GroupLayout panelTodayQueueLayout = new javax.swing.GroupLayout(panelTodayQueue);
        panelTodayQueue.setLayout(panelTodayQueueLayout);
        panelTodayQueueLayout.setHorizontalGroup(
            panelTodayQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTodayQueueLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(labelTodayQueue)
                .addContainerGap(267, Short.MAX_VALUE))
            .addGroup(panelTodayQueueLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollTodayQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelTodayQueueLayout.setVerticalGroup(
            panelTodayQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTodayQueueLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(labelTodayQueue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTodayQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelDashboardLayout = new javax.swing.GroupLayout(panelDashboard);
        panelDashboard.setLayout(panelDashboardLayout);
        panelDashboardLayout.setHorizontalGroup(
            panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDashboardLayout.createSequentialGroup()
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDashboardLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(panelQuickActions, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelTodayQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDashboardLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDashboardLayout.createSequentialGroup()
                                .addComponent(panelStudentCard, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panelAppointmentCard, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panelWaitingCard, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panelCounselorCard, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(labelSubtitle)
                            .addComponent(labelWelcome))))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        panelDashboardLayout.setVerticalGroup(
            panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDashboardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(labelWelcome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelSubtitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelAppointmentCard, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelStudentCard, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelWaitingCard, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCounselorCard, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelTodayQueue, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                    .addComponent(panelQuickActions, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        tabbedMain.addTab("Dashboard", panelDashboard);

        panelStudents.setBackground(new java.awt.Color(248, 255, 252));
        panelStudents.setPreferredSize(new java.awt.Dimension(772, 650));

        labelStudentManagement.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelStudentManagement.setForeground(new java.awt.Color(42, 74, 230));
        labelStudentManagement.setText("Student Management");

        labelStudentSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelStudentSubtitle.setForeground(new java.awt.Color(71, 85, 105));
        labelStudentSubtitle.setText("Register, search and manage student accounts");

        panelRegisterStudent.setBackground(new java.awt.Color(255, 255, 255));
        panelRegisterStudent.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelRegisterStudent.setPreferredSize(new java.awt.Dimension(390, 520));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(42, 74, 230));
        jLabel7.setText("REGISTER NEW STUDENT");

        labelFullName.setText("Full Name");

        StdNameTf.addActionListener(this::StdNameTfActionPerformed);

        labelUsername.setText("Username");

        labelPassword.setText("Password");

        StdPasswordTf.addActionListener(this::StdPasswordTfActionPerformed);

        SaveBtn.setBackground(new java.awt.Color(37, 99, 235));
        SaveBtn.setForeground(new java.awt.Color(255, 255, 255));
        SaveBtn.setText("Save");
        SaveBtn.addActionListener(this::SaveBtnActionPerformed);

        ClearBtn.setText("Clear");
        ClearBtn.addActionListener(this::ClearBtnActionPerformed);

        javax.swing.GroupLayout panelRegisterStudentLayout = new javax.swing.GroupLayout(panelRegisterStudent);
        panelRegisterStudent.setLayout(panelRegisterStudentLayout);
        panelRegisterStudentLayout.setHorizontalGroup(
            panelRegisterStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegisterStudentLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelRegisterStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UserNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(StdNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelFullName)
                    .addComponent(labelUsername)
                    .addComponent(labelPassword)
                    .addComponent(StdPasswordTf, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelRegisterStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(panelRegisterStudentLayout.createSequentialGroup()
                            .addComponent(SaveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ClearBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jLabel7)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        panelRegisterStudentLayout.setVerticalGroup(
            panelRegisterStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRegisterStudentLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelFullName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StdNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(labelUsername)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UserNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(labelPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StdPasswordTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addGroup(panelRegisterStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SaveBtn)
                    .addComponent(ClearBtn))
                .addContainerGap(134, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        jPanel1.setPreferredSize(new java.awt.Dimension(600, 520));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(42, 74, 230));
        jLabel8.setText("STUDENT ACCOUNTS");

        SearchTf.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        SearchTf.addActionListener(this::SearchTfActionPerformed);

        SearchBtn.setText("Search");
        SearchBtn.addActionListener(this::SearchBtnActionPerformed);

        RefreshBtn.setBackground(new java.awt.Color(37, 99, 235));
        RefreshBtn.setForeground(new java.awt.Color(255, 255, 255));
        RefreshBtn.setText("Refresh");
        RefreshBtn.addActionListener(this::RefreshBtnActionPerformed);

        tableStudents.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Student ID", "Username", "Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableStudents.getTableHeader().setReorderingAllowed(false);
        scrollStudents.setViewportView(tableStudents);

        UpdateBtn.setText("Update");
        UpdateBtn.addActionListener(this::UpdateBtnActionPerformed);

        DeleteBtn.setText("Delete ");
        DeleteBtn.addActionListener(this::DeleteBtnActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollStudents, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(SearchTf)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(RefreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(SearchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(UpdateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(DeleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RefreshBtn)
                    .addComponent(SearchBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollStudents, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UpdateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DeleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47))
        );

        javax.swing.GroupLayout panelStudentsLayout = new javax.swing.GroupLayout(panelStudents);
        panelStudents.setLayout(panelStudentsLayout);
        panelStudentsLayout.setHorizontalGroup(
            panelStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStudentsLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(panelStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelStudentsLayout.createSequentialGroup()
                        .addComponent(panelRegisterStudent, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                        .addGap(484, 484, 484))
                    .addGroup(panelStudentsLayout.createSequentialGroup()
                        .addGroup(panelStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelStudentSubtitle)
                            .addComponent(labelStudentManagement)
                            .addGroup(panelStudentsLayout.createSequentialGroup()
                                .addGap(260, 260, 260)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        panelStudentsLayout.setVerticalGroup(
            panelStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStudentsLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(labelStudentManagement)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelStudentSubtitle)
                .addGap(18, 18, 18)
                .addGroup(panelStudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelRegisterStudent, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))
                .addGap(0, 35, Short.MAX_VALUE))
        );

        tabbedMain.addTab("Students", panelStudents);

        panelAppointments.setBackground(new java.awt.Color(248, 250, 252));
        panelAppointments.setPreferredSize(new java.awt.Dimension(1100, 650));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(42, 74, 230));
        jLabel9.setText("Appointment Management");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(71, 85, 105));
        jLabel10.setText("Create, view, and manage counseling appointments");

        panelCreateAppointment.setBackground(new java.awt.Color(255, 255, 255));
        panelCreateAppointment.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelCreateAppointment.setPreferredSize(new java.awt.Dimension(350, 600));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(42, 74, 230));
        jLabel11.setText("CREATE APPOINTMENT");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(30, 41, 59));
        jLabel12.setText("Student ID");

        AppStdTf.setPreferredSize(new java.awt.Dimension(290, 35));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(30, 41, 59));
        jLabel13.setText("Counselor");

        CounselorCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Counselor" }));
        CounselorCombo.setPreferredSize(new java.awt.Dimension(290, 35));
        CounselorCombo.addActionListener(this::CounselorComboActionPerformed);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(30, 41, 59));
        jLabel14.setText("Date (YYYY-MM-DD)");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(30, 41, 59));
        jLabel15.setText("Time (HH:MM)");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(30, 41, 59));
        jLabel16.setText("Appointment Type");

        AppTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Walk-in", "Online"}));
        AppTypeCombo.setPreferredSize(new java.awt.Dimension(290, 35));

        CreateAppBtn.setBackground(new java.awt.Color(59, 111, 246));
        CreateAppBtn.setForeground(new java.awt.Color(255, 255, 255));
        CreateAppBtn.setText("Create Appointment");
        CreateAppBtn.addActionListener(this::CreateAppBtnActionPerformed);

        ClearAppBtn.setText("Clear");
        ClearAppBtn.addActionListener(this::ClearAppBtnActionPerformed);

        jLabel5.setBackground(new java.awt.Color(0, 0, 0));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("-");

        jLabel6.setBackground(new java.awt.Color(0, 0, 0));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("-");

        jLabel30.setBackground(new java.awt.Color(0, 0, 0));
        jLabel30.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(0, 0, 0));
        jLabel30.setText("-");

        javax.swing.GroupLayout panelCreateAppointmentLayout = new javax.swing.GroupLayout(panelCreateAppointment);
        panelCreateAppointment.setLayout(panelCreateAppointmentLayout);
        panelCreateAppointmentLayout.setHorizontalGroup(
            panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AppStdTf, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel15)
                            .addComponent(jLabel11)
                            .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(AppTypeCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 161, Short.MAX_VALUE)
                                .addComponent(CounselorCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE))
                            .addComponent(jLabel14)
                            .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                                .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                                        .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel5))
                                    .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboDate, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                                .addComponent(comboHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel30)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboMinute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(CreateAppBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ClearAppBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelCreateAppointmentLayout.setVerticalGroup(
            panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCreateAppointmentLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AppStdTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CounselorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(comboYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(comboMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6)
                        .addComponent(comboDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30)
                    .addComponent(comboMinute, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AppTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(panelCreateAppointmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CreateAppBtn)
                    .addComponent(ClearAppBtn))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        panelAppointmentRecords.setBackground(new java.awt.Color(255, 255, 255));
        panelAppointmentRecords.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 225, 232)));
        panelAppointmentRecords.setPreferredSize(new java.awt.Dimension(700, 600));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(45, 72, 230));
        jLabel17.setText("APPOINTMENT RECORDS");

        SearchAppTf.setPreferredSize(new java.awt.Dimension(330, 35));

        SearchAppBtn.setBackground(new java.awt.Color(59, 111, 246));
        SearchAppBtn.setForeground(new java.awt.Color(255, 255, 255));
        SearchAppBtn.setText("Search");
        SearchAppBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        SearchAppBtn.addActionListener(this::SearchAppBtnActionPerformed);

        RefreshAppBtn.setForeground(new java.awt.Color(0, 0, 0));
        RefreshAppBtn.setText("Refresh");
        RefreshAppBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        RefreshAppBtn.addActionListener(this::RefreshAppBtnActionPerformed);

        scrollApp.setPreferredSize(new java.awt.Dimension(600, 480));

        AppTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Appointment ID", "Student ID", "Counselor", "Date", "Time", "Status", "Type", "Queue"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        AppTbl.setPreferredSize(new java.awt.Dimension(600, 480));
        AppTbl.getTableHeader().setReorderingAllowed(false);
        scrollApp.setViewportView(AppTbl);

        RescheduleBtn.setBackground(new java.awt.Color(59, 111, 246));
        RescheduleBtn.setForeground(new java.awt.Color(255, 255, 255));
        RescheduleBtn.setText("Reschedule");
        RescheduleBtn.setMinimumSize(new java.awt.Dimension(14, 14));
        RescheduleBtn.setPreferredSize(new java.awt.Dimension(143, 27));
        RescheduleBtn.addActionListener(this::RescheduleBtnActionPerformed);

        AssignBtn.setForeground(new java.awt.Color(255, 255, 255));
        AssignBtn.setText("Assign Counselor");
        AssignBtn.setPreferredSize(new java.awt.Dimension(143, 27));
        AssignBtn.addActionListener(this::AssignBtnActionPerformed);

        CancelBtn.setForeground(new java.awt.Color(220, 53, 69));
        CancelBtn.setText("Cancel");
        CancelBtn.setPreferredSize(new java.awt.Dimension(143, 27));
        CancelBtn.addActionListener(this::CancelBtnActionPerformed);

        javax.swing.GroupLayout panelAppointmentRecordsLayout = new javax.swing.GroupLayout(panelAppointmentRecords);
        panelAppointmentRecords.setLayout(panelAppointmentRecordsLayout);
        panelAppointmentRecordsLayout.setHorizontalGroup(
            panelAppointmentRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelAppointmentRecordsLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(SearchAppTf, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(SearchAppBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(RefreshAppBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(97, 97, 97))
            .addGroup(panelAppointmentRecordsLayout.createSequentialGroup()
                .addGroup(panelAppointmentRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelAppointmentRecordsLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel17))
                    .addGroup(panelAppointmentRecordsLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(scrollApp, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 19, Short.MAX_VALUE))
            .addGroup(panelAppointmentRecordsLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(RescheduleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(AssignBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelAppointmentRecordsLayout.setVerticalGroup(
            panelAppointmentRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAppointmentRecordsLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelAppointmentRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchAppTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SearchAppBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RefreshAppBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(scrollApp, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(panelAppointmentRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RescheduleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AssignBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CancelBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelAppointmentsLayout = new javax.swing.GroupLayout(panelAppointments);
        panelAppointments.setLayout(panelAppointmentsLayout);
        panelAppointmentsLayout.setHorizontalGroup(
            panelAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAppointmentsLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panelAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelAppointmentsLayout.createSequentialGroup()
                        .addComponent(panelCreateAppointment, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(panelAppointmentRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addContainerGap())
        );
        panelAppointmentsLayout.setVerticalGroup(
            panelAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAppointmentsLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelAppointmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelAppointmentRecords, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addComponent(panelCreateAppointment, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        tabbedMain.addTab("Appointments", panelAppointments);

        panelQueue.setBackground(new java.awt.Color(248, 250, 252));
        panelQueue.setPreferredSize(new java.awt.Dimension(1029, 650));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(45, 72, 230));
        jLabel18.setText("Queue Management");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(71, 85, 105));
        jLabel19.setText("View and manage upcoming appointment queue");

        panelTotalQueueCard.setBackground(new java.awt.Color(255, 255, 255));
        panelTotalQueueCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));

        labelTotalQueue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelTotalQueue.setForeground(new java.awt.Color(37, 99, 235));
        labelTotalQueue.setText("0");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(71, 85, 105));
        jLabel20.setText("Total Upcoming");

        javax.swing.GroupLayout panelTotalQueueCardLayout = new javax.swing.GroupLayout(panelTotalQueueCard);
        panelTotalQueueCard.setLayout(panelTotalQueueCardLayout);
        panelTotalQueueCardLayout.setHorizontalGroup(
            panelTotalQueueCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalQueueCardLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel20)
                .addContainerGap(14, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTotalQueueCardLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelTotalQueue)
                .addGap(62, 62, 62))
        );
        panelTotalQueueCardLayout.setVerticalGroup(
            panelTotalQueueCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalQueueCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTotalQueue)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        panelWaiting.setBackground(new java.awt.Color(255, 255, 255));
        panelWaiting.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelWaiting.setPreferredSize(new java.awt.Dimension(143, 108));

        labelOnlineQueue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelOnlineQueue.setForeground(new java.awt.Color(234, 88, 12));
        labelOnlineQueue.setText("0");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(71, 85, 105));
        jLabel21.setText("Online");

        javax.swing.GroupLayout panelWaitingLayout = new javax.swing.GroupLayout(panelWaiting);
        panelWaiting.setLayout(panelWaitingLayout);
        panelWaitingLayout.setHorizontalGroup(
            panelWaitingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWaitingLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelOnlineQueue)
                .addGap(51, 51, 51))
            .addGroup(panelWaitingLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel21)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        panelWaitingLayout.setVerticalGroup(
            panelWaitingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWaitingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelOnlineQueue)
                .addGap(18, 18, 18)
                .addComponent(jLabel21)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        panelCalledCard.setBackground(new java.awt.Color(255, 255, 255));
        panelCalledCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelCalledCard.setPreferredSize(new java.awt.Dimension(143, 108));

        labelWalkInQueue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelWalkInQueue.setForeground(new java.awt.Color(109, 40, 217));
        labelWalkInQueue.setText("0");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(71, 85, 105));
        jLabel22.setText("Walk-in");

        javax.swing.GroupLayout panelCalledCardLayout = new javax.swing.GroupLayout(panelCalledCard);
        panelCalledCard.setLayout(panelCalledCardLayout);
        panelCalledCardLayout.setHorizontalGroup(
            panelCalledCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCalledCardLayout.createSequentialGroup()
                .addGroup(panelCalledCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCalledCardLayout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jLabel22))
                    .addGroup(panelCalledCardLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(labelWalkInQueue)))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        panelCalledCardLayout.setVerticalGroup(
            panelCalledCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCalledCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelWalkInQueue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jLabel22)
                .addGap(20, 20, 20))
        );

        panelCompletedCard.setBackground(new java.awt.Color(255, 255, 255));
        panelCompletedCard.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelCompletedCard.setPreferredSize(new java.awt.Dimension(143, 108));

        labelPendingQueue.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelPendingQueue.setForeground(new java.awt.Color(22, 163, 74));
        labelPendingQueue.setText("0");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(71, 85, 105));
        jLabel23.setText("Pending");

        javax.swing.GroupLayout panelCompletedCardLayout = new javax.swing.GroupLayout(panelCompletedCard);
        panelCompletedCard.setLayout(panelCompletedCardLayout);
        panelCompletedCardLayout.setHorizontalGroup(
            panelCompletedCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCompletedCardLayout.createSequentialGroup()
                .addGroup(panelCompletedCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCompletedCardLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(labelPendingQueue))
                    .addGroup(panelCompletedCardLayout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jLabel23)))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        panelCompletedCardLayout.setVerticalGroup(
            panelCompletedCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCompletedCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelPendingQueue)
                .addGap(18, 18, 18)
                .addComponent(jLabel23)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        panelQueueRecords.setBackground(new java.awt.Color(255, 255, 255));
        panelQueueRecords.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 225, 232)));

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(42, 72, 230));
        jLabel24.setText("TODAY'S QUEUE");

        SearchQueTf.setPreferredSize(new java.awt.Dimension(450, 35));

        SearchQueBtn.setBackground(new java.awt.Color(59, 111, 246));
        SearchQueBtn.setForeground(new java.awt.Color(255, 255, 255));
        SearchQueBtn.setText("Search");
        SearchQueBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        SearchQueBtn.addActionListener(this::SearchQueBtnActionPerformed);

        RefreshQueBtn.setText("Refresh");
        RefreshQueBtn.setPreferredSize(new java.awt.Dimension(100, 35));
        RefreshQueBtn.addActionListener(this::RefreshQueBtnActionPerformed);

        QueueTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Queue", "Appointment ID", "Student", "Counselor", "Date", "Time", "Type", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        QueueTbl.getTableHeader().setReorderingAllowed(false);
        scrollQueue.setViewportView(QueueTbl);
        if (QueueTbl.getColumnModel().getColumnCount() > 0) {
            QueueTbl.getColumnModel().getColumn(0).setPreferredWidth(70);
            QueueTbl.getColumnModel().getColumn(1).setPreferredWidth(170);
            QueueTbl.getColumnModel().getColumn(2).setPreferredWidth(220);
            QueueTbl.getColumnModel().getColumn(3).setPreferredWidth(220);
            QueueTbl.getColumnModel().getColumn(4).setPreferredWidth(140);
            QueueTbl.getColumnModel().getColumn(5).setPreferredWidth(150);
        }

        javax.swing.GroupLayout panelQueueRecordsLayout = new javax.swing.GroupLayout(panelQueueRecords);
        panelQueueRecords.setLayout(panelQueueRecordsLayout);
        panelQueueRecordsLayout.setHorizontalGroup(
            panelQueueRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelQueueRecordsLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(panelQueueRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addGroup(panelQueueRecordsLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panelQueueRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrollQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 645, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelQueueRecordsLayout.createSequentialGroup()
                                .addComponent(SearchQueTf, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(SearchQueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RefreshQueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(9, Short.MAX_VALUE))
        );
        panelQueueRecordsLayout.setVerticalGroup(
            panelQueueRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelQueueRecordsLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelQueueRecordsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchQueTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SearchQueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RefreshQueBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollQueue, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelQueueLayout = new javax.swing.GroupLayout(panelQueue);
        panelQueue.setLayout(panelQueueLayout);
        panelQueueLayout.setHorizontalGroup(
            panelQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelQueueLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(panelQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelQueueLayout.createSequentialGroup()
                        .addComponent(panelTotalQueueCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelWaiting, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelCalledCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelCompletedCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel19)
                    .addComponent(jLabel18)
                    .addComponent(panelQueueRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        panelQueueLayout.setVerticalGroup(
            panelQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelQueueLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelQueueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelWaiting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelTotalQueueCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCalledCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCompletedCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelQueueRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        tabbedMain.addTab("Queue", panelQueue);

        panelCounselors.setBackground(new java.awt.Color(248, 250, 252));
        panelCounselors.setPreferredSize(new java.awt.Dimension(1046, 650));

        labelCounselorTitle.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelCounselorTitle.setForeground(new java.awt.Color(45, 72, 230));
        labelCounselorTitle.setText("Counselor Directory");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(71, 85, 105));
        jLabel25.setText("View counselor information and appointment schedules");

        panelTotalCounselorsCount.setBackground(new java.awt.Color(255, 255, 255));
        panelTotalCounselorsCount.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelTotalCounselorsCount.setPreferredSize(new java.awt.Dimension(250, 120));

        labelTotalCounselors.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelTotalCounselors.setForeground(new java.awt.Color(37, 99, 235));
        labelTotalCounselors.setText("0");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(71, 85, 105));
        jLabel26.setText("Total Counselors");

        javax.swing.GroupLayout panelTotalCounselorsCountLayout = new javax.swing.GroupLayout(panelTotalCounselorsCount);
        panelTotalCounselorsCount.setLayout(panelTotalCounselorsCountLayout);
        panelTotalCounselorsCountLayout.setHorizontalGroup(
            panelTotalCounselorsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalCounselorsCountLayout.createSequentialGroup()
                .addGroup(panelTotalCounselorsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTotalCounselorsCountLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel26))
                    .addGroup(panelTotalCounselorsCountLayout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(labelTotalCounselors)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        panelTotalCounselorsCountLayout.setVerticalGroup(
            panelTotalCounselorsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalCounselorsCountLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTotalCounselors)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel26)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        panelAvailableCounselorsCount.setBackground(new java.awt.Color(255, 255, 255));
        panelAvailableCounselorsCount.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(215, 222, 232)));
        panelAvailableCounselorsCount.setPreferredSize(new java.awt.Dimension(250, 120));

        labelAvailableCounselors.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        labelAvailableCounselors.setForeground(new java.awt.Color(22, 163, 74));
        labelAvailableCounselors.setText("0");

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(71, 85, 105));
        jLabel27.setText("Available Today");

        javax.swing.GroupLayout panelAvailableCounselorsCountLayout = new javax.swing.GroupLayout(panelAvailableCounselorsCount);
        panelAvailableCounselorsCount.setLayout(panelAvailableCounselorsCountLayout);
        panelAvailableCounselorsCountLayout.setHorizontalGroup(
            panelAvailableCounselorsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAvailableCounselorsCountLayout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(labelAvailableCounselors)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelAvailableCounselorsCountLayout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addComponent(jLabel27)
                .addGap(35, 35, 35))
        );
        panelAvailableCounselorsCountLayout.setVerticalGroup(
            panelAvailableCounselorsCountLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAvailableCounselorsCountLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelAvailableCounselors)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel27)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 225, 232)));
        jPanel2.setPreferredSize(new java.awt.Dimension(580, 560));

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(42, 72, 230));
        jLabel28.setText("COUNSELOR LIST");

        SearchCslBtn.setBackground(new java.awt.Color(59, 111, 246));
        SearchCslBtn.setForeground(new java.awt.Color(255, 255, 255));
        SearchCslBtn.setText("Search");
        SearchCslBtn.addActionListener(this::SearchCslBtnActionPerformed);

        RefreshCslBtn.setText("Refresh");
        RefreshCslBtn.addActionListener(this::RefreshCslBtnActionPerformed);

        CslTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Counselor ID", "Name", "Username", "Today's Appointments", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        CslTbl.getTableHeader().setReorderingAllowed(false);
        scrollCsl.setViewportView(CslTbl);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollCsl, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel28)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(SearchCslTf, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(SearchCslBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(RefreshCslBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchCslTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SearchCslBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RefreshCslBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollCsl, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        panelCounselorSchedule.setBackground(new java.awt.Color(255, 255, 255));
        panelCounselorSchedule.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 225, 232)));

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(42, 72, 230));
        jLabel29.setText("COUNSELOR SCHEDULE");

        labelSelectedCsl.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        labelSelectedCsl.setForeground(new java.awt.Color(71, 85, 105));
        labelSelectedCsl.setText("Select a counselor to view their schedule");

        CslScheduleTbl.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Time", "Student", "Appointment ID", "Type", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        CslScheduleTbl.getTableHeader().setReorderingAllowed(false);
        scrollCslSchedule.setViewportView(CslScheduleTbl);

        ViewScheduleBtn.setBackground(new java.awt.Color(37, 99, 235));
        ViewScheduleBtn.setForeground(new java.awt.Color(255, 255, 255));
        ViewScheduleBtn.setText("View Schedule");
        ViewScheduleBtn.addActionListener(this::ViewScheduleBtnActionPerformed);

        javax.swing.GroupLayout panelCounselorScheduleLayout = new javax.swing.GroupLayout(panelCounselorSchedule);
        panelCounselorSchedule.setLayout(panelCounselorScheduleLayout);
        panelCounselorScheduleLayout.setHorizontalGroup(
            panelCounselorScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCounselorScheduleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollCslSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panelCounselorScheduleLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panelCounselorScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelSelectedCsl)
                    .addComponent(jLabel29))
                .addContainerGap(55, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCounselorScheduleLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ViewScheduleBtn)
                .addGap(108, 108, 108))
        );
        panelCounselorScheduleLayout.setVerticalGroup(
            panelCounselorScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCounselorScheduleLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelSelectedCsl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollCslSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(ViewScheduleBtn)
                .addContainerGap(117, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelCounselorsLayout = new javax.swing.GroupLayout(panelCounselors);
        panelCounselors.setLayout(panelCounselorsLayout);
        panelCounselorsLayout.setHorizontalGroup(
            panelCounselorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCounselorsLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(panelCounselorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25)
                    .addComponent(labelCounselorTitle)
                    .addGroup(panelCounselorsLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelCounselorSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCounselorsLayout.createSequentialGroup()
                        .addComponent(panelTotalCounselorsCount, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelAvailableCounselorsCount, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        panelCounselorsLayout.setVerticalGroup(
            panelCounselorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCounselorsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(labelCounselorTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25)
                .addGap(18, 18, 18)
                .addGroup(panelCounselorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelTotalCounselorsCount, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelAvailableCounselorsCount, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCounselorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCounselorSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedMain.addTab("Counselors", panelCounselors);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedMain, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelSidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 719, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tabbedMain, javax.swing.GroupLayout.PREFERRED_SIZE, 623, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 106, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void StdBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StdBtnActionPerformed
        // TODO add your handling code here:
        loadStudentsTable();
        tabbedMain.setSelectedIndex(1);
    }//GEN-LAST:event_StdBtnActionPerformed

    private void DbdBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DbdBtnActionPerformed
        // TODO add your handling code here:       
        loadDashboardData();
        tabbedMain.setSelectedIndex(0); 
    }//GEN-LAST:event_DbdBtnActionPerformed

    private void AppBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AppBtnActionPerformed
        // TODO add your handling code here:
        tabbedMain.setSelectedIndex(2);
    }//GEN-LAST:event_AppBtnActionPerformed

    private void QueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_QueBtnActionPerformed
        // TODO add your handling code here:        
        loadQueueData();
        tabbedMain.setSelectedIndex(3);
    }//GEN-LAST:event_QueBtnActionPerformed

    private void CslBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CslBtnActionPerformed
        // TODO add your handling code here:       
        loadCounselorsTable();
        tabbedMain.setSelectedIndex(4);
    }//GEN-LAST:event_CslBtnActionPerformed

    private void LogoutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutBtnActionPerformed
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
    }//GEN-LAST:event_LogoutBtnActionPerformed

    private void tabbedMainFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tabbedMainFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_tabbedMainFocusLost

    private void CreateOnlineBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateOnlineBtnActionPerformed
        // TODO add your handling code here:
        tabbedMain.setSelectedIndex(2);
        AppTypeCombo.setSelectedItem("Online");
        AppStdTf.requestFocus();
    }//GEN-LAST:event_CreateOnlineBtnActionPerformed

    private void ViewAppointmentsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewAppointmentsBtnActionPerformed
        // TODO add your handling code here:
        loadAppointmentsTable();
        tabbedMain.setSelectedIndex(2);
    }//GEN-LAST:event_ViewAppointmentsBtnActionPerformed

    private void RegisterStudentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RegisterStudentBtnActionPerformed
        // TODO add your handling code here:
        tabbedMain.setSelectedIndex(1);
        StdNameTf.setText("");
        UserNameTf.setText("");
        StdPasswordTf.setText("");
        StdNameTf.requestFocus();
    }//GEN-LAST:event_RegisterStudentBtnActionPerformed

    private void CreateWalkIBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateWalkIBtnActionPerformed
        // TODO add your handling code here:
        tabbedMain.setSelectedIndex(2);
        AppTypeCombo.setSelectedItem("Walk-in");
        AppStdTf.requestFocus();
    }//GEN-LAST:event_CreateWalkIBtnActionPerformed

    private void StdNameTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StdNameTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_StdNameTfActionPerformed

    private void StdPasswordTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StdPasswordTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_StdPasswordTfActionPerformed

    private void SearchTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchTfActionPerformed
        // TODO add your handling code here:        
         SearchBtn.doClick();
    }//GEN-LAST:event_SearchTfActionPerformed

    private void SearchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchBtnActionPerformed
        // TODO add your handling code here:       
        String keyword = SearchTf.getText().trim();

        if (keyword.isEmpty()) {
            loadStudentsTable();
            return;
        }

        searchStudentsTable(keyword);
    }//GEN-LAST:event_SearchBtnActionPerformed

    private void RefreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshBtnActionPerformed
        // TODO add your handling code here:       
        SearchTf.setText("");
        loadStudentsTable();
    }//GEN-LAST:event_RefreshBtnActionPerformed

    private void SaveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveBtnActionPerformed
        // TODO add your handling code here:
        String name = StdNameTf.getText().trim();
        String username = UserNameTf.getText().trim();
        String password = new String(StdPasswordTf.getPassword()).trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete all student fields.", "Missing Information", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = receptionist.registerStudentAccount(username, password, name, fileManager);

        if (student != null) {
            JOptionPane.showMessageDialog(this, "Student registered successfully.\n" 
                    + "Student ID: " + student.getUserID());

            clearStudentFields();
            loadStudentsTable();
            loadDashboardData();
        }else{
            JOptionPane.showMessageDialog(this, "Registration failed. " 
                    + "The username may already exist.", "Registration Failed", 
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_SaveBtnActionPerformed

    private void ClearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearBtnActionPerformed
        // TODO add your handling code here:       
        clearStudentFields();
    }//GEN-LAST:event_ClearBtnActionPerformed

    private void UpdateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateBtnActionPerformed
        // TODO add your handling code here:        
        int selectedRow = tableStudents.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", 
                    "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentID = tableStudents.getValueAt(selectedRow, 0).toString();

        ArrayList<User> users =fileManager.getAllUsers();

        User selectedStudent = null;

        for (User user : users) {
            if (user instanceof Student && user.getUserID().equalsIgnoreCase(studentID)) {
            selectedStudent = user;
            break;
            }
        }   

        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Student could not be found.", 
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newName = JOptionPane.showInputDialog(this, "Enter the new student name:", selectedStudent.getName());

        // Cancel was pressed
        if (newName == null) {
            return;
        }

        newName = newName.trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student name cannot be empty.", 
                    "Invalid Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JPasswordField passwordField = new javax.swing.JPasswordField();

        int passwordChoice = JOptionPane.showConfirmDialog(this, passwordField, 
                "Enter a new password " + "(leave empty to keep the current password)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (passwordChoice != JOptionPane.OK_OPTION) {
            return;
        }

        String newPassword = new String(passwordField.getPassword()).trim();

        selectedStudent.setName(newName);

        if (!newPassword.isEmpty()) {
            selectedStudent.setPassword(newPassword);
        }

        fileManager.saveAllUsers(users);

        loadStudentsTable();
        loadDashboardData();

        JOptionPane.showMessageDialog(this, "Student updated successfully.");
    }//GEN-LAST:event_UpdateBtnActionPerformed

    private void DeleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteBtnActionPerformed
        // TODO add your handling code here:        
        int selectedRow = tableStudents.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.",
                    "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentID = tableStudents.getValueAt(selectedRow, 0).toString();

        // Prevent appointments from referring to a deleted student
        for (Appointment appointment : fileManager.getAllAppointments()) {
            if (appointment.getStudentID().equalsIgnoreCase(studentID)) {
                JOptionPane.showMessageDialog(this, "This student has appointment records " 
                        + "and cannot be deleted.", "Delete Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
            }
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "Delete student " 
                + studentID + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        ArrayList<User> users = fileManager.getAllUsers();

        User studentToDelete = null;

        for (User user : users) {
            if (user instanceof Student && user.getUserID().equalsIgnoreCase(studentID)) {
                studentToDelete = user;
                break;
            }
        }

        if (studentToDelete == null) {
            JOptionPane.showMessageDialog(this, "Student could not be found.", 
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        users.remove(studentToDelete);
        fileManager.saveAllUsers(users);

        loadStudentsTable();
        loadDashboardData();

        JOptionPane.showMessageDialog(this, "Student deleted successfully.");
    }//GEN-LAST:event_DeleteBtnActionPerformed

    private void CounselorComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CounselorComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CounselorComboActionPerformed

    private void ClearAppBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearAppBtnActionPerformed
        // TODO add your handling code here:       
        AppStdTf.setText("");
        CounselorCombo.setSelectedIndex(0);
        comboYear.setSelectedIndex(0);
        comboMonth.setSelectedIndex(0);
        comboDate.setSelectedIndex(0);
        comboHour.setSelectedIndex(0);
        comboMinute.setSelectedIndex(0);
        AppTypeCombo.setSelectedIndex(0);
        AppStdTf.requestFocus();
    }//GEN-LAST:event_ClearAppBtnActionPerformed

    private void CreateAppBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateAppBtnActionPerformed
        // TODO add your handling code here:
        
        String studentID = AppStdTf.getText().trim();
        String counselorSelection = String.valueOf(CounselorCombo.getSelectedItem());
        String date = comboYear.getSelectedItem() + "-" + comboMonth.getSelectedItem() + "-" + comboDate.getSelectedItem();
        String time = comboHour.getSelectedItem() + ":" + comboMinute.getSelectedItem();
        String type = String.valueOf(AppTypeCombo.getSelectedItem());

        // Check empty fields
        if (studentID.isEmpty() || date.isEmpty() || time.isEmpty()|| CounselorCombo.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this,"Please complete all appointment fields.");
            return;
        }

        // Check whether student exists
        boolean studentExists = false;

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Student && user.getUserID().equalsIgnoreCase(studentID)) {
                studentExists = true;
                break;
            }
        }

        if (!studentExists) {
            JOptionPane.showMessageDialog(this,"Student ID does not exist.");
            return;
        }

        // Validate date and time formats
        
        LocalDate parsedDate;
        LocalTime parsedTime;
        
        try {
            parsedDate = LocalDate.parse(date);
            parsedTime = LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,"Use date YYYY-MM-DD and time HH:MM.");
            return;
        }
        
        LocalDate today = LocalDate.now();
        if (parsedDate.isBefore(today) || (parsedDate.isEqual(today) && parsedTime.isBefore(LocalTime.now()))) {
            JOptionPane.showMessageDialog(this,
                    "Cannot create an appointment in the past. Please choose a current or future date/time.");
            return;
        }

        String counselorID = counselorSelection.split(" - ", 2)[0];
        
        if (hasCounselorTimeConflict(counselorID, date, time, null)) {

            JOptionPane.showMessageDialog(this, "This counselor already has an appointment " 
                    + "at the selected date and time.\n" + "Please choose another counselor or time.");
            return;
        }
        
        Appointment appointment;

        if (type.equalsIgnoreCase("Online")) {
            appointment = receptionist.createAppointment(
                studentID,
                counselorID,
                date,
                time,
                true,
                fileManager
            );
        } else {  
            
            int newQueueNumber = generateNextQueueNumber(date);
            
            appointment = receptionist.createAppointment(
                studentID,
                counselorID,
                date,
                time,
                fileManager
            );
        
        
        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        for (Appointment savedAppointment : appointments){
            if (savedAppointment.getAppointmentID().equalsIgnoreCase(appointment.getAppointmentID())){

                savedAppointment.setQueueNumber(newQueueNumber);
                appointment.setQueueNumber(newQueueNumber);

                break;
            }
        }

        fileManager.saveAllAppointments(appointments);

        // Update the returned object used by the message
        appointment.setQueueNumber(newQueueNumber);
        }

        String successMessage = "Appointment created successfully!\n"
                + "Appointment ID: " + appointment.getAppointmentID() 
                + "\nType: " + appointment.getType();
        
        if (appointment.getType().equalsIgnoreCase("Walk-in")){
            successMessage +=  "\nQueue number: " + appointment.getQueueNumber();
        }else{
            successMessage += "\nQueue number: Not assigned";
        }

        JOptionPane.showMessageDialog(
                this,
                successMessage
        );

        loadAppointmentsTable();
        loadDashboardData();
        loadQueueData();
        loadCounselorsTable();

        AppStdTf.setText("");
        CounselorCombo.setSelectedIndex(0);
        comboYear.setSelectedIndex(0);
        comboMonth.setSelectedIndex(0);
        comboDate.setSelectedIndex(0);
        comboHour.setSelectedIndex(0);
        comboMinute.setSelectedIndex(0);
        AppTypeCombo.setSelectedIndex(0);
        
        AppStdTf.requestFocus();
    }//GEN-LAST:event_CreateAppBtnActionPerformed

    private void RefreshAppBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshAppBtnActionPerformed
        // TODO add your handling code here:       
        SearchAppTf.setText("");
        loadAppointmentsTable();
    }//GEN-LAST:event_RefreshAppBtnActionPerformed

    private void SearchAppBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchAppBtnActionPerformed
        // TODO add your handling code here:        
        String keyword = SearchAppTf.getText().trim();

        if (keyword.isEmpty()) {
            loadAppointmentsTable();
        } else {
            searchAppointmentsTable(keyword);
        }
    }//GEN-LAST:event_SearchAppBtnActionPerformed

    private void CancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelBtnActionPerformed
        // TODO add your handling code here:        
        int selectedRow = AppTbl.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,"Please select an appointment first.");
            return;
        }

        String appointmentID = AppTbl.getValueAt(selectedRow, 0).toString();

        String currentStatus = AppTbl.getValueAt(selectedRow, 5).toString();

        if (currentStatus.equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this,"This appointment is already cancelled.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Cancel appointment " + appointmentID + "?",
                "Confirm Cancellation",JOptionPane.YES_NO_OPTION);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        boolean cancelled = receptionist.cancelAppointment(appointmentID,fileManager);

        if (cancelled) {
            JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");

            loadAppointmentsTable();
            loadDashboardData();
        } else {
            JOptionPane.showMessageDialog(this, "Appointment could not be cancelled.");
        }
    }//GEN-LAST:event_CancelBtnActionPerformed

    private void RescheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RescheduleBtnActionPerformed
        // TODO add your handling code here:        
       int selectedRow = AppTbl.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,"Please select an appointment first.");
            return;
        }

        String appointmentID = AppTbl.getValueAt(selectedRow, 0).toString();
        String counselorID = AppTbl.getValueAt(selectedRow, 2).toString();
        String currentDate = AppTbl.getValueAt(selectedRow, 3).toString();
        String currentTime = AppTbl.getValueAt(selectedRow, 4).toString();
        String status = AppTbl.getValueAt(selectedRow, 5).toString();

        if (status.equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this, "A cancelled appointment cannot be rescheduled.");
            return;
        }

        String newDate = JOptionPane.showInputDialog(this, "Enter new date (YYYY-MM-DD):", currentDate);

        // User pressed Cancel
        if (newDate == null) {
            return;
        }

        String newTime = JOptionPane.showInputDialog(this, "Enter new time (HH:MM):", currentTime);

        // User pressed Cancel
        if (newTime == null) {
            return;
        }

        newDate = newDate.trim();
        newTime = newTime.trim();

        if (newDate.isEmpty() || newTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date and time cannot be empty.");
            return;
        }

        try {
            LocalDate.parse(newDate);
            LocalTime.parse(newTime);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Use date YYYY-MM-DD and time HH:MM.");
            return;
        }
        
        if (hasCounselorTimeConflict(counselorID, newDate, newTime, appointmentID)) {

            JOptionPane.showMessageDialog(this, "This counselor already has another appointment "
                    + "at the selected date and time.\n" + "Please choose a different time.");
            return;
        }

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        boolean updated = false;

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentID().equalsIgnoreCase(appointmentID)) {
                appointment.setDate(newDate);
                appointment.setTime(newTime);

                System.out.println("Updated: " + appointment.getAppointmentID() 
                        + " | " + appointment.getDate() + " | " + appointment.getTime());

                updated = true;
                break;
            }
        }

        if (updated) {
            fileManager.saveAllAppointments(appointments);

            JOptionPane.showMessageDialog(this, "Appointment rescheduled to:\n" + newDate + " at " + newTime);

            loadAppointmentsTable();
            loadDashboardData();
            loadQueueData();
        } else {
            JOptionPane.showMessageDialog(this, "Appointment could not be found.");
        }
    }//GEN-LAST:event_RescheduleBtnActionPerformed

    private void AssignBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AssignBtnActionPerformed
        // TODO add your handling code here:      
        int selectedRow = AppTbl.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        String appointmentID = AppTbl.getValueAt(selectedRow, 0).toString();
        String status = AppTbl.getValueAt(selectedRow, 5).toString();

        if (status.equalsIgnoreCase("Cancelled")) {
            JOptionPane.showMessageDialog(this, "Cannot assign a counselor to a cancelled appointment.");
            return;
        }

        ArrayList<String> counselorOptions = new ArrayList<>();

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Counselor) {
                counselorOptions.add(user.getUserID() + " - " + user.getName());
            }
        }

        if (counselorOptions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No counselors are available.");
            return;
        }

        String selection = (String) JOptionPane.showInputDialog(this, 
                "Select the new counselor:", "Assign Counselor", 
                JOptionPane.QUESTION_MESSAGE, null, counselorOptions.toArray(),
                counselorOptions.get(0));

        // User pressed Cancel
        if (selection == null) {
            return;
        }

        String counselorID = selection.split(" - ", 2)[0];

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        boolean updated = false;

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentID().equalsIgnoreCase(appointmentID)) {

                appointment.setCounselorID(counselorID);
                updated = true;
                break;
            }
        }

        if (updated) {
            fileManager.saveAllAppointments(appointments);
            JOptionPane.showMessageDialog(this, "Counselor assigned successfully.");

            loadAppointmentsTable();
            loadDashboardData();
        } else {
            JOptionPane.showMessageDialog(this, "Appointment could not be found.");
        }
    }//GEN-LAST:event_AssignBtnActionPerformed

    private void SearchQueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchQueBtnActionPerformed
        // TODO add your handling code here:       
        String keyword = SearchQueTf.getText().trim();

        if (keyword.isEmpty()) {
            loadQueueData();
        } else {
            searchQueueTable(keyword);
        }
    }//GEN-LAST:event_SearchQueBtnActionPerformed

    private void RefreshQueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshQueBtnActionPerformed
        // TODO add your handling code here:        
        SearchQueTf.setText("");
        loadQueueData();
    }//GEN-LAST:event_RefreshQueBtnActionPerformed

    private void SearchCslBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchCslBtnActionPerformed
        // TODO add your handling code here:
        
        String keyword = SearchCslTf.getText().trim();

        if (keyword.isEmpty()) {
            loadCounselorsTable();
        } else {
            searchCounselorsTable(keyword);
        }
    }//GEN-LAST:event_SearchCslBtnActionPerformed

    private void RefreshCslBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshCslBtnActionPerformed
        // TODO add your handling code here:       
        SearchCslTf.setText("");
        labelSelectedCsl.setText("Select a counselor to view their schedule");

        DefaultTableModel scheduleModel = (DefaultTableModel) CslScheduleTbl.getModel();
        scheduleModel.setRowCount(0);

        loadCounselorsTable();
    }//GEN-LAST:event_RefreshCslBtnActionPerformed

    private void ViewScheduleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewScheduleBtnActionPerformed
        // TODO add your handling code here:        
        int selectedRow = CslTbl.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a counselor first.");
            return;
        }

        String counselorID = CslTbl.getValueAt(selectedRow, 0).toString();
        String counselorName = CslTbl.getValueAt(selectedRow, 1).toString();

        labelSelectedCsl.setText(counselorID + " - " + counselorName + " | Today's Schedule");

        DefaultTableModel model = (DefaultTableModel) CslScheduleTbl.getModel();
        model.setRowCount(0);

        String today = LocalDate.now().toString();

        for (Appointment appointment : fileManager.getAllAppointments()) {
            boolean sameCounselor = appointment.getCounselorID().equalsIgnoreCase(counselorID);
            boolean isToday = appointment.getDate().equals(today);
            boolean isCancelled = appointment.getStatus().equalsIgnoreCase("Cancelled");
            if (sameCounselor && isToday && !isCancelled) {
                model.addRow(new Object[]{
                appointment.getTime(),
                findStudentName(appointment.getStudentID()),
                appointment.getAppointmentID(),
                appointment.getType(),
                appointment.getStatus()
            });
        }
    }

    if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(
                this,
                counselorName
                + " has no appointments scheduled for today."
        );
    }
    }//GEN-LAST:event_ViewScheduleBtnActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new ReceptionistDashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AppBtn;
    private javax.swing.JTextField AppStdTf;
    private javax.swing.JTable AppTbl;
    private javax.swing.JComboBox<String> AppTypeCombo;
    private javax.swing.JButton AssignBtn;
    private javax.swing.JButton CancelBtn;
    private javax.swing.JButton ClearAppBtn;
    private javax.swing.JButton ClearBtn;
    private javax.swing.JComboBox<String> CounselorCombo;
    private javax.swing.JButton CreateAppBtn;
    private javax.swing.JButton CreateOnlineBtn;
    private javax.swing.JButton CreateWalkIBtn;
    private javax.swing.JButton CslBtn;
    private javax.swing.JTable CslScheduleTbl;
    private javax.swing.JTable CslTbl;
    private javax.swing.JButton DbdBtn;
    private javax.swing.JButton DeleteBtn;
    private javax.swing.JButton LogoutBtn;
    private javax.swing.JButton QueBtn;
    private javax.swing.JTable QueueTbl;
    private javax.swing.JButton RefreshAppBtn;
    private javax.swing.JButton RefreshBtn;
    private javax.swing.JButton RefreshCslBtn;
    private javax.swing.JButton RefreshQueBtn;
    private javax.swing.JButton RegisterStudentBtn;
    private javax.swing.JButton RescheduleBtn;
    private javax.swing.JButton SaveBtn;
    private javax.swing.JButton SearchAppBtn;
    private javax.swing.JTextField SearchAppTf;
    private javax.swing.JButton SearchBtn;
    private javax.swing.JButton SearchCslBtn;
    private javax.swing.JTextField SearchCslTf;
    private javax.swing.JButton SearchQueBtn;
    private javax.swing.JTextField SearchQueTf;
    private javax.swing.JTextField SearchTf;
    private javax.swing.JButton StdBtn;
    private javax.swing.JTextField StdNameTf;
    private javax.swing.JPasswordField StdPasswordTf;
    private javax.swing.JButton UpdateBtn;
    private javax.swing.JTextField UserNameTf;
    private javax.swing.JButton ViewAppointmentsBtn;
    private javax.swing.JButton ViewScheduleBtn;
    private javax.swing.JComboBox<String> comboDate;
    private javax.swing.JComboBox<String> comboHour;
    private javax.swing.JComboBox<String> comboMinute;
    private javax.swing.JComboBox<String> comboMonth;
    private javax.swing.JComboBox<String> comboYear;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labelAppointmentCardTitle;
    private javax.swing.JLabel labelAppointmentCount;
    private javax.swing.JLabel labelAvailableCounselors;
    private javax.swing.JLabel labelCounselorCount;
    private javax.swing.JLabel labelCounselorTitle;
    private javax.swing.JLabel labelCounselorsCardTitle;
    private javax.swing.JLabel labelFullName;
    private javax.swing.JLabel labelOnlineQueue;
    private javax.swing.JLabel labelPassword;
    private javax.swing.JLabel labelPendingQueue;
    private javax.swing.JLabel labelQuickActions;
    private javax.swing.JLabel labelSelectedCsl;
    private javax.swing.JLabel labelStudentCardTitle;
    private javax.swing.JLabel labelStudentCount;
    private javax.swing.JLabel labelStudentManagement;
    private javax.swing.JLabel labelStudentSubtitle;
    private javax.swing.JLabel labelSubtitle;
    private javax.swing.JLabel labelTodayQueue;
    private javax.swing.JLabel labelTotalCounselors;
    private javax.swing.JLabel labelTotalQueue;
    private javax.swing.JLabel labelUsername;
    private javax.swing.JLabel labelWaitingCardTitle;
    private javax.swing.JLabel labelWaitingCount;
    private javax.swing.JLabel labelWalkInQueue;
    private javax.swing.JLabel labelWelcome;
    private javax.swing.JPanel panelAppointmentCard;
    private javax.swing.JPanel panelAppointmentRecords;
    private javax.swing.JPanel panelAppointments;
    private javax.swing.JPanel panelAvailableCounselorsCount;
    private javax.swing.JPanel panelCalledCard;
    private javax.swing.JPanel panelCompletedCard;
    private javax.swing.JPanel panelCounselorCard;
    private javax.swing.JPanel panelCounselorSchedule;
    private javax.swing.JPanel panelCounselors;
    private javax.swing.JPanel panelCreateAppointment;
    private javax.swing.JPanel panelDashboard;
    private javax.swing.JPanel panelQueue;
    private javax.swing.JPanel panelQueueRecords;
    private javax.swing.JPanel panelQuickActions;
    private javax.swing.JPanel panelRegisterStudent;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelStudentCard;
    private javax.swing.JPanel panelStudents;
    private javax.swing.JPanel panelTodayQueue;
    private javax.swing.JPanel panelTotalCounselorsCount;
    private javax.swing.JPanel panelTotalQueueCard;
    private javax.swing.JPanel panelWaiting;
    private javax.swing.JPanel panelWaitingCard;
    private javax.swing.JScrollPane scrollApp;
    private javax.swing.JScrollPane scrollCsl;
    private javax.swing.JScrollPane scrollCslSchedule;
    private javax.swing.JScrollPane scrollQueue;
    private javax.swing.JScrollPane scrollStudents;
    private javax.swing.JScrollPane scrollTodayQueue;
    private javax.swing.JTabbedPane tabbedMain;
    private javax.swing.JTable tableStudents;
    private javax.swing.JTable tableTodayQueue;
    // End of variables declaration//GEN-END:variables
}
