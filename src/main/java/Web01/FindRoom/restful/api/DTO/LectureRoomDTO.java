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
    private List<String> availableRooms;
    private List<String> top3Hashtags;
    private Integer count;

    // search 리퀘스트
    public static LectureRoomDTO forSearch(String building, String weekday, String startTime, String endTime) {
        return LectureRoomDTO.builder()
                .building(building)
                .weekday(weekday)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public static LectureRoomDTO forSearchResponse(List<String> availableRooms, Integer count) {
        return LectureRoomDTO.builder()
                .availableRooms(availableRooms)
                .count(count)
                .build();
    }

    public static LectureRoomDTO forSelect(String building, String classId) {
        return LectureRoomDTO.builder()
                .building(building)
                .classId(classId)
                .build();
    }

    // 사용 이력 조회용, 강의실 ID와 건물명만 반환
    public static LectureRoomDTO forSelectResponse(String building, String classId, Integer floor, List<String> top3Hashtags, Integer capacity, Integer currentOccupancy) {
        return LectureRoomDTO.builder()
                .building(building)
                .classId(classId)
                .floor(floor)
                .top3Hashtags(top3Hashtags)
                .capacity(capacity)
                .currentOccupancy(currentOccupancy)
                .build();
    }

    // 쿠키 열람 후 쿠키 userId로 history 조회 -> history에서 가져올 classid, building 정보
    public static LectureRoomDTO forUsage(String classId, String building) {
        return LectureRoomDTO.builder()
                .classId(classId)
                .building(building)
                .build();
    }
}
