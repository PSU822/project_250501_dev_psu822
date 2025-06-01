package Web01.FindRoom.restful.api.DTO;

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
public class RoomSearchResultDTO {

    private String classId;
    private String room;
    private String capacity;
    private String availableTime;
}
