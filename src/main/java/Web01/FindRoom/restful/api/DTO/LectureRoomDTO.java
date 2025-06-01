package Web01.FindRoom.restful.api.DTO;

import java.util.List;

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
@JsonInclude(JsonInclude.Include.NON_NULL)  // 응답 형식에 NULL이 있는 것이 프론트에서 보고 쓰기 어렵다는 판단으로 추가
public class LectureRoomDTO {

    // 강의실 
    private String classId;
    private String room;
    private String building;

    private Integer floor;
    private Integer capacity;
    private Integer currentOccupancy;

    // 강의실 스케쥴
    private Integer lectureId;
    private String weekday;
    private String startTime;
    private String endTime;

    // 응답으로 받을 data
    private List<RoomSearchResultDTO> availableRoomsData;
    private List<ScheduleDetailDTO> scheduleDetails;
    private List<String> top3Hashtags;
    private Integer count;
}
