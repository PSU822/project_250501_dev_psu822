package Web01.FindRoom.restful.api.DTO;

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
public class UsageDTO {

    private String classId;
    private String building;
    private Integer participantCount;
    private String hashtags;
    private Integer capacity;
    private Integer currentOccupancy;
}
