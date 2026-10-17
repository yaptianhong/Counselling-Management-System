package cms;


public class Schedule {

    private String scheduleID;
    private String counselorID;
    private String day;         // e.g. "Monday"
    private String startTime;
    private String endTime;
    private String status;      // Available / On Leave / Off

    public Schedule(String scheduleID, String counselorID, String day,
                     String startTime, String endTime, String status) {
        this.scheduleID = scheduleID;
        this.counselorID = counselorID;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    
    //getter
    public String getScheduleID() { return scheduleID; }
    public String getCounselorID() { return counselorID; }
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    
    //setter
    public void setDay(String day) {
        this.day = day;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toFileString() {
        return scheduleID + "|" + counselorID + "|" + day + "|" + startTime + "|" + endTime + "|" + status;
    }

    @Override
    public String toString() {
        return day + " " + startTime + "-" + endTime + " (" + status + ") - Counselor:" + counselorID;
    }
}
