package Web01.FindRoom.restful.api.Entity;

import jakarta.persistence.*;

// 강의 시간표 매핑 엔티티
@Entity
@Table(name = "lecture_schedule")
public class LectureSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "weekday", length = 10, nullable = false)
    private String weekday;

    @Column(name = "start_time", length = 10, nullable = false)
    private String startTime;

    @Column(name = "end_time", length = 10, nullable = false)
    private String endTime;

    @Column(name = "classId", length = 15, nullable = false)
    private String classId;

    @Column(name = "lecture_id", nullable = false)
    private Integer lectureId;

    // 기본 생성자
    public LectureSchedule() {
    }

    // Getter/Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Integer getLectureId() {
        return lectureId;
    }

    public void setLectureId(Integer lectureId) {
        this.lectureId = lectureId;
    }
}
