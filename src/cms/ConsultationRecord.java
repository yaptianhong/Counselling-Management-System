package cms;


public class ConsultationRecord {

    private String recordID;
    private String appointmentID;
    private String notes;
    private String recommendations;

    public ConsultationRecord(String recordID, String appointmentID, String notes, String recommendations) {
        this.recordID = recordID;
        this.appointmentID = appointmentID;
        this.notes = notes;
        this.recommendations = recommendations;
    }

    public String getRecordID() { return recordID; }
    public String getAppointmentID() { return appointmentID; }
    public String getNotes() { return notes; }
    public String getRecommendations() { return recommendations; }

    public String toFileString() {
        return recordID + "|" + appointmentID + "|" + notes + "|" + recommendations;
    }

    @Override
    public String toString() {
        return "Record[" + recordID + "] for Appt:" + appointmentID +
                " | Notes: " + notes + " | Rec: " + recommendations;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
}
