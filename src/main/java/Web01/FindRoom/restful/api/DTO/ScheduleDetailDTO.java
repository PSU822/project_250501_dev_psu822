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
public class ScheduleDetailDTO {

    private String courseName;
    private String startTime;
    private String endTime;
}
