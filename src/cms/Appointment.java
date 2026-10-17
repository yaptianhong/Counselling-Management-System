package cms;


public class Appointment {

    private String appointmentID;
    private String studentID;
    private String counselorID;
    private String date;
    private String time;
    private String status;       // Pending / Confirmed / Cancelled / Completed
    private String type;         // Walk-in / Online
    private int queueNumber;     // 0 if not yet issued

    public Appointment(String appointmentID, String studentID, String counselorID,
                        String date, String time, String status, String type, int queueNumber) {
        this.appointmentID = appointmentID;
        this.studentID = studentID;
        this.counselorID = counselorID;
        this.date = date;
        this.time = time;
        this.status = status;
        this.type = type;
        this.queueNumber = queueNumber;
    }

    public String getAppointmentID() { return appointmentID; }
    public String getStudentID() { return studentID; }
    public String getCounselorID() { return counselorID; }
    public void setCounselorID(String counselorID) { this.counselorID = counselorID; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public int getQueueNumber() { return queueNumber; }
    public void setQueueNumber(int queueNumber) { this.queueNumber = queueNumber; }

    public String toFileString() {
        return appointmentID + "|" + studentID + "|" + counselorID + "|" + date + "|" +
                time + "|" + status + "|" + type + "|" + queueNumber;
    }

    @Override
    public String toString() {
        return "Appt[" + appointmentID + "] " + date + " " + time +
                " | Student:" + studentID + " Counselor:" + counselorID +
                " | " + status + " (" + type + ")";
    }
}
