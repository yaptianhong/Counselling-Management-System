package cms;

import java.util.ArrayList;
import java.util.Locale;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class Admin extends User {

    public Admin(String userID, String username, String password, String name) {
        super(userID, username, password, name, Role.ADMIN);
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }


//     Create a new staff account (Counselor or Receptionist).
//     The userID is auto-generated (not typed in) using FileManager.generateId(),
//     with a prefix based on role: "C" for Counselor, "R" for Receptionist.
//     @return the newly created User if successful, or null if it failed (message printed either way)

    public User createStaffAccount(String username, String password, String name, Role role, FileManager fileManager) {
        if (role != Role.COUNSELOR && role != Role.RECEPTIONIST) {
            System.out.println("Admin can only create Counselor or Receptionist accounts.");
            return null;
        }
 
        //variable = (condition) ? valueIfTrue : valueIfFalse;
        String prefix = (role == Role.COUNSELOR) ? "C" : "R";
        String newID = fileManager.generateId(FileManager.USERS_FILE, prefix);

        
        // User newStaff;
        // if (role == Role.COUNSELOR) {
        //    newStaff = new Counselor(newID, username, password, name);
        // } else {
        //    newStaff = new Receptionist(newID, username, password, name);
        // }
        // only works when both Counselor and Receptionist extends form User
        User newStaff = (role == Role.COUNSELOR)
                ? new Counselor(newID, username, password, name)
                : new Receptionist(newID, username, password, name);

        String error = fileManager.registerUser(newStaff);
        if (error == null) {
            System.out.println("Staff account created successfully: " + newStaff);
            return newStaff;
        } else {
            System.out.println("Failed to create account: " + error);
            return null;
        }
    }
    
//    READ operation for Staff Accounts. Displays all Counselor and
//    Receptionist accounts stored in users.txt.

    public void viewStaffAccounts(FileManager fileManager) {

        // Load every user from the text file
        ArrayList<User> allUsers = fileManager.getAllUsers();

        System.out.println();
        System.out.println("==============================================");
        System.out.println("          STAFF ACCOUNT LIST");
        System.out.println("==============================================");

        int totalStaff = 0;

        // Loop through every user
        for (User user : allUsers) {

            // Only display Counselor and Receptionist
            if (user.getRole() == Role.COUNSELOR
                    || user.getRole() == Role.RECEPTIONIST) {

                System.out.println("User ID  : " + user.getUserID());
                System.out.println("Name     : " + user.getName());
                System.out.println("Username : " + user.getUsername());
                System.out.println("Role     : " + user.getRole());
                System.out.println("----------------------------------------------");

                totalStaff++;
            }
        }

        if (totalStaff == 0) {
            System.out.println("No staff accounts found.");
        } else {
            System.out.println("Total Staff : " + totalStaff);
        }
    }
    
    // Returns list of staff (Counselor/Receptionist) for GUI display purposes
    public ArrayList<User> getStaffList(FileManager fileManager) {
        ArrayList<User> allUsers = fileManager.getAllUsers();
        ArrayList<User> staffList = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getRole() == Role.COUNSELOR || user.getRole() == Role.RECEPTIONIST) {
                staffList.add(user);
            }
        }

        return staffList;
    }

    // Update an existing staff account's name
    public boolean updateStaffName(String userID, String newName, FileManager fileManager) {
        ArrayList<User> allUsers = fileManager.getAllUsers();
        for (User u : allUsers) {
            if (u.getUserID().equals(userID)) {
                u.setName(newName);
                fileManager.saveAllUsers(allUsers);
                System.out.println("Updated name for " + userID + " to " + newName);
                return true;
            }
        }
        System.out.println("No user found with ID: " + userID);
        return false;
    }
    
    // Update an existing staff account's username
    public boolean updateStaffUsername(String userID, String newUsername, FileManager fileManager) {
        ArrayList<User> allUsers = fileManager.getAllUsers();

        // Check if the new username is already taken by someone else
        for (User u : allUsers) {
            if (u.getUsername().equalsIgnoreCase(newUsername) && !u.getUserID().equals(userID)) {
                System.out.println("Username already taken: " + newUsername);
                return false;
            }
        }

        for (User u : allUsers) {
            if (u.getUserID().equals(userID)) {
                u.setUsername(newUsername);
                fileManager.saveAllUsers(allUsers);
                System.out.println("Updated username for " + userID + " to " + newUsername);
                return true;
            }
        }

        System.out.println("No user found with ID: " + userID);
        return false;
    }
    
    // Update an existing staff account's password
    public boolean updateStaffPassword(String userID, String newPassword, FileManager fileManager) {
        ArrayList<User> allUsers = fileManager.getAllUsers();

        for (User u : allUsers) {
            if (u.getUserID().equals(userID)) {
                u.setPassword(newPassword);
                fileManager.saveAllUsers(allUsers);
                System.out.println("Updated password for " + userID);
                return true;
            }
        }

        System.out.println("No user found with ID: " + userID);
        return false;
    }

    // Delete a staff account
    public boolean deleteStaffAccount(String userID, FileManager fileManager) {
        ArrayList<User> allUsers = fileManager.getAllUsers();
        for (User u : allUsers) {
            if (u.getUserID().equals(userID)) {
                allUsers.remove(u);
                fileManager.saveAllUsers(allUsers);
                System.out.println("Deleted account: " + userID);
                return true;
            }
        }
        System.out.println("No user found with ID: " + userID);
        return false;
    }
    
    
    //Create a new counselor schedule.
    public Schedule createSchedule(String counselorID,
            String day,
            String startTime,
            String endTime,
            String status,
            FileManager fileManager) {

        // Validate that the Counselor ID actually exists and is a Counselor
        ArrayList<User> allUsers = fileManager.getAllUsers();
        boolean counselorExists = false;

        for (User u : allUsers) {
            if (u.getUserID().equals(counselorID) && u.getRole() == Role.COUNSELOR) {
                counselorExists = true;
                break;
            }
        }

        if (!counselorExists) {
            System.out.println("Cannot create schedule: Counselor ID '" + counselorID + "' does not exist.");
            return null;
        }
        
        // Generate Schedule ID automatically
        String scheduleID = fileManager.generateId(FileManager.SCHEDULE_FILE, "SCH");
        
        // Reject if this overlaps an existing slot already on file for this counselor
        if (hasScheduleConflict(counselorID, day, startTime, endTime, null, fileManager)) {
            System.out.println("Cannot create schedule: this overlaps an existing schedule for counselor "
                    + counselorID + " on " + day + ".");
            return null;
        }

        // Create Schedule object
        Schedule schedule = new Schedule(
                scheduleID,
                counselorID,
                day,
                startTime,
                endTime,
                status
        );

        // Save into schedule.txt
        fileManager.saveSchedule(schedule);

        System.out.println("Schedule created successfully!");
        System.out.println(schedule);

        return schedule;
    }
    
    //View all counselor schedules
    public void viewSchedules(FileManager fileManager) {

        // Load every schedule from schedule.txt
        ArrayList<Schedule> schedules = fileManager.getAllSchedules();

        System.out.println();
        System.out.println("========================================================");
        System.out.println("              COUNSELOR SCHEDULE LIST");
        System.out.println("========================================================");

        if (schedules.isEmpty()) {
            System.out.println("No schedules found.");
            System.out.println("========================================================");
            return;
        }

        for (Schedule schedule : schedules) {

            System.out.println("Schedule ID : " + schedule.getScheduleID());
            System.out.println("Counselor ID: " + schedule.getCounselorID());
            System.out.println("Day         : " + schedule.getDay());
            System.out.println("Time        : "
                    + schedule.getStartTime()
                    + " - "
                    + schedule.getEndTime());
            System.out.println("Status      : " + schedule.getStatus());

            System.out.println("--------------------------------------------------------");
        }

        System.out.println("Total Schedules : " + schedules.size());
        System.out.println("========================================================");
    }
    
    //Update an existing counselor schedule.
    public boolean updateSchedule(String scheduleID,
            String newDay,
            String newStartTime,
            String newEndTime,
            String newStatus,
            FileManager fileManager) {

        ArrayList<Schedule> schedules = fileManager.getAllSchedules();

        for (Schedule schedule : schedules) {

            if (schedule.getScheduleID().equalsIgnoreCase(scheduleID)) {

                // Counselor never changes on an update, only day/time/status -
                // reuse the existing counselorID for the conflict check
                if (hasScheduleConflict(schedule.getCounselorID(), newDay, newStartTime, newEndTime,
                        scheduleID, fileManager)) {
                    System.out.println("Cannot update schedule: this overlaps another existing schedule for counselor "
                            + schedule.getCounselorID() + " on " + newDay + ".");
                    return false;
                }
                
                schedule.setDay(newDay);
                schedule.setStartTime(newStartTime);
                schedule.setEndTime(newEndTime);
                schedule.setStatus(newStatus);

                fileManager.saveAllSchedules(schedules);

                System.out.println("Schedule updated successfully!");
                System.out.println(schedule);

                return true;
            }
        }

        System.out.println("Schedule ID not found.");

        return false;
    }
    
    
    //Delete a counselor schedule.
    public boolean deleteSchedule(String scheduleID,
            FileManager fileManager) {

        ArrayList<Schedule> schedules = fileManager.getAllSchedules();

        for (int i = 0; i < schedules.size(); i++) {

            if (schedules.get(i).getScheduleID().equalsIgnoreCase(scheduleID)) {

                schedules.remove(i);

                fileManager.saveAllSchedules(schedules);

                System.out.println("Schedule deleted successfully.");

                return true;
            }
        }

        System.out.println("Schedule ID not found.");

        return false;
    }
    
    //check if overlap issue
    private boolean hasScheduleConflict(String counselorID, String day, String startTime,
            String endTime, String excludeScheduleID, FileManager fileManager) {

        ArrayList<Schedule> allSchedules = fileManager.getAllSchedules();

        for (Schedule existing : allSchedules) {
            System.out.println("Comparing new [" + counselorID + "|" + day + "] with saved ["
                    + existing.getCounselorID() + "|" + existing.getDay() + "]");

            // Skip the record being edited (relevant for updateSchedule only)
            if (excludeScheduleID != null && existing.getScheduleID().equalsIgnoreCase(excludeScheduleID)) {
                continue;
            }

            // Only slots for the same counselor, on the same day, can conflict
            if (!existing.getCounselorID().trim().equalsIgnoreCase(counselorID.trim())
                    || !existing.getDay().trim().equalsIgnoreCase(day.trim())) {
                continue;
            }

            // Two time ranges [startA, endA) and [startB, endB) overlap when
            // startA is before endB AND startB is before endA. Times are
            // typed as "9:00 AM" / "5:00 PM" style, so they must be parsed
            // into real LocalTime values before comparing - plain string
            // comparison sorts "9:00 AM" after "10:00 AM" (because '9' > '1'),
            // which is wrong.
            LocalTime newStart = parseTime(startTime);
            LocalTime newEnd = parseTime(endTime);
            LocalTime existingStart = parseTime(existing.getStartTime());
            LocalTime existingEnd = parseTime(existing.getEndTime());

            // If either time can't be parsed, skip this comparison rather
            // than crash - the calling method is responsible for rejecting
            // bad input before it ever reaches here
            if (newStart == null || newEnd == null) {
                System.out.println("Invalid new schedule time: "
                        + startTime + " - " + endTime);
                return true; // reject invalid input; never bypass validation
            }

            if (existingStart == null || existingEnd == null) {
                System.out.println("Invalid saved schedule time in "
                        + existing.getScheduleID());
                continue;
            }

            boolean overlaps = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);

            if (overlaps) {
                return true;
            }
        }

        return false;
    }
    
    

    // Parses times typed as "9:00 AM" / "5:00 PM" (also accepts "09:00 AM").
    // Returns null instead of throwing if the text isn't in that format.
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(normalizeTimeText(text), TIME_FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
    
    // Cleans up common typing variations into one consistent "H:MM AM/PM" form:
    //  1. Uppercase everything, so "am"/"Am"/"AM" all match
    //  2. Turn a "." between digits into ":" so "9.00" becomes "9:00"
    //  3. Strip ALL whitespace, so "09:00 AM", "09:00  AM", "09:00AM" all
    //     collapse to the same thing
    //  4. Re-insert exactly one space before the trailing AM/PM, since the
    //     parser pattern "h:mm a" requires the space back
    private String normalizeTimeText(String text) {
        if (text == null) {
            return null;
        }

        String cleaned = text.trim().toUpperCase();
        cleaned = cleaned.replaceAll("(\\d)\\.(\\d)", "$1:$2");
        cleaned = cleaned.replaceAll("\\s+", "");
        cleaned = cleaned.replaceAll("(AM|PM)$", " $1");

        return cleaned;
    }
    

    //Display appointment statistics.
    public void monitorAppointmentStatistics(FileManager fileManager) {

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        int total = appointments.size();
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        int cancelled = 0;

        for (Appointment appointment : appointments) {

            switch (appointment.getStatus()) {

                case "Pending":
                    pending++;
                    break;

                case "Confirmed":
                    confirmed++;
                    break;

                case "Completed":
                    completed++;
                    break;

                case "Cancelled":
                    cancelled++;
                    break;
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("      APPOINTMENT STATISTICS");
        System.out.println("========================================");
        System.out.println("Total Appointments     : " + total);
        System.out.println("Pending Appointments   : " + pending);
        System.out.println("Confirmed Appointments : " + confirmed);
        System.out.println("Completed Appointments : " + completed);
        System.out.println("Cancelled Appointments : " + cancelled);
        System.out.println("========================================");
    }
    
    
    //Display the number of appointments handled by each counselor.
    public void monitorCounselorWorkload(FileManager fileManager) {

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        ArrayList<String> counselorIDs = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();

        for (Appointment appointment : appointments) {

            String counselorID = appointment.getCounselorID();

            int index = counselorIDs.indexOf(counselorID);

            if (index == -1) {
                counselorIDs.add(counselorID);
                counts.add(1);
            } else {
                counts.set(index, counts.get(index) + 1);
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("     COUNSELOR WORKLOAD");
        System.out.println("========================================");

        if (counselorIDs.isEmpty()) {
            System.out.println("No appointment records found.");
        } else {

            for (int i = 0; i < counselorIDs.size(); i++) {

                System.out.println(
                        counselorIDs.get(i)
                        + " : "
                        + counts.get(i)
                        + " appointment(s)"
                );

            }

        }

        System.out.println("========================================");
    }
    
    // Returns appointment statistics as an array: [total, pending, confirmed, completed, cancelled]
    public int[] getAppointmentStatistics(FileManager fileManager) {
        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        int total = appointments.size();
        int pending = 0, confirmed = 0, completed = 0, cancelled = 0;
        int walkIn = 0, online = 0;
        int today = 0, upcoming = 0;

        LocalDate now = LocalDate.now();

        for (Appointment appointment : appointments) {
            switch (appointment.getStatus()) {
                case "Pending":
                    pending++;
                    break;
                case "Confirmed":
                    confirmed++;
                    break;
                case "Completed":
                    completed++;
                    break;
                case "Cancelled":
                    cancelled++;
                    break;
            }

           
            if ("Walk-in".equalsIgnoreCase(appointment.getType())) {
                walkIn++;
            } else if ("Online".equalsIgnoreCase(appointment.getType())) {
                online++;
            }

            try {
                LocalDate appointmentDate = LocalDate.parse(appointment.getDate());
                if (appointmentDate.isEqual(now)) {
                    today++;
                } else if (appointmentDate.isAfter(now)
                        && ("Pending".equals(appointment.getStatus()) || "Confirmed".equals(appointment.getStatus()))) {
                    upcoming++;
                }
            } catch (DateTimeParseException e) {
                // skip unparseable dates
            }
        }

        return new int[]{total, pending, confirmed, completed, cancelled, walkIn, online, today, upcoming};
    }
    
    // Returns counselor workload as two parallel lists: counselor IDs and their appointment counts
    public ArrayList<Object[]> getCounselorWorkload(FileManager fileManager) {
        ArrayList<Appointment> appointments = fileManager.getAllAppointments();
        ArrayList<User> allUsers = fileManager.getAllUsers();

        ArrayList<String> counselorIDs = new ArrayList<>();
        ArrayList<int[]> statusCounts = new ArrayList<>();

        for (Appointment appointment : appointments) {
            String counselorID = appointment.getCounselorID();
            int index = counselorIDs.indexOf(counselorID);

            if (index == -1) {
                counselorIDs.add(counselorID);
                statusCounts.add(new int[4]);
                index = counselorIDs.size() - 1;
            }

            int[] counts = statusCounts.get(index);
            switch (appointment.getStatus()) {
                case "Pending":
                    counts[0]++;
                    break;
                case "Confirmed":
                    counts[1]++;
                    break;
                case "Completed":
                    counts[2]++;
                    break;
                case "Cancelled":
                    counts[3]++;
                    break;
            }
        }

        ArrayList<Object[]> result = new ArrayList<>();
        for (int i = 0; i < counselorIDs.size(); i++) {
            String counselorID = counselorIDs.get(i);
            String counselorName = "Unknown";
            for (User u : allUsers) {
                if (u.getUserID().equals(counselorID)) {
                    counselorName = u.getName();
                    break;
                }
            }
            int[] counts = statusCounts.get(i);
            int total = counts[0] + counts[1] + counts[2] + counts[3];
            result.add(new Object[]{counselorID, counselorName, counts[0], counts[1], counts[2], counts[3], total});
        }

        return result;
    }
    
    

    // METHOD OVERLOADING - generate report with different parameter sets
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // Works out the [from, to] date range (inclusive, "yyyy-MM-dd") that a
    // report period should cover, based on today's date.
    //   Daily     -> today only
    //   Monthly   -> the current calendar month (1st -> last day of month)
    //   Quarterly -> the last 3 months, rolling window ending today
    //   Yearly    -> the current calendar year (1 Jan -> 31 Dec)
    // Any other/unrecognised type falls back to covering all records.
    public String[] getReportDateRange(String reportType) {
        LocalDate today = LocalDate.now();
        LocalDate from;
        LocalDate to;

        if (reportType == null) {
            reportType = "";
        }

        switch (reportType.trim().toLowerCase(Locale.ROOT)) {
            case "daily":
                from = today;
                to = today;
                break;

            case "monthly":
                from = today.withDayOfMonth(1);
                to = today.withDayOfMonth(today.lengthOfMonth());
                break;

            case "quarterly":
                // Last 3 months, rolling window that ends today
                from = today.minusMonths(3).plusDays(1);
                to = today;
                break;

            case "yearly":
                from = today.withDayOfYear(1);
                to = LocalDate.of(today.getYear(), 12, 31);
                break;

            default:
                // Unknown type: don't filter, cover everything
                from = LocalDate.of(2000, 1, 1);
                to = LocalDate.of(2100, 12, 31);
                break;
        }

        return new String[]{from.format(REPORT_DATE_FORMAT), to.format(REPORT_DATE_FORMAT)};
    }
    
    public void generateReport(String reportType, FileManager fileManager) {
        // Auto-calculate the correct date range for Daily/Monthly/Quarterly/Yearly
        String[] range = getReportDateRange(reportType);
        generateReport(reportType, range[0], range[1], fileManager);
        

    }

    public void generateReport(String reportType,
            String dateFrom,
            String dateTo,
            FileManager fileManager) {

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();
        ArrayList<Appointment> filtered = new ArrayList<>();

        for (Appointment appointment : appointments) {
            if (appointment.getDate().compareTo(dateFrom) >= 0
                    && appointment.getDate().compareTo(dateTo) <= 0) {
                filtered.add(appointment);
            }
        }

        int total = filtered.size();
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        int cancelled = 0;

        for (Appointment appointment : filtered) {
            switch (appointment.getStatus()) {
                case "Pending":
                    pending++;
                    break;
                case "Confirmed":
                    confirmed++;
                    break;
                case "Completed":
                    completed++;
                    break;
                case "Cancelled":
                    cancelled++;
                    break;
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("      " + reportType.toUpperCase() + " REPORT");
        System.out.println("========================================");
        System.out.println("From : " + dateFrom);
        System.out.println("To   : " + dateTo);
        System.out.println("Total Appointments    : " + total);
        System.out.println("Pending               : " + pending);
        System.out.println("Confirmed             : " + confirmed);
        System.out.println("Completed             : " + completed);
        System.out.println("Cancelled             : " + cancelled);
        System.out.println("========================================");
    }
    
    // METHOD OVERLOADING - returns report as a String for GUI display
    public String getReportText(String reportType, FileManager fileManager) {
        String[] range = getReportDateRange(reportType);
        return getReportText(reportType, range[0], range[1], fileManager);
        
    }

    public String getReportText(String reportType, String dateFrom, String dateTo, FileManager fileManager) {

        // Guard against a reversed range (e.g. user typed From/To the wrong way round)
        if (dateFrom.compareTo(dateTo) > 0) {
            String temp = dateFrom;
            dateFrom = dateTo;
            dateTo = temp;
        }

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();
        ArrayList<Appointment> filtered = new ArrayList<>();

        for (Appointment appointment : appointments) {
            if (appointment.getDate().compareTo(dateFrom) >= 0
                    && appointment.getDate().compareTo(dateTo) <= 0) {
                filtered.add(appointment);
            }
        }

        int total = filtered.size();
        int pending = 0;
        int confirmed = 0;
        int completed = 0;
        int cancelled = 0;

        for (Appointment appointment : filtered) {
            switch (appointment.getStatus()) {
                case "Pending":
                    pending++;
                    break;
                case "Confirmed":
                    confirmed++;
                    break;
                case "Completed":
                    completed++;
                    break;
                case "Cancelled":
                    cancelled++;
                    break;
            }
        }

        String periodLabel = (reportType == null) ? "" : reportType.toUpperCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("        COUNSELING MANAGEMENT SYSTEM REPORT\n");
        sb.append("                 ").append(periodLabel).append(" REPORT\n");
        sb.append("====================================================\n");
        sb.append("Generated on          : ").append(LocalDate.now()).append("\n");
        sb.append("Reporting period      : ").append(dateFrom).append("  to  ").append(dateTo).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("APPOINTMENT SUMMARY\n");
        sb.append("Total Appointments    : ").append(total).append("\n");
        sb.append("Pending               : ").append(pending).append("\n");
        sb.append("Confirmed             : ").append(confirmed).append("\n");
        sb.append("Completed             : ").append(completed).append("\n");
        sb.append("Cancelled             : ").append(cancelled).append("\n");
        sb.append("====================================================\n");
        sb.append("End of report\n");

        return sb.toString();
    }


}
