package cn.edu.sdu.online.isdu.bean;

public class StudyRoom {
    private String building;
    private String classroom;
    private int[] status;

    public StudyRoom() {
        status = new int[6];
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public int[] getStatus() {
        return status;
    }

    public void setStatus(int[] status) {
        this.status = status;
    }
}
