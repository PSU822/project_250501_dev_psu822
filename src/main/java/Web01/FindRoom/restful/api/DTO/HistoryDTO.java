package Web01.FindRoom.restful.api.DTO;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryDTO {

    private String classId;
    private Integer participantCount;
    private Set<String> hashtags;
    private String endTime;
    private String weekday;
    private EventType eventType;

    public static HistoryDTO forRequestHistory(String classId, String weekday, Integer participantCount, Set<String> hashtags, String endTime, EventType eventType) {
        return HistoryDTO.builder()
                .classId(classId)
                .weekday(weekday)
                .participantCount(participantCount)
                .hashtags(hashtags)
                .endTime(endTime)
                .eventType(eventType)
                .build();
    }

    public enum EventType {
        usage_start,
        usage_end,
        favorite_add,
        favorite_remove
    }
}
