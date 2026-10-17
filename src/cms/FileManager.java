package cms;

import java.io.*;
import java.util.ArrayList;


// File formats (pipe-delimited "|"):
// users.txt         -> userID|username|password|name|role
// appointments.txt  -> appointmentID|studentID|counselorID|date|time|status|type|queueNumber
// records.txt       -> recordID|appointmentID|notes|recommendations
// schedule.txt      -> scheduleID|counselorID|day|startTime|endTime|status

public class FileManager {

    public static final String USERS_FILE = "users.txt";
    public static final String APPOINTMENTS_FILE = "appointments.txt";
    public static final String RECORDS_FILE = "records.txt";
    public static final String SCHEDULE_FILE = "schedule.txt";

    /**
     * Checks a username/password against every stored user and returns the
     * matching User object (already the correct subclass - Admin/Counselor/
     * Receptionist/Student - thanks to loadUsers() rebuilding real objects).
     * Used directly by LoginPage.java so the form doesn't need its own
     * login-checking logic.
     *
     * @return the matching User, or null if no account matches
     */
    public static User getLoggedInUser(String username, String password) {
        FileManager fileManager = new FileManager();
        ArrayList<User> allUsers = fileManager.loadUsers();
        for (User u : allUsers) {
            if (u.login(username, password)) {
                return u;
            }
        }
        return null;
    }

    // ---------- ID GENERATION ----------

    /**
     * Generates the next unique ID for a given file, based on a prefix.
     * Scans existing IDs starting with that prefix, finds the highest
     * number used so far, and returns prefix + (highest + 1).
     * e.g. if A001 and A002 already exist, generateId(USERS_FILE, "A") -> "A003"
     */
    public String generateId(String fileName, String prefix) {
        ArrayList<String> lines = readLines(fileName);
        int maxNumber = 0;

        for (String line : lines) {
            String[] fields = line.split("\\|");
            if (fields.length > 0) {
                String id = fields[0];
                if (id.startsWith(prefix)) {
                    String numberPart = id.substring(prefix.length());
                    try {
                        int number = Integer.parseInt(numberPart);
                        if (number > maxNumber) {
                            maxNumber = number;
                        }
                    } catch (NumberFormatException e) {
                        // ID doesn't end in a plain number - skip it
                    }
                }
            }
        }
        int nextNumber = maxNumber + 1;
        return prefix + String.format("%03d", nextNumber); // e.g. A001, A012, A123
    }

    // Reads every line of any file as plain text - used only by generateId()
    private ArrayList<String> readLines(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        File file = new File(fileName);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    // ---------- USERS ----------

    public ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) return users;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;

                String userID = parts[0];
                String username = parts[1];
                String password = parts[2];
                String name = parts[3];
                String roleText = parts[4];

                Role role;
                try {
                    role = Role.valueOf(roleText);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown role in users.txt: " + roleText);
                    continue;
                }

                switch (role) {
                    case ADMIN:
                        users.add(new Admin(userID, username, password, name));
                        break;
                    case COUNSELOR:
                        users.add(new Counselor(userID, username, password, name));
                        break;
                    case RECEPTIONIST:
                        users.add(new Receptionist(userID, username, password, name));
                        break;
                    case STUDENT:
                        users.add(new Student(userID, username, password, name));
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
        return users;
    }

    /**
     * Validates and registers a brand new user account.
     * Used by BOTH Admin (creating Counselor/Receptionist) and Receptionist
     * (creating Student) so registration rules stay consistent everywhere.
     *
     * @return null if registration succeeded, otherwise an error message
     */
    public String registerUser(User newUser) {
        if (newUser.getUserID() == null || newUser.getUserID().trim().isEmpty()) {
            return "User ID cannot be empty.";
        }
        if (newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
            return "Username cannot be empty.";
        }
        if (newUser.getPassword() == null || newUser.getPassword().trim().isEmpty()) {
            return "Password cannot be empty.";
        }
        if (newUser.getName() == null || newUser.getName().trim().isEmpty()) {
            return "Name cannot be empty.";
        }

        ArrayList<User> existingUsers = loadUsers();
        for (User u : existingUsers) {
            if (u.getUserID().equalsIgnoreCase(newUser.getUserID())) {
                return "User ID already exists. Please use a different ID.";
            }
            if (u.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                return "Username already taken. Please choose a different username.";
            }
        }

        saveUser(newUser);
        return null;
    }

    public void saveUser(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(user.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public void saveAllUsers(ArrayList<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
            for (User u : users) {
                writer.write(u.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public ArrayList<User> getAllUsers() {
        return loadUsers();
    }

    // ---------- APPOINTMENTS ----------

    public ArrayList<Appointment> loadAppointments() {
        ArrayList<Appointment> appointments = new ArrayList<>();
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return appointments;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 8) continue;
                appointments.add(new Appointment(p[0], p[1], p[2], p[3], p[4], p[5], p[6],
                        Integer.parseInt(p[7])));
            }
        } catch (IOException e) {
            System.out.println("Error reading appointments file: " + e.getMessage());
        }
        return appointments;
    }

    public ArrayList<Appointment> getAllAppointments() {
        return loadAppointments();
    }

    public void saveAppointment(Appointment appt) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE, true))) {
            writer.write(appt.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving appointment: " + e.getMessage());
        }
    }

    public void saveAllAppointments(ArrayList<Appointment> appointments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE, false))) {
            for (Appointment a : appointments) {
                writer.write(a.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }

    // ---------- CONSULTATION RECORDS ----------

    public ArrayList<ConsultationRecord> loadRecords() {
        ArrayList<ConsultationRecord> records = new ArrayList<>();
        File file = new File(RECORDS_FILE);
        if (!file.exists()) return records;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 4) continue;
                records.add(new ConsultationRecord(p[0], p[1], p[2], p[3]));
            }
        } catch (IOException e) {
            System.out.println("Error reading records file: " + e.getMessage());
        }
        return records;
    }

    public ArrayList<ConsultationRecord> getAllRecords() {
        return loadRecords();
    }

    public void saveRecord(ConsultationRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORDS_FILE, true))) {
            writer.write(record.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving record: " + e.getMessage());
        }
    }
    
    public void saveAllRecords(ArrayList<ConsultationRecord> records) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORDS_FILE, false))) {
            for (ConsultationRecord r : records) {
                writer.write(r.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving records: " + e.getMessage());
        }
    }

    // ---------- SCHEDULE ----------

    public ArrayList<Schedule> loadSchedules() {
        ArrayList<Schedule> schedules = new ArrayList<>();
        File file = new File(SCHEDULE_FILE);
        if (!file.exists()) return schedules;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 6) continue;
                schedules.add(new Schedule(p[0], p[1], p[2], p[3], p[4], p[5]));
            }
        } catch (IOException e) {
            System.out.println("Error reading schedule file: " + e.getMessage());
        }
        return schedules;
    }

    public ArrayList<Schedule> getAllSchedules() {
        return loadSchedules();
    }

    public void saveSchedule(Schedule schedule) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCHEDULE_FILE, true))) {
            writer.write(schedule.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
        }
    }

    public void saveAllSchedules(ArrayList<Schedule> schedules) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCHEDULE_FILE, false))) {
            for (Schedule s : schedules) {
                writer.write(s.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving schedules: " + e.getMessage());
        }
    }
}
