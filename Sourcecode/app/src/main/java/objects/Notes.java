package objects;


import com.google.firebase.Timestamp;

public class Notes {
    private String title;
    private String notes;
    private Timestamp timeAdded;
    private String userName;
    private String userId;

    public Notes() { } //Empty Contructor required for firebase

    public Notes(String title, String notes, Timestamp timeAdded, String userName, String userId) {
        this.title = title;
        this.notes = notes;
        this.timeAdded = timeAdded;
        this.userName = userName;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
