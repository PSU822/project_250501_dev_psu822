package Web01.FindRoom.restful.api.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 강의실 정보 매핑 엔티티
@Entity
@Table(name = "lecture_room")
public class LectureRoom {

    @Id
    @Column(name = "classId", length = 15)
    private String classId;

    @Column(name = "building", length = 50, nullable = false)
    private String building;

    @Column(name = "room", length = 50, nullable = false)
    private String room;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "current_occupancy", nullable = false)
    private Integer currentOccupancy = 0;

    @Column(name = "cnt_alone_study", nullable = false)
    private Integer cntAloneStudy = 0;

    @Column(name = "cnt_group_meeting", nullable = false)
    private Integer cntGroupMeeting = 0;

    @Column(name = "cnt_quiet", nullable = false)
    private Integer cntQuiet = 0;

    @Column(name = "cnt_free_talk", nullable = false)
    private Integer cntFreeTalk = 0;

    @Column(name = "cnt_short_stay", nullable = false)
    private Integer cntShortStay = 0;

    @Column(name = "cnt_comfortable", nullable = false)
    private Integer cntComfortable = 0;

    public LectureRoom() {
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentOccupancy() {
        return currentOccupancy;
    }

    public void setCurrentOccupancy(Integer currentOccupancy) {
        this.currentOccupancy = currentOccupancy;
    }

    public Integer getCntAloneStudy() {
        return cntAloneStudy;
    }

    public void setCntAloneStudy(Integer cntAloneStudy) {
        this.cntAloneStudy = cntAloneStudy;
    }

    public Integer getCntGroupMeeting() {
        return cntGroupMeeting;
    }

    public void setCntGroupMeeting(Integer cntGroupMeeting) {
        this.cntGroupMeeting = cntGroupMeeting;
    }

    public Integer getCntQuiet() {
        return cntQuiet;
    }

    public void setCntQuiet(Integer cntQuiet) {
        this.cntQuiet = cntQuiet;
    }

    public Integer getCntFreeTalk() {
        return cntFreeTalk;
    }

    public void setCntFreeTalk(Integer cntFreeTalk) {
        this.cntFreeTalk = cntFreeTalk;
    }

    public Integer getCntShortStay() {
        return cntShortStay;
    }

    public void setCntShortStay(Integer cntShortStay) {
        this.cntShortStay = cntShortStay;
    }

    public Integer getCntComfortable() {
        return cntComfortable;
    }

    public void setCntComfortable(Integer cntComfortable) {
        this.cntComfortable = cntComfortable;
    }
}
