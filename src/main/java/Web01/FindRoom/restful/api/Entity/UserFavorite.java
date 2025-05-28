package Web01.FindRoom.restful.api.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_favorite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite {

    @EmbeddedId
    private UserFavoriteId id;

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;

    @CreationTimestamp
    @Column(name = "favorited_at", nullable = false, updatable = false)
    private LocalDateTime favoritedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserFavoriteId implements Serializable {

        @Column(name = "user_id", length = 50)
        private String userId;

        @Column(name = "classId", length = 15)
        private String classId;

        @Enumerated(EnumType.STRING)
        @Column(name = "weekday")
        private Weekday weekday;

        @Column(name = "start_time")
        private LocalTime startTime;

        @Column(name = "end_time")
        private LocalTime endTime;
    }

    public enum Weekday {
        월, 화, 수, 목, 금, 토, 일
    }

    public static UserFavorite create(String userId, String classId, Weekday weekday,
            LocalTime startTime, LocalTime endTime, Integer participantCount) {
        UserFavoriteId id = new UserFavoriteId(userId, classId, weekday, startTime, endTime);
        return UserFavorite.builder()
                .id(id)
                .participantCount(participantCount)
                .build();
    }
}
