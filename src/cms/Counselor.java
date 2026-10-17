package cms;

import java.util.ArrayList;


public class Counselor extends User {

    public Counselor(String userID, String username, String password, String name) {
        super(userID, username, password, name, Role.COUNSELOR);
    }

    @Override
    public String getDashboardTitle() {
        return "Counselor Dashboard";
    }

    public ArrayList<Schedule> viewMyRoster(FileManager fileManager) {
        ArrayList<Schedule> allSchedules = fileManager.getAllSchedules();
        ArrayList<Schedule> mySchedule = new ArrayList<>();
        for (Schedule s : allSchedules) {
            if (s.getCounselorID().equals(this.getUserID())) {
                mySchedule.add(s);
            }
        }
        return mySchedule;
    }

    public ArrayList<Appointment> viewMyAppointments(FileManager fileManager) {
        ArrayList<Appointment> allAppointments = fileManager.getAllAppointments();
        ArrayList<Appointment> myAppointments = new ArrayList<>();
        for (Appointment a : allAppointments) {
            if (a.getCounselorID().equals(this.getUserID())) {
                myAppointments.add(a);
            }
        }
        return myAppointments;
    }
    
    public ArrayList<ConsultationRecord> getRecordsForStudent(String studentId, FileManager fileManager) {
        ArrayList<ConsultationRecord> allRecords = fileManager.getAllRecords();
        ArrayList<Appointment> myAppts = viewMyAppointments(fileManager);
        ArrayList<ConsultationRecord> result = new ArrayList<>();

        for (ConsultationRecord r : allRecords) {
            for (Appointment a : myAppts) {
                if (a.getAppointmentID().equals(r.getAppointmentID())) {
                    if (studentId.equals("ALL") || a.getStudentID().equals(studentId)) {
                        result.add(r);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Add a consultation note. The recordID is auto-generated using
     * FileManager.generateId() with prefix "REC".
     */
    public ConsultationRecord addConsultationNote(String appointmentID, String notes,
                                                   String recommendations, FileManager fileManager) {
        String newID = fileManager.generateId(FileManager.RECORDS_FILE, "REC");
        ConsultationRecord record = new ConsultationRecord(newID, appointmentID, notes, recommendations);
        fileManager.saveRecord(record);
        
        ArrayList<Appointment> allAppts = fileManager.getAllAppointments();
        for (Appointment a : allAppts) {
            if (a.getAppointmentID().equals(appointmentID)) {
                a.setStatus("Completed");
                break;
            }
        }
        fileManager.saveAllAppointments(allAppts);
        
        System.out.println("Consultation record saved for appointment " + appointmentID);
        return record;
        
        
    }
}
