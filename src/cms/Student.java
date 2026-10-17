package cms;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;


public class Student extends User {

    public Student(String userID, String username, String password, String name) {
        super(userID, username, password, name, Role.STUDENT);
    }

    @Override
    public String getDashboardTitle() {
        return "Student Dashboard";
    }

    // View only the appointments belonging to the logged-in student
    public ArrayList<Appointment> viewMyHistory(FileManager fileManager) {
        ArrayList<Appointment> myAppointments = new ArrayList<>();

        for (Appointment appointment : fileManager.getAllAppointments()) {
            if (appointment.getStudentID().equals(getUserID())) {
                myAppointments.add(appointment);
            }
        }

        return myAppointments;
    }

    // Student books an online appointment
    public Appointment bookOnlineAppointment(String counselorID, String date,
                                             String time, FileManager fileManager) {

        if (!isValidAppointmentInput(counselorID, date, time)) {
            return null;
        }

        // Check that the selected counselor exists
        boolean counselorExists = false;

        for (User user : fileManager.getAllUsers()) {
            if (user.getUserID().equals(counselorID)
                    && user instanceof Counselor) {
                counselorExists = true;
                break;
            }
        }

        if (!counselorExists) {
            System.out.println("Counselor ID does not exist.");
            return null;
        }

        // Prevent two active appointments with the same counselor/date/time
        for (Appointment appointment : fileManager.getAllAppointments()) {
            if (appointment.getCounselorID().equals(counselorID)
                    && appointment.getDate().equals(date)
                    && appointment.getTime().equals(time)
                    && !appointment.getStatus().equalsIgnoreCase("Cancelled")) {

                System.out.println("This counselor is not available at the selected date and time.");
                return null;
            }
        }

        String appointmentID = fileManager.generateId(
                FileManager.APPOINTMENTS_FILE, "APT");

        Appointment newAppointment = new Appointment(
                appointmentID,
                getUserID(),
                counselorID,
                date,
                time,
                "Pending",
                "Online",
                0
        );

        fileManager.saveAppointment(newAppointment);

        System.out.println("Appointment booked successfully.");
        System.out.println("Appointment ID: " + appointmentID);

        return newAppointment;
    }

    // Student changes date and time of their own active appointment
    public boolean rescheduleAppointment(String appointmentID, String newDate,
                                         String newTime, FileManager fileManager) {

        if (!isValidDateAndTime(newDate, newTime)) {
            return false;
        }

        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentID().equals(appointmentID)
                    && appointment.getStudentID().equals(getUserID())) {

                if (appointment.getStatus().equalsIgnoreCase("Cancelled")
                        || appointment.getStatus().equalsIgnoreCase("Completed")) {

                    System.out.println("This appointment cannot be rescheduled.");
                    return false;
                }

                appointment.setDate(newDate);
                appointment.setTime(newTime);

                fileManager.saveAllAppointments(appointments);

                System.out.println("Appointment rescheduled successfully.");
                return true;
            }
        }

        System.out.println("Appointment not found, or it does not belong to you.");
        return false;
    }

    // Student cancels their own appointment without deleting its history
    public boolean cancelAppointment(String appointmentID, FileManager fileManager) {
        ArrayList<Appointment> appointments = fileManager.getAllAppointments();

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentID().equals(appointmentID)
                    && appointment.getStudentID().equals(getUserID())) {

                if (appointment.getStatus().equalsIgnoreCase("Cancelled")) {
                    System.out.println("This appointment is already cancelled.");
                    return false;
                }

                if (appointment.getStatus().equalsIgnoreCase("Completed")) {
                    System.out.println("A completed appointment cannot be cancelled.");
                    return false;
                }

                appointment.setStatus("Cancelled");
                fileManager.saveAllAppointments(appointments);

                System.out.println("Appointment cancelled successfully.");
                return true;
            }
        }

        System.out.println("Appointment not found, or it does not belong to you.");
        return false;
    }

    // View active appointments that already have a queue number
    public ArrayList<Appointment> viewMyQueueNumbers(FileManager fileManager) {
        ArrayList<Appointment> queueAppointments = new ArrayList<>();

        for (Appointment appointment : viewMyHistory(fileManager)) {
            if (appointment.getQueueNumber() > 0
                    && !appointment.getStatus().equalsIgnoreCase("Cancelled")) {

                queueAppointments.add(appointment);
            }
        }

        return queueAppointments;
    }

    // View the available counselor profiles
    public ArrayList<Counselor> viewCounselorProfiles(FileManager fileManager) {
        ArrayList<Counselor> counselors = new ArrayList<>();

        for (User user : fileManager.getAllUsers()) {
            if (user instanceof Counselor) {
                counselors.add((Counselor) user);
            }
        }

        return counselors;
    }

    // Optional extra function: Student can read records for their own appointments
    public ArrayList<ConsultationRecord> viewMyConsultationRecords(
            FileManager fileManager) {

        ArrayList<ConsultationRecord> myRecords = new ArrayList<>();
        ArrayList<Appointment> myAppointments = viewMyHistory(fileManager);

        for (ConsultationRecord record : fileManager.getAllRecords()) {
            for (Appointment appointment : myAppointments) {
                if (record.getAppointmentID().equals(
                        appointment.getAppointmentID())) {

                    myRecords.add(record);
                    break;
                }
            }
        }

        return myRecords;
    }
    
    public boolean changeMyPassword(String currentPassword,
                                String newPassword,
                                String confirmPassword,
                                FileManager fileManager) {

    if (currentPassword == null || currentPassword.trim().isEmpty()
            || newPassword == null || newPassword.trim().isEmpty()
            || confirmPassword == null || confirmPassword.trim().isEmpty()) {

        System.out.println("All password fields are required.");
        return false;
    }

    if (!getPassword().equals(currentPassword)) {
        System.out.println("Current password is incorrect.");
        return false;
    }

    if (newPassword.length() < 6) {
        System.out.println("New password must contain at least 6 characters.");
        return false;
    }

    if (!newPassword.equals(confirmPassword)) {
        System.out.println("New password and confirmation password do not match.");
        return false;
    }

    ArrayList<User> users = fileManager.getAllUsers();

    for (User user : users) {
        if (user.getUserID().equals(getUserID())) {

            user.setPassword(newPassword);
            fileManager.saveAllUsers(users);

            // Update password in the current logged-in Student object too
            setPassword(newPassword);

            return true;
        }
    }

    System.out.println("Student account not found.");
    return false;
}

    // Checks empty fields and validates the expected date/time format
    private boolean isValidAppointmentInput(String counselorID, String date,
                                            String time) {

        if (counselorID == null || counselorID.trim().isEmpty()) {
            System.out.println("Counselor ID cannot be empty.");
            return false;
        }

        return isValidDateAndTime(date, time);
    }

    private boolean isValidDateAndTime(String date, String time) {
    try {
        LocalDate appointmentDate = LocalDate.parse(date);
        LocalTime.parse(time);
 
        if (appointmentDate.isBefore(LocalDate.now())) {
            System.out.println(
                    "Appointment date cannot be before today.");
            return false;
        }
 
        return true;
 
    } catch (DateTimeParseException e) {
        System.out.println(
                "Invalid date or time. Use YYYY-MM-DD and HH:MM.");
        return false;
    }
}
}


