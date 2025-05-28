package Web01.FindRoom.restful.api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavDTO {

    private String classId;
    private String weekday;
    private String startTime;
    private String endTime;
    private Integer participantCount;

    private String building;
    private String room;
    private Integer floor;
    private String favoritedAt;

}
