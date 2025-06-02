package Web01.FindRoom.restful.api.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.LectureRoomDTO;
import Web01.FindRoom.restful.api.DTO.RoomSearchResultDTO;
import Web01.FindRoom.restful.api.DTO.ScheduleDetailDTO;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class LectureRoomService {

    private static final Logger logger = LoggerFactory.getLogger(LectureRoomService.class);

    @Autowired
    private EntityManager entityManager;

    public APIResponseDTO<LectureRoomDTO> search(LectureRoomDTO searchRequest) {
        logger.info("강의실 검색 시도: 건물={}, 요일={}, 시간={}",
                searchRequest.getBuilding(), searchRequest.getWeekday(), searchRequest.getStartTime());
        //native sql + search 연산 변경으로 메소드 분리가 더 코드 리뷰가 힘들어진다고 판단.
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> roomResults = entityManager.createNativeQuery(
                    "SELECT lr.classId, lr.room, lr.capacity,"
                    + "CASE "
                    + "  WHEN COUNT(ls.classId) = 0 THEN '09:00-21:00' "
                    + "  ELSE GROUP_CONCAT(DISTINCT "
                    + "    CASE "
                    + "      WHEN ls.start_time > '09:00:00' "
                    + "      THEN CONCAT('09:00-', SUBSTRING(ls.start_time, 1, 5)) "
                    + "      WHEN ls.end_time < '21:00:00' "
                    + "      THEN CONCAT(SUBSTRING(ls.end_time, 1, 5), '-21:00') "
                    + "      ELSE NULL "
                    + "    END "
                    + "    SEPARATOR ', ') "
                    + "END AS available_time "
                    + "FROM lecture_room lr "
                    + "LEFT JOIN lecture_schedule ls ON lr.classId = ls.classId AND ls.weekday = ? "
                    + "WHERE lr.building = ? "
                    + "  AND (ls.classId IS NULL OR ? < ls.start_time OR ? >= ls.end_time) "
                    + "GROUP BY lr.classId, lr.building, lr.room, lr.capacity")
                    .setParameter(1, searchRequest.getWeekday())
                    .setParameter(2, searchRequest.getBuilding())
                    .setParameter(3, searchRequest.getStartTime())
                    .setParameter(4, searchRequest.getStartTime())
                    .getResultList();

            List<RoomSearchResultDTO> availableRoomsData = roomResults.stream()
                    .map(row -> RoomSearchResultDTO.builder()
                    .classId((String) row[0])
                    .room((String) row[1])
                    .capacity(((Integer) row[2]).toString())
                    .availableTime((String) row[3])
                    .build())
                    .collect(Collectors.toList());

            LectureRoomDTO response = LectureRoomDTO.builder()
                    .availableRoomsData(availableRoomsData)
                    .count(availableRoomsData.size())
                    .build();

            logger.info("강의실 검색 성공: {}개 강의실 발견", availableRoomsData.size());
            return APIResponseDTO.success("강의실 검색 완료", response);

        } catch (Exception e) {
            logger.error("강의실 검색 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("강의실 검색 중 오류가 발생했습니다.");
        }
    }

    public APIResponseDTO<LectureRoomDTO> select(LectureRoomDTO selectRequest) {
        logger.info("강의실 조회 시도: 건물={}, 강의실={}",
                selectRequest.getBuilding(), selectRequest.getClassId());

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> result = entityManager.createNativeQuery(
                    "SELECT lr.classId, lr.building, lr.room, lr.floor, lr.capacity, lr.current_occupancy, "
                    + "lr.cnt_alone_study, lr.cnt_group_meeting, lr.cnt_quiet, lr.cnt_free_talk, "
                    + "lr.cnt_short_stay, lr.cnt_comfortable, "
                    + "ls.start_time, ls.end_time "
                    + "FROM lecture_room lr "
                    + "LEFT JOIN lecture_schedule ls ON lr.classId = ls.classId AND ls.weekday = ? "
                    + "WHERE lr.building = ? AND lr.classId = ?")
                    .setParameter(1, selectRequest.getWeekday())
                    .setParameter(2, selectRequest.getBuilding())
                    .setParameter(3, selectRequest.getClassId())
                    .getResultList();

            if (result.isEmpty()) {
                logger.warn("강의실을 찾을 수 없음: 건물={}, 강의실={}",
                        selectRequest.getBuilding(), selectRequest.getClassId());
                return APIResponseDTO.error("해당 강의실을 찾을 수 없습니다.");
            }
            // 매핑
            Object[] row = result.get(0);
            // 해시태그 추출
            List<String> top3Hashtags = getTop3Hashtags(
                    (Integer) row[6], // cnt_alone_study
                    (Integer) row[7], // cnt_group_meeting
                    (Integer) row[8], // cnt_quiet
                    (Integer) row[9], // cnt_free_talk
                    (Integer) row[10], // cnt_short_stay
                    (Integer) row[11] // cnt_comfortable
            );

            LectureRoomDTO response = LectureRoomDTO.builder()
                    .classId((String) row[0])
                    .building((String) row[1])
                    .room((String) row[2])
                    .floor((Integer) row[3])
                    .capacity((Integer) row[4])
                    .currentOccupancy((Integer) row[5])
                    .scheduleDetails(getScheduleDetails((String) row[0], selectRequest.getWeekday()))
                    .top3Hashtags(top3Hashtags)
                    .build();

            logger.info("강의실 조회 성공: {}", selectRequest.getClassId());
            return APIResponseDTO.success("강의실 조회 완료", response);

        } catch (Exception e) {
            logger.error("강의실 조회 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("강의실 조회 중 오류가 발생했습니다.");
        }
    }

    // 해시태그 top 3 추출 함수
    private List<String> getTop3Hashtags(Integer aloneStudy, Integer groupMeeting, Integer quiet, Integer freeTalk, Integer shortStay, Integer comfortable) {

        Map<String, Integer> hashtagMap = Map.of(
                "cnt_alone_study", aloneStudy != null ? aloneStudy : 0,
                "cnt_group_meeting", groupMeeting != null ? groupMeeting : 0,
                "cnt_quiet", quiet != null ? quiet : 0,
                "cnt_free_talk", freeTalk != null ? freeTalk : 0,
                "cnt_short_stay", shortStay != null ? shortStay : 0,
                "cnt_comfortable", comfortable != null ? comfortable : 0
        );

        List<String> result = hashtagMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // 리스트가 비면(db의 모든 해시가 0일때) 빈 리스트 반환
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    // 강의 시간표 가져오기용 메소드
    private List<ScheduleDetailDTO> getScheduleDetails(String classId, String weekday) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> schedules = entityManager.createNativeQuery(
                    "SELECT l.course_name, ls.start_time, ls.end_time "
                    + "FROM lecture_schedule ls "
                    + "JOIN lecture l ON ls.course_id = l.course_id "
                    + "WHERE ls.classId = ? AND ls.weekday = ? "
                    + "ORDER BY ls.start_time")
                    .setParameter(1, classId)
                    .setParameter(2, weekday)
                    .getResultList();

            return schedules.stream()
                    .map(row -> ScheduleDetailDTO.builder()
                    .courseName((String) row[0])
                    .startTime(((String) row[1]).substring(0, 5))
                    .endTime(((String) row[2]).substring(0, 5))
                    .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.warn("스케줄 조회 실패: classId={}, weekday={}", classId, weekday);
            return new ArrayList<>();  // 빈 리스트 반환
        }
    }
}
