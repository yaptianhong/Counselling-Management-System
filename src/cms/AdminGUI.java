/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cms;

import java.awt.CardLayout;
import java.util.ArrayList;


/**
 *
 * @author yiche
 */
public class AdminGUI extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminGUI.class.getName());
    private Admin admin;
    private FileManager fileManager;

    /**
     * Creates new form AdminGUI
     */
    public AdminGUI() {
        this(new Admin("A001", "admin", "admin123", "Administrator"), new FileManager());
    }
    
    public AdminGUI(Admin admin, FileManager fileManager) {
        initComponents();
        this.admin = admin;
        this.fileManager = fileManager;
        startClock();
        loadImages();
        loadStaffTable();
        wireStaffButtons();
        loadScheduleTable();
        wireScheduleButtons();
        wireStatisticsButtons();
        wireReportsButtons();
        wireLogoutButton();
        setActiveSidebarButton(btnDashboard1);
    }
    
    //------------------------------------------------//
    //********************STAFF********************//
    //------------------------------------------------//
    private void loadStaffTable() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblStaff.getModel();
        model.setRowCount(0); // clear existing rows

        java.util.ArrayList<User> staffList = admin.getStaffList(fileManager);

        for (User staff : staffList) {
            model.addRow(new Object[]{
                staff.getUserID(),
                staff.getUsername(),
                staff.getName(),
                staff.getRole()
            });
        }

        lblStatus.setText("Status : " + staffList.size() + " staff member(s) loaded");
    }
    
    //Add staff Button
    private void showAddStaffDialog() {
        javax.swing.JTextField txtUsername = new javax.swing.JTextField();
        javax.swing.JPasswordField txtPassword = new javax.swing.JPasswordField();
        javax.swing.JTextField txtFullName = new javax.swing.JTextField();
        javax.swing.JComboBox<String> cmbRole = new javax.swing.JComboBox<>(new String[]{"Counselor", "Receptionist"});

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        panel.add(new javax.swing.JLabel("Username:"));
        panel.add(txtUsername);
        panel.add(new javax.swing.JLabel("Password:"));
        panel.add(txtPassword);
        panel.add(new javax.swing.JLabel("Full Name:"));
        panel.add(txtFullName);
        panel.add(new javax.swing.JLabel("Role:"));
        panel.add(cmbRole);

        int result = javax.swing.JOptionPane.showConfirmDialog(
                this, panel, "Add Staff",
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE
        );

        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return; // user clicked Cancel or closed the dialog
        }

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String fullName = txtFullName.getText().trim();
        String selectedRole = (String) cmbRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "All fields are required.",
                    "Validation Error",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        Role role = selectedRole.equals("Counselor") ? Role.COUNSELOR : Role.RECEPTIONIST;

        User newStaff = admin.createStaffAccount(username, password, fullName, role, fileManager);

        if (newStaff != null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Staff account created successfully!\nID: " + newStaff.getUserID(),
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadStaffTable();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to create staff account. Username may already exist.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //Edit staff
    private void showEditStaffDialog() {
        int selectedRow = tblStaff.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a staff member from the table first.",
                    "No Selection",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userID = tblStaff.getValueAt(selectedRow, 0).toString();
        String currentUsername = tblStaff.getValueAt(selectedRow, 1).toString();
        String currentName = tblStaff.getValueAt(selectedRow, 2).toString();

        javax.swing.JTextField txtUsername = new javax.swing.JTextField(currentUsername);
        javax.swing.JTextField txtFullName = new javax.swing.JTextField(currentName);
        javax.swing.JPasswordField txtPassword = new javax.swing.JPasswordField();

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        panel.add(new javax.swing.JLabel("User ID: " + userID));
        panel.add(new javax.swing.JLabel("Username:"));
        panel.add(txtUsername);
        panel.add(new javax.swing.JLabel("Full Name:"));
        panel.add(txtFullName);
        panel.add(new javax.swing.JLabel("New Password (leave blank to keep current):"));
        panel.add(txtPassword);

        int result = javax.swing.JOptionPane.showConfirmDialog(
                this, panel, "Edit Staff",
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE
        );

        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }

        String newUsername = txtUsername.getText().trim();
        String newName = txtFullName.getText().trim();
        String newPassword = new String(txtPassword.getPassword()).trim();

        if (newUsername.isEmpty() || newName.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Username and Name cannot be empty.",
                    "Validation Error",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean anyFailed = false;

        if (!newName.equals(currentName)) {
            if (!admin.updateStaffName(userID, newName, fileManager)) {
                anyFailed = true;
            }
        }

        if (!newUsername.equals(currentUsername)) {
            if (!admin.updateStaffUsername(userID, newUsername, fileManager)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Username update failed. It may already be taken.",
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                anyFailed = true;
            }
        }

        if (!newPassword.isEmpty()) {
            if (!admin.updateStaffPassword(userID, newPassword, fileManager)) {
                anyFailed = true;
            }
        }

        if (!anyFailed) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Staff account updated successfully!",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }

        loadStaffTable();
    }
    

    private void showDeleteStaffDialog() {
        int selectedRow = tblStaff.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a staff member from the table first.",
                    "No Selection",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userID = tblStaff.getValueAt(selectedRow, 0).toString();
        String username = tblStaff.getValueAt(selectedRow, 1).toString();
        String name = tblStaff.getValueAt(selectedRow, 2).toString();

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this staff account?\n\n"
                + "ID: " + userID + "\n"
                + "Username: " + username + "\n"
                + "Name: " + name + "\n\n"
                + "This action cannot be undone.",
                "Confirm Delete",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = admin.deleteStaffAccount(userID, fileManager);

        if (deleted) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Staff account deleted successfully.",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadStaffTable();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to delete staff account. User ID not found.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void wireStaffButtons() {
        btnRefreshStaff.addActionListener(evt -> loadStaffTable());
        btnAddStaff.addActionListener(evt -> showAddStaffDialog());
        btnEditStaff1.addActionListener(evt -> showEditStaffDialog());
        btnDeleteStaff.addActionListener(evt -> showDeleteStaffDialog());
    }
    
    
    
    //------------------------------------------------//
    //********************SCHEDULE********************//
    //------------------------------------------------//
    private void loadScheduleTable() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblSchedule.getModel();
        model.setRowCount(0);

        java.util.ArrayList<Schedule> schedules = fileManager.getAllSchedules();

        for (Schedule s : schedules) {
            model.addRow(new Object[]{
                s.getScheduleID(),
                s.getCounselorID(),
                s.getDay(),
                s.getStartTime(),
                s.getEndTime(),
                s.getStatus()
            });
        }

        lblStatus.setText("Status : " + schedules.size() + " schedule(s) loaded");
    }

    //Add Schedule
    private javax.swing.JComboBox<String> createHourCombo() {
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>();
        for (int h = 1; h <= 12; h++) {
            combo.addItem(String.valueOf(h));
        }
        return combo;
    }

    private javax.swing.JComboBox<String> createMinuteCombo() {
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>();
        for (int m = 0; m < 60; m+=15) {
            combo.addItem(String.format("%02d", m));
        }
        return combo;
    }

    private javax.swing.JComboBox<String> createAmPmCombo() {
        return new javax.swing.JComboBox<>(new String[]{"AM", "PM"});
    }

    // Builds a "Hour : Minute AM/PM" row of 3 combo boxes in one small panel
    private javax.swing.JPanel buildTimeComboPanel(javax.swing.JComboBox<String> hourCombo,
            javax.swing.JComboBox<String> minuteCombo, javax.swing.JComboBox<String> ampmCombo) {
        javax.swing.JPanel timePanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        timePanel.add(hourCombo);
        timePanel.add(new javax.swing.JLabel(":"));
        timePanel.add(minuteCombo);
        timePanel.add(ampmCombo);
        return timePanel;
    }

    // Parses an existing "9:00 AM" style string (as already saved on file) and
    // pre-selects the matching hour/minute/AM-PM combo values - used by the
    // Edit dialog so it opens showing the schedule's current time, not defaults.
    private void selectTimeCombo(String timeText, javax.swing.JComboBox<String> hourCombo,
            javax.swing.JComboBox<String> minuteCombo, javax.swing.JComboBox<String> ampmCombo) {
        try {
            java.time.LocalTime t = java.time.LocalTime.parse(timeText.trim().toUpperCase(),
                    java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            int hour12 = t.getHour() % 12;
            if (hour12 == 0) {
                hour12 = 12;
            }
            hourCombo.setSelectedItem(String.valueOf(hour12));
            minuteCombo.setSelectedItem(String.format("%02d", (t.getMinute() / 15) * 15));
            ampmCombo.setSelectedItem(t.getHour() < 12 ? "AM" : "PM");
        } catch (java.time.format.DateTimeParseException e) {
            // If the stored text doesn't parse (e.g. legacy free-typed data),
            // just leave the combos at their default first-item selection.
        }
    }

    private void showAddScheduleDialog() {
        javax.swing.JComboBox<String> cmbCounselor = buildCounselorComboBox();

        if (cmbCounselor.getItemCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No counselors found. Please add a Counselor staff account first.",
                    "No Counselors Available",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JComboBox<String> cmbDay = new javax.swing.JComboBox<>(new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        });

        javax.swing.JComboBox<String> cmbStartHour = createHourCombo();
        javax.swing.JComboBox<String> cmbStartMinute = createMinuteCombo();
        javax.swing.JComboBox<String> cmbStartAmPm = createAmPmCombo();

        javax.swing.JComboBox<String> cmbEndHour = createHourCombo();
        javax.swing.JComboBox<String> cmbEndMinute = createMinuteCombo();
        javax.swing.JComboBox<String> cmbEndAmPm = createAmPmCombo();

        javax.swing.JComboBox<String> cmbStatus = new javax.swing.JComboBox<>(new String[]{
            "Available", "Booked", "Unavailable"
        });

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        panel.add(new javax.swing.JLabel("Counselor:"));
        panel.add(cmbCounselor);
        panel.add(new javax.swing.JLabel("Day:"));
        panel.add(cmbDay);
        panel.add(new javax.swing.JLabel("Start Time:"));
        panel.add(buildTimeComboPanel(cmbStartHour, cmbStartMinute, cmbStartAmPm));
        panel.add(new javax.swing.JLabel("End Time:"));
        panel.add(buildTimeComboPanel(cmbEndHour, cmbEndMinute, cmbEndAmPm));
        panel.add(new javax.swing.JLabel("Status:"));
        panel.add(cmbStatus);

        int result = javax.swing.JOptionPane.showConfirmDialog(
                this, panel, "Add Schedule",
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE
        );

        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }

        String counselorID = extractCounselorID((String) cmbCounselor.getSelectedItem());
        String day = (String) cmbDay.getSelectedItem();
        String startTime = cmbStartHour.getSelectedItem() + ":" + cmbStartMinute.getSelectedItem()
                + " " + cmbStartAmPm.getSelectedItem();
        String endTime = cmbEndHour.getSelectedItem() + ":" + cmbEndMinute.getSelectedItem()
                + " " + cmbEndAmPm.getSelectedItem();
        String status = (String) cmbStatus.getSelectedItem();

        Schedule newSchedule = admin.createSchedule(counselorID, day, startTime, endTime, status, fileManager);

        if (newSchedule != null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Schedule created successfully!",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadScheduleTable();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to create schedule.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    //Edit Schedule
    private void showEditScheduleDialog() {
        int selectedRow = tblSchedule.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a schedule from the table first.",
                    "No Selection",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String scheduleID = tblSchedule.getValueAt(selectedRow, 0).toString();
        String currentDay = tblSchedule.getValueAt(selectedRow, 2).toString();
        String currentStart = tblSchedule.getValueAt(selectedRow, 3).toString();
        String currentEnd = tblSchedule.getValueAt(selectedRow, 4).toString();
        String currentStatus = tblSchedule.getValueAt(selectedRow, 5).toString();

        javax.swing.JComboBox<String> cmbDay = new javax.swing.JComboBox<>(new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
        });
        cmbDay.setSelectedItem(currentDay);

        javax.swing.JComboBox<String> cmbStartHour = createHourCombo();
        javax.swing.JComboBox<String> cmbStartMinute = createMinuteCombo();
        javax.swing.JComboBox<String> cmbStartAmPm = createAmPmCombo();
        selectTimeCombo(currentStart, cmbStartHour, cmbStartMinute, cmbStartAmPm);

        javax.swing.JComboBox<String> cmbEndHour = createHourCombo();
        javax.swing.JComboBox<String> cmbEndMinute = createMinuteCombo();
        javax.swing.JComboBox<String> cmbEndAmPm = createAmPmCombo();
        selectTimeCombo(currentEnd, cmbEndHour, cmbEndMinute, cmbEndAmPm);

        javax.swing.JComboBox<String> cmbStatus = new javax.swing.JComboBox<>(new String[]{
            "Available", "Booked", "Unavailable"
        });
        cmbStatus.setSelectedItem(currentStatus);

        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        panel.add(new javax.swing.JLabel("Schedule ID: " + scheduleID));
        panel.add(new javax.swing.JLabel("Day:"));
        panel.add(cmbDay);
        panel.add(new javax.swing.JLabel("Start Time:"));
        panel.add(buildTimeComboPanel(cmbStartHour, cmbStartMinute, cmbStartAmPm));
        panel.add(new javax.swing.JLabel("End Time:"));
        panel.add(buildTimeComboPanel(cmbEndHour, cmbEndMinute, cmbEndAmPm));
        panel.add(new javax.swing.JLabel("Status:"));
        panel.add(cmbStatus);

        int result = javax.swing.JOptionPane.showConfirmDialog(
                this, panel, "Edit Schedule",
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.PLAIN_MESSAGE
        );

        if (result != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }

        String newDay = (String) cmbDay.getSelectedItem();
        String newStart = cmbStartHour.getSelectedItem() + ":" + cmbStartMinute.getSelectedItem()
                + " " + cmbStartAmPm.getSelectedItem();
        String newEnd = cmbEndHour.getSelectedItem() + ":" + cmbEndMinute.getSelectedItem()
                + " " + cmbEndAmPm.getSelectedItem();
        String newStatus = (String) cmbStatus.getSelectedItem();

        boolean updated = admin.updateSchedule(scheduleID, newDay, newStart, newEnd, newStatus, fileManager);

        if (updated) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Schedule updated successfully!",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadScheduleTable();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to update schedule. Schedule ID not found.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    //Delete Schedule
    private void showDeleteScheduleDialog() {
        int selectedRow = tblSchedule.getSelectedRow();

        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please select a schedule from the table first.",
                    "No Selection",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String scheduleID = tblSchedule.getValueAt(selectedRow, 0).toString();

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete schedule " + scheduleID + "?\nThis action cannot be undone.",
                "Confirm Delete",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = admin.deleteSchedule(scheduleID, fileManager);

        if (deleted) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Schedule deleted successfully.",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadScheduleTable();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Failed to delete schedule. Schedule ID not found.",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void wireScheduleButtons() {
        btnRefreshSchedule.addActionListener(evt -> loadScheduleTable());
        btnAddSchedule.addActionListener(evt -> showAddScheduleDialog());
        btnEditSchedule.addActionListener(evt -> showEditScheduleDialog());
        btnDeleteSchedule.addActionListener(evt -> showDeleteScheduleDialog());
    }
    
    //------------------------------------------------//
    //********************STATISTIC********************//
    //------------------------------------------------//
    private void loadStatistics() {
        int[] stats = admin.getAppointmentStatistics(fileManager);

        lblTotalCount.setText(String.valueOf(stats[0]));
        lblPendingCount.setText(String.valueOf(stats[1]));
        lblConfirmedCount.setText(String.valueOf(stats[2]));
        lblCompletedCount.setText(String.valueOf(stats[3]));
        lblCancelledCount.setText(String.valueOf(stats[4]));

        ArrayList<Object[]> workload = admin.getCounselorWorkload(fileManager);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tblWorkload.getModel();
        model.setRowCount(0);

        for (Object[] entry : workload) {
            model.addRow(entry);
        }

        lblStatus.setText("Status : Statistics refreshed");
    }
    
    private void wireStatisticsButtons() {
        btnRefreshStatistics.addActionListener(evt -> loadStatistics());
    }
    
    //------------------------------------------------//
    //********************REPORT********************//
    //------------------------------------------------//
    private void generateReportAction() {
        String reportType = (String) cmbReportType.getSelectedItem();
        String dateFrom = txtDateFrom.getText().trim();
        String dateTo = txtDateTo.getText().trim();

        String reportText;

        if (!dateFrom.isEmpty() && !dateTo.isEmpty()) {
            // Custom date range typed in by the admin overrides the auto period
            if (!isValidReportDate(dateFrom) || !isValidReportDate(dateTo)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Please enter valid dates in yyyy-MM-dd format (e.g. 2026-08-04),\n"
                        + "or leave both fields blank to use the selected Report Type period.",
                        "Invalid Date",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            reportText = admin.getReportText(reportType, dateFrom, dateTo, fileManager);
        } else {
            // No custom range entered -> auto-calculate the period from the
            // selected Report Type (Daily / Monthly / Quarterly / Yearly)
            reportText = admin.getReportText(reportType, fileManager);
        }

        txtReportOutput.setText(reportText);
        lblStatus.setText("Status : " + reportType + " report generated");
    }

// Validates that a date string is in the "yyyy-MM-dd" format used by
// appointments.txt, e.g. "2026-08-04".
    private boolean isValidReportDate(String dateText) {
        try {
            java.time.LocalDate.parse(dateText, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }
    
    private void saveReportAction() {
        String reportText = txtReportOutput.getText();

        if (reportText.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please generate a report first.",
                    "No Report",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("report.txt"));
        int userChoice = fileChooser.showSaveDialog(this);

        if (userChoice == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.print(reportText);
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Report saved successfully to:\n" + file.getAbsolutePath(),
                        "Saved",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Failed to save report: " + e.getMessage(),
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void wireReportsButtons() {
        btnGenerateReport.addActionListener(evt -> generateReportAction());
        btnSaveReport.addActionListener(evt -> saveReportAction());
    }
    
    
    //------------------------------------------------//
    //********************IMAGE RESIZE********************//
    //------------------------------------------------//
    private void loadImages() {
        setScaledIcon(lblLogoIcon, "/cms/CMS_Icon.png", lblLogoIcon.getWidth(), lblLogoIcon.getHeight());
        setScaledIcon(lblUserIcon, "/cms/userIcon.png", lblUserIcon.getWidth(), lblUserIcon.getHeight());
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
    
    //------------------------------------------------//
    //********************BUTTON Color********************//
    //------------------------------------------------//
    private void setActiveSidebarButton(javax.swing.JButton activeButton) {
        javax.swing.JButton[] allButtons = {
            btnDashboard1, btnStaff1, btnSchedule1, btnStatistics1, btnReports1
        };

        java.awt.Color inactiveColor = new java.awt.Color(30, 41, 59); 
        java.awt.Color activeColor = new java.awt.Color(70, 110, 255);   

        for (javax.swing.JButton btn : allButtons) {
            btn.setBackground(inactiveColor);
        }

        activeButton.setBackground(activeColor);
    }
    
    
    
    //------------------------------------------------//
    //********************JComboBox(Dropdown selection)********************//
    //------------------------------------------------//
    private javax.swing.JComboBox<String> buildCounselorComboBox() {
        java.util.ArrayList<User> staffList = admin.getStaffList(fileManager);
        java.util.ArrayList<String> counselorEntries = new java.util.ArrayList<>();

        for (User u : staffList) {
            if (u.getRole() == Role.COUNSELOR) {
                counselorEntries.add(u.getUserID() + " - " + u.getName());
            }
        }

        return new javax.swing.JComboBox<>(counselorEntries.toArray(new String[0]));
    }

    // Extracts just the ID portion from a "ID - Name" combo box entry
    private String extractCounselorID(String comboEntry) {
        if (comboEntry == null) {
            return "";
        }
        return comboEntry.split(" - ")[0].trim();
    }
    
    //------------------------------------------------//
    //********************RealtimeClock********************//
    //------------------------------------------------//
    private void startClock() {
        // Set initial values immediately (so it doesn't show blank for the first second)
        lblDate.setText(RealDateTime.getFormattedDate());
        lblTime.setText(RealDateTime.getFormattedTime());

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, evt -> {
            lblDate.setText(RealDateTime.getFormattedDate());
            lblTime.setText(RealDateTime.getFormattedTime());
        });
        clockTimer.start();
    }
    
    //------------------------------------------------//
    //********************LOGOUT********************//
    //------------------------------------------------//
    private void logoutAction() {
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
    }
    
    private void wireLogoutButton() {
        btnLogout.addActionListener(evt -> logoutAction());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelStatus = new javax.swing.JPanel();
        lblStatus = new javax.swing.JLabel();
        panelClock = new javax.swing.JPanel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        panelSidebar = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblLogoIcon = new javax.swing.JLabel();
        lblUserIcon = new javax.swing.JLabel();
        lblAdmin = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        btnDashboard1 = new javax.swing.JButton();
        btnStaff1 = new javax.swing.JButton();
        btnSchedule1 = new javax.swing.JButton();
        btnStatistics1 = new javax.swing.JButton();
        btnReports1 = new javax.swing.JButton();
        panelMain = new javax.swing.JPanel();
        cardDashboard = new javax.swing.JPanel();
        panelModules = new javax.swing.JPanel();
        lblModules = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        panelOverview = new javax.swing.JPanel();
        lblOverview = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtOverview = new javax.swing.JTextArea();
        lblSubtitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        cardStaff = new javax.swing.JPanel();
        lblStaff = new javax.swing.JLabel();
        btnRefreshStaff = new javax.swing.JButton();
        btnAddStaff = new javax.swing.JButton();
        btnEditStaff1 = new javax.swing.JButton();
        btnDeleteStaff = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblStaff = new javax.swing.JTable();
        cardSchedule = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnRefreshSchedule = new javax.swing.JButton();
        btnAddSchedule = new javax.swing.JButton();
        btnEditSchedule = new javax.swing.JButton();
        btnDeleteSchedule = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblSchedule = new javax.swing.JTable();
        cardStatistics = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        pnlStatTotal = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        lblTotalCount = new javax.swing.JLabel();
        pnlStatPending = new javax.swing.JPanel();
        lblPending = new javax.swing.JLabel();
        lblPendingCount = new javax.swing.JLabel();
        pnlStatConfirmed = new javax.swing.JPanel();
        lblConfirmed = new javax.swing.JLabel();
        lblConfirmedCount = new javax.swing.JLabel();
        pnlStatCompleted = new javax.swing.JPanel();
        lblCompleted = new javax.swing.JLabel();
        lblCompletedCount = new javax.swing.JLabel();
        pnlStatCancelled = new javax.swing.JPanel();
        lblCancelled = new javax.swing.JLabel();
        lblCancelledCount = new javax.swing.JLabel();
        lblCounselorWorkload = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblWorkload = new javax.swing.JTable();
        btnRefreshStatistics = new javax.swing.JButton();
        cardReports = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblReportType = new javax.swing.JLabel();
        cmbReportType = new javax.swing.JComboBox<>();
        txtDateFrom = new javax.swing.JTextField();
        txtDateTo = new javax.swing.JTextField();
        btnGenerateReport = new javax.swing.JButton();
        btnSaveReport = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtReportOutput = new javax.swing.JTextArea();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("CMS - Admin Menu");
        setResizable(false);
        setSize(new java.awt.Dimension(1000, 650));

        panelStatus.setBackground(new java.awt.Color(245, 245, 245));
        panelStatus.setPreferredSize(new java.awt.Dimension(1000, 28));
        panelStatus.setLayout(new java.awt.BorderLayout());

        lblStatus.setForeground(new java.awt.Color(0, 0, 0));
        lblStatus.setText("Status : Ready");
        lblStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panelStatus.add(lblStatus, java.awt.BorderLayout.LINE_START);

        panelClock.setBackground(new java.awt.Color(245, 245, 245));
        panelClock.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        lblDate.setForeground(new java.awt.Color(0, 0, 0));
        lblDate.setText("Date : 30/07/2026");
        panelClock.add(lblDate);

        lblTime.setForeground(new java.awt.Color(0, 0, 0));
        lblTime.setText("Time : 10 : 30 PM");
        panelClock.add(lblTime);

        panelStatus.add(panelClock, java.awt.BorderLayout.LINE_END);

        getContentPane().add(panelStatus, java.awt.BorderLayout.PAGE_START);

        panelSidebar.setBackground(new java.awt.Color(20, 32, 61));
        panelSidebar.setPreferredSize(new java.awt.Dimension(200, 650));
        panelSidebar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblLogo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblLogo.setForeground(new java.awt.Color(255, 255, 255));
        lblLogo.setText("CMS");
        lblLogo.setAlignmentX(25.0F);
        lblLogo.setAlignmentY(20.0F);
        lblLogo.setPreferredSize(new java.awt.Dimension(120, 35));
        panelSidebar.add(lblLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, -1, -1));

        lblLogoIcon.setPreferredSize(new java.awt.Dimension(32, 32));
        panelSidebar.add(lblLogoIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 50, 50));

        lblUserIcon.setPreferredSize(new java.awt.Dimension(48, 48));
        panelSidebar.add(lblUserIcon, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 90, 30, 30));

        lblAdmin.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblAdmin.setForeground(new java.awt.Color(255, 255, 255));
        lblAdmin.setText("Admin");
        panelSidebar.add(lblAdmin, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 90, -1, -1));

        btnLogout.setBackground(new java.awt.Color(55, 100, 240));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("Logout");
        btnLogout.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(this::btnLogoutActionPerformed);
        panelSidebar.add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 540, 170, 40));

        btnDashboard1.setBackground(new java.awt.Color(70, 110, 255));
        btnDashboard1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDashboard1.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard1.setText("Dashboard");
        btnDashboard1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnDashboard1.setBorderPainted(false);
        btnDashboard1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDashboard1.setFocusPainted(false);
        btnDashboard1.addActionListener(this::btnDashboard1ActionPerformed);
        panelSidebar.add(btnDashboard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 150, 170, 40));

        btnStaff1.setBackground(new java.awt.Color(30, 41, 59));
        btnStaff1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnStaff1.setForeground(new java.awt.Color(255, 255, 255));
        btnStaff1.setText("Staff");
        btnStaff1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnStaff1.setBorderPainted(false);
        btnStaff1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnStaff1.setFocusPainted(false);
        btnStaff1.addActionListener(this::btnStaff1ActionPerformed);
        panelSidebar.add(btnStaff1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 195, 170, 40));

        btnSchedule1.setBackground(new java.awt.Color(30, 41, 59));
        btnSchedule1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSchedule1.setForeground(new java.awt.Color(255, 255, 255));
        btnSchedule1.setText("Schedule");
        btnSchedule1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnSchedule1.setBorderPainted(false);
        btnSchedule1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSchedule1.setFocusPainted(false);
        btnSchedule1.addActionListener(this::btnSchedule1ActionPerformed);
        panelSidebar.add(btnSchedule1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 240, 170, 40));

        btnStatistics1.setBackground(new java.awt.Color(30, 41, 59));
        btnStatistics1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnStatistics1.setForeground(new java.awt.Color(255, 255, 255));
        btnStatistics1.setText("Statistics");
        btnStatistics1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnStatistics1.setBorderPainted(false);
        btnStatistics1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnStatistics1.setFocusPainted(false);
        btnStatistics1.addActionListener(this::btnStatistics1ActionPerformed);
        panelSidebar.add(btnStatistics1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 285, 170, 40));

        btnReports1.setBackground(new java.awt.Color(30, 41, 59));
        btnReports1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnReports1.setForeground(new java.awt.Color(255, 255, 255));
        btnReports1.setText("Reports");
        btnReports1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnReports1.setBorderPainted(false);
        btnReports1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReports1.setFocusPainted(false);
        btnReports1.addActionListener(this::btnReports1ActionPerformed);
        panelSidebar.add(btnReports1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 330, 170, 40));

        getContentPane().add(panelSidebar, java.awt.BorderLayout.LINE_START);

        panelMain.setBackground(new java.awt.Color(249, 250, 251));
        panelMain.setToolTipText("OVERVIEW");
        panelMain.setPreferredSize(new java.awt.Dimension(800, 622));
        panelMain.setLayout(new java.awt.CardLayout());

        cardDashboard.setBackground(new java.awt.Color(255, 255, 255));
        cardDashboard.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        cardDashboard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelModules.setBackground(new java.awt.Color(255, 255, 255));
        panelModules.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235)), javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        panelModules.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblModules.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblModules.setForeground(new java.awt.Color(79, 70, 229));
        lblModules.setText("SYSTEM MODULES");
        panelModules.add(lblModules, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("Manage Staff Accounts (CRUD)");
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        panelModules.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 60, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 102, 102));
        jLabel4.setText("Manage Staff Roster & Schedules");
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });
        panelModules.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 100, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(102, 102, 102));
        jLabel5.setText("Monitor Appointment Statistics");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });
        panelModules.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 140, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(102, 102, 102));
        jLabel6.setText("Generate Reports");
        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });
        panelModules.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 180, -1, -1));

        cardDashboard.add(panelModules, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 740, 220));

        panelOverview.setBackground(new java.awt.Color(255, 255, 255));
        panelOverview.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235)), javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        panelOverview.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblOverview.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblOverview.setForeground(new java.awt.Color(79, 70, 229));
        lblOverview.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblOverview.setText("OVERVIEW");
        lblOverview.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        panelOverview.add(lblOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 20, -1, -1));

        txtOverview.setEditable(false);
        txtOverview.setBackground(new java.awt.Color(255, 255, 255));
        txtOverview.setColumns(20);
        txtOverview.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        txtOverview.setForeground(new java.awt.Color(0, 0, 0));
        txtOverview.setLineWrap(true);
        txtOverview.setRows(5);
        txtOverview.setText("This system allows administrators to manage staff accounts, schedules, appointment statistics and reports.\n\nSelect a module from the navigation menu to begin.");
        txtOverview.setWrapStyleWord(true);
        txtOverview.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jScrollPane2.setViewportView(txtOverview);

        panelOverview.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 690, 110));

        cardDashboard.add(panelOverview, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 330, 740, 190));

        lblSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitle.setForeground(new java.awt.Color(107, 114, 128));
        lblSubtitle.setText("Use the menu on the left to manage the system.");
        cardDashboard.add(lblSubtitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 60, -1, -1));

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setForeground(new java.awt.Color(50, 70, 230));
        lblWelcome.setText("WELCOME,  ADMIN!");
        cardDashboard.add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 20, -1, -1));

        panelMain.add(cardDashboard, "dashboard");

        cardStaff.setBackground(new java.awt.Color(204, 204, 204));
        cardStaff.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        cardStaff.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStaff.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblStaff.setForeground(new java.awt.Color(79, 70, 229));
        lblStaff.setText("Staff Management");
        cardStaff.add(lblStaff, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        btnRefreshStaff.setBackground(new java.awt.Color(107, 114, 128));
        btnRefreshStaff.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefreshStaff.setText("Refresh");
        btnRefreshStaff.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnRefreshStaff.setBorderPainted(false);
        btnRefreshStaff.setFocusPainted(false);
        cardStaff.add(btnRefreshStaff, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 70, -1, -1));

        btnAddStaff.setBackground(new java.awt.Color(34, 197, 94));
        btnAddStaff.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAddStaff.setText("Add Staff");
        btnAddStaff.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnAddStaff.setBorderPainted(false);
        btnAddStaff.setFocusPainted(false);
        btnAddStaff.addActionListener(this::btnAddStaffActionPerformed);
        cardStaff.add(btnAddStaff, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        btnEditStaff1.setBackground(new java.awt.Color(79, 70, 229));
        btnEditStaff1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditStaff1.setText("Edit Staff");
        btnEditStaff1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnEditStaff1.setBorderPainted(false);
        btnEditStaff1.setFocusPainted(false);
        cardStaff.add(btnEditStaff1, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, -1, -1));

        btnDeleteStaff.setBackground(new java.awt.Color(239, 68, 68));
        btnDeleteStaff.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDeleteStaff.setText("Delete Staff");
        btnDeleteStaff.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnDeleteStaff.setBorderPainted(false);
        btnDeleteStaff.setFocusPainted(false);
        cardStaff.add(btnDeleteStaff, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 70, -1, -1));

        tblStaff.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User ID", "Username", "Name", "Role"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblStaff.setMinimumSize(new java.awt.Dimension(900, 400));
        tblStaff.setPreferredSize(new java.awt.Dimension(900, 400));
        jScrollPane3.setViewportView(tblStaff);

        cardStaff.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 710, -1));

        panelMain.add(cardStaff, "staff");

        cardSchedule.setBackground(new java.awt.Color(204, 204, 204));
        cardSchedule.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        cardSchedule.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("Schedule Management");
        cardSchedule.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        btnRefreshSchedule.setBackground(new java.awt.Color(107, 114, 128));
        btnRefreshSchedule.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefreshSchedule.setText("Refresh");
        btnRefreshSchedule.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnRefreshSchedule.setBorderPainted(false);
        btnRefreshSchedule.setFocusPainted(false);
        cardSchedule.add(btnRefreshSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 70, -1, -1));

        btnAddSchedule.setBackground(new java.awt.Color(34, 197, 94));
        btnAddSchedule.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAddSchedule.setText("Add Schedule");
        btnAddSchedule.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnAddSchedule.setBorderPainted(false);
        btnAddSchedule.setFocusPainted(false);
        btnAddSchedule.addActionListener(this::btnAddScheduleActionPerformed);
        cardSchedule.add(btnAddSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        btnEditSchedule.setBackground(new java.awt.Color(79, 70, 229));
        btnEditSchedule.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditSchedule.setText("Edit Schedule");
        btnEditSchedule.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnEditSchedule.setBorderPainted(false);
        btnEditSchedule.setFocusPainted(false);
        cardSchedule.add(btnEditSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, -1, -1));

        btnDeleteSchedule.setBackground(new java.awt.Color(239, 68, 68));
        btnDeleteSchedule.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDeleteSchedule.setText("Delete Schedule");
        btnDeleteSchedule.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btnDeleteSchedule.setBorderPainted(false);
        btnDeleteSchedule.setFocusPainted(false);
        cardSchedule.add(btnDeleteSchedule, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 70, -1, -1));

        tblSchedule.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Schedule ID", "Counselor ID", "Day", "Start Time", "End Time", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSchedule.setMinimumSize(new java.awt.Dimension(900, 400));
        tblSchedule.setPreferredSize(new java.awt.Dimension(900, 400));
        jScrollPane4.setViewportView(tblSchedule);

        cardSchedule.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 710, -1));

        panelMain.add(cardSchedule, "schedule");

        cardStatistics.setBackground(new java.awt.Color(204, 204, 204));
        cardStatistics.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        cardStatistics.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 51, 255));
        jLabel2.setText("Appointment Statistics");
        cardStatistics.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        pnlStatTotal.setBackground(new java.awt.Color(255, 255, 255));
        pnlStatTotal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        lblTotal.setForeground(new java.awt.Color(0, 51, 255));
        lblTotal.setText("Total");
        pnlStatTotal.add(lblTotal);

        lblTotalCount.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblTotalCount.setForeground(new java.awt.Color(0, 0, 0));
        lblTotalCount.setText("0");
        pnlStatTotal.add(lblTotalCount);

        cardStatistics.add(pnlStatTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 70, 130, 90));

        pnlStatPending.setBackground(new java.awt.Color(255, 255, 255));
        pnlStatPending.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        lblPending.setForeground(new java.awt.Color(255, 153, 0));
        lblPending.setText("Pending");
        pnlStatPending.add(lblPending);

        lblPendingCount.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblPendingCount.setForeground(new java.awt.Color(0, 0, 0));
        lblPendingCount.setText("0");
        pnlStatPending.add(lblPendingCount);

        cardStatistics.add(pnlStatPending, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 70, 130, 90));

        pnlStatConfirmed.setBackground(new java.awt.Color(255, 255, 255));
        pnlStatConfirmed.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        lblConfirmed.setForeground(new java.awt.Color(51, 204, 255));
        lblConfirmed.setText("Confirmed");
        pnlStatConfirmed.add(lblConfirmed);

        lblConfirmedCount.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblConfirmedCount.setForeground(new java.awt.Color(0, 0, 0));
        lblConfirmedCount.setText("0");
        pnlStatConfirmed.add(lblConfirmedCount);

        cardStatistics.add(pnlStatConfirmed, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 70, 130, 90));

        pnlStatCompleted.setBackground(new java.awt.Color(255, 255, 255));
        pnlStatCompleted.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        lblCompleted.setForeground(new java.awt.Color(51, 204, 0));
        lblCompleted.setText("Completed");
        pnlStatCompleted.add(lblCompleted);

        lblCompletedCount.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblCompletedCount.setForeground(new java.awt.Color(0, 0, 0));
        lblCompletedCount.setText("0");
        pnlStatCompleted.add(lblCompletedCount);

        cardStatistics.add(pnlStatCompleted, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 70, 130, 90));

        pnlStatCancelled.setBackground(new java.awt.Color(255, 255, 255));
        pnlStatCancelled.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        lblCancelled.setForeground(new java.awt.Color(255, 51, 51));
        lblCancelled.setText("Cancelled");
        pnlStatCancelled.add(lblCancelled);

        lblCancelledCount.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        lblCancelledCount.setForeground(new java.awt.Color(0, 0, 0));
        lblCancelledCount.setText("0");
        pnlStatCancelled.add(lblCancelledCount);

        cardStatistics.add(pnlStatCancelled, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 70, 130, 90));

        lblCounselorWorkload.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblCounselorWorkload.setText("Counselor Workload");
        cardStatistics.add(lblCounselorWorkload, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, -1, -1));

        tblWorkload.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Counselor ID", "Name", "Pending", "Confirmed", "Completed", "Cancelled", "Total"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblWorkload.setMaximumSize(new java.awt.Dimension(880, 300));
        tblWorkload.setMinimumSize(new java.awt.Dimension(800, 290));
        tblWorkload.setPreferredSize(new java.awt.Dimension(880, 300));
        jScrollPane5.setViewportView(tblWorkload);

        cardStatistics.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 760, 370));

        btnRefreshStatistics.setText("Refresh");
        cardStatistics.add(btnRefreshStatistics, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 20, -1, -1));

        panelMain.add(cardStatistics, "statistics");

        cardReports.setBackground(new java.awt.Color(204, 204, 204));
        cardReports.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)), javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        cardReports.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(0, 51, 255));
        lblTitle.setText("Reports");
        cardReports.add(lblTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        lblReportType.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        lblReportType.setForeground(new java.awt.Color(0, 0, 0));
        lblReportType.setText("Report Type: ");
        cardReports.add(lblReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        cmbReportType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Daily", "Monthly", "Quarterly", "Yearly" }));
        cardReports.add(cmbReportType, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 80, -1, -1));
        cardReports.add(txtDateFrom, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 120, 80, -1));
        cardReports.add(txtDateTo, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 120, 80, -1));

        btnGenerateReport.setText("Generate Report");
        cardReports.add(btnGenerateReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 80, -1, -1));

        btnSaveReport.setText("Save To File");
        btnSaveReport.addActionListener(this::btnSaveReportActionPerformed);
        cardReports.add(btnSaveReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 120, 110, -1));

        txtReportOutput.setEditable(false);
        txtReportOutput.setColumns(20);
        txtReportOutput.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
        txtReportOutput.setRows(5);
        txtReportOutput.setPreferredSize(new java.awt.Dimension(880, 400));
        jScrollPane6.setViewportView(txtReportOutput);

        cardReports.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 730, 360));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("From: ");
        cardReports.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 120, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("To:");
        cardReports.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 120, -1, -1));

        panelMain.add(cardReports, "reports");

        getContentPane().add(panelMain, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnDashboard1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboard1ActionPerformed
        // TODO add your handling code here
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "dashboard");
        setActiveSidebarButton(btnDashboard1);
    }//GEN-LAST:event_btnDashboard1ActionPerformed

    private void btnStaff1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStaff1ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "staff");
        setActiveSidebarButton(btnStaff1);
    }//GEN-LAST:event_btnStaff1ActionPerformed

    private void btnAddStaffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddStaffActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddStaffActionPerformed

    private void btnAddScheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddScheduleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddScheduleActionPerformed

    private void btnSchedule1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSchedule1ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "schedule");
        setActiveSidebarButton(btnSchedule1);
    }//GEN-LAST:event_btnSchedule1ActionPerformed

    private void btnStatistics1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatistics1ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "statistics");
        setActiveSidebarButton(btnStatistics1);
        loadStatistics();
    }//GEN-LAST:event_btnStatistics1ActionPerformed

    private void btnReports1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReports1ActionPerformed
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "reports");
        setActiveSidebarButton(btnReports1);
    }//GEN-LAST:event_btnReports1ActionPerformed

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "staff");
        setActiveSidebarButton(btnStaff1);
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "schedule");
        setActiveSidebarButton(btnSchedule1);
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "statistics");
        setActiveSidebarButton(btnStatistics1);
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        // TODO add your handling code here:
        CardLayout cl = (CardLayout) panelMain.getLayout();
        cl.show(panelMain, "reports");
        setActiveSidebarButton(btnReports1);
    }//GEN-LAST:event_jLabel6MouseClicked

    private void btnSaveReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveReportActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSaveReportActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
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
        java.awt.EventQueue.invokeLater(() -> new AdminGUI().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddSchedule;
    private javax.swing.JButton btnAddStaff;
    private javax.swing.JButton btnDashboard1;
    private javax.swing.JButton btnDeleteSchedule;
    private javax.swing.JButton btnDeleteStaff;
    private javax.swing.JButton btnEditSchedule;
    private javax.swing.JButton btnEditStaff1;
    private javax.swing.JButton btnGenerateReport;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnRefreshSchedule;
    private javax.swing.JButton btnRefreshStaff;
    private javax.swing.JButton btnRefreshStatistics;
    private javax.swing.JButton btnReports1;
    private javax.swing.JButton btnSaveReport;
    private javax.swing.JButton btnSchedule1;
    private javax.swing.JButton btnStaff1;
    private javax.swing.JButton btnStatistics1;
    private javax.swing.JPanel cardDashboard;
    private javax.swing.JPanel cardReports;
    private javax.swing.JPanel cardSchedule;
    private javax.swing.JPanel cardStaff;
    private javax.swing.JPanel cardStatistics;
    private javax.swing.JComboBox<String> cmbReportType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JLabel lblAdmin;
    private javax.swing.JLabel lblCancelled;
    private javax.swing.JLabel lblCancelledCount;
    private javax.swing.JLabel lblCompleted;
    private javax.swing.JLabel lblCompletedCount;
    private javax.swing.JLabel lblConfirmed;
    private javax.swing.JLabel lblConfirmedCount;
    private javax.swing.JLabel lblCounselorWorkload;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblLogoIcon;
    private javax.swing.JLabel lblModules;
    private javax.swing.JLabel lblOverview;
    private javax.swing.JLabel lblPending;
    private javax.swing.JLabel lblPendingCount;
    private javax.swing.JLabel lblReportType;
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalCount;
    private javax.swing.JLabel lblUserIcon;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel panelClock;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelModules;
    private javax.swing.JPanel panelOverview;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JPanel pnlStatCancelled;
    private javax.swing.JPanel pnlStatCompleted;
    private javax.swing.JPanel pnlStatConfirmed;
    private javax.swing.JPanel pnlStatPending;
    private javax.swing.JPanel pnlStatTotal;
    private javax.swing.JTable tblSchedule;
    private javax.swing.JTable tblStaff;
    private javax.swing.JTable tblWorkload;
    private javax.swing.JTextField txtDateFrom;
    private javax.swing.JTextField txtDateTo;
    private javax.swing.JTextArea txtOverview;
    private javax.swing.JTextArea txtReportOutput;
    // End of variables declaration//GEN-END:variables
}
