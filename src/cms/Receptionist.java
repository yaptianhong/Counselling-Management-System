package cms;

import java.util.ArrayList;


public class Receptionist extends User {

    private static int queueCounter = 0; // shared across all Receptionist objects

    public Receptionist(String userID, String username, String password, String name) {
        super(userID, username, password, name, Role.RECEPTIONIST);
    }

    @Override
    public String getDashboardTitle() {
        return "Receptionist Dashboard";
    }

    /**
     * Register a new Student account. The userID is auto-generated using
     * FileManager.generateId() with prefix "S" - no manual ID typing needed.
     *
     * @return the newly created Student if successful, or null if it failed
     */
    public Student registerStudentAccount(String username, String password, String name, FileManager fileManager) {
        String newID = fileManager.generateId(FileManager.USERS_FILE, "S");
        Student newStudent = new Student(newID, username, password, name);

        String error = fileManager.registerUser(newStudent);
        if (error == null) {
            System.out.println("Student account created successfully: " + newStudent);
            return newStudent;
        } else {
            System.out.println("Failed to create account: " + error);
            return null;
        }
    }

    /**
     * Create a new appointment. The appointmentID is auto-generated using
     * FileManager.generateId() with prefix "APT".
     * METHOD OVERLOADING - walk-in gets a queue number immediately, online does not.
     */
    public Appointment createAppointment(String studentID, String counselorID, String date,
                                          String time, FileManager fileManager) {
        // Walk-in booking: queue number generated immediately
        String newID = fileManager.generateId(FileManager.APPOINTMENTS_FILE, "APT");
        int queueNo = generateQueueNumber();
        Appointment appt = new Appointment(newID, studentID, counselorID, date, time,
                "Pending", "Walk-in", queueNo);
        fileManager.saveAppointment(appt);
        return appt;
    }

    public Appointment createAppointment(String studentID, String counselorID, String date,
                                          String time, boolean isOnline, FileManager fileManager) {
        // Online booking: queue number assigned only on arrival (0 = not yet issued)
        String newID = fileManager.generateId(FileManager.APPOINTMENTS_FILE, "APT");
        Appointment appt = new Appointment(newID, studentID, counselorID, date, time,
                "Pending", "Online", 0);
        fileManager.saveAppointment(appt);
        return appt;
    }

    public int generateQueueNumber() {
        queueCounter++;
        return queueCounter;
    }

    // Cancel an existing appointment
    public boolean cancelAppointment(String appointmentID, FileManager fileManager) {
        ArrayList<Appointment> allAppointments = fileManager.getAllAppointments();
        for (Appointment a : allAppointments) {
            if (a.getAppointmentID().equals(appointmentID)) {
                a.setStatus("Cancelled");
                fileManager.saveAllAppointments(allAppointments);
                System.out.println("Appointment " + appointmentID + " cancelled.");
                return true;
            }
        }
        System.out.println("No appointment found with ID: " + appointmentID);
        return false;
    }
}
