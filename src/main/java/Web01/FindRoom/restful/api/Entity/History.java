package Web01.FindRoom.restful.api.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "history")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "classId", nullable = false, length = 15)
    private String classId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "participant_count")
    private Integer participantCount;

    @Column(name = "hashtags", columnDefinition = "SET('혼자 개인 공부해요','여럿이서 회의해요','조용하게 있어요','자유롭게 대화해요','아주 잠깐 머물러요','편하게 있어요')")
    private String hashtags;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Enum 정의
    public enum EventType {
        usage_start,
        usage_end,
        favorite_add,
        favorite_remove
    }

    // 기본 생성자
    public History() {
    }

    // 생성자
    public History(String userId, String classId, LocalDateTime startTime, LocalDateTime endTime,
            Integer participantCount, String hashtags, EventType eventType) {
        this.userId = userId;
        this.classId = classId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.participantCount = participantCount;
        this.hashtags = hashtags;
        this.eventType = eventType;
    }

    // Getter & Setter
    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // toString
    @Override
    public String toString() {
        return "History{"
                + "historyId=" + historyId
                + ", userId='" + userId + '\''
                + ", classId='" + classId + '\''
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", participantCount=" + participantCount
                + ", hashtags='" + hashtags + '\''
                + ", eventType=" + eventType
                + ", createdAt=" + createdAt
                + '}';
    }
}
