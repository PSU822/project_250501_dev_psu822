package Web01.FindRoom.restful.api.Service;

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
        //native sql
        try {
            @SuppressWarnings("unchecked")
            List<String> availableRooms = entityManager.createNativeQuery(
                    "SELECT lr.classId FROM lecture_room lr "
                    + "WHERE lr.building = ? "
                    + "AND lr.classId NOT IN ("
                    + "SELECT DISTINCT ls.classId FROM lecture_schedule ls "
                    + "WHERE ls.weekday = ? "
                    + "AND ? >= ls.start_time "
                    + "AND ? < ls.end_time"
                    + ")")
                    .setParameter(1, searchRequest.getBuilding())
                    .setParameter(2, searchRequest.getWeekday())
                    .setParameter(3, searchRequest.getStartTime())
                    .setParameter(4, searchRequest.getStartTime())
                    .getResultList();

            LectureRoomDTO response = LectureRoomDTO.builder()
                    .availableRooms(availableRooms)
                    .count(availableRooms.size())
                    .build();

            logger.info("강의실 검색 성공: {}개 강의실 발견", availableRooms.size());
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
                    .setParameter(1, selectRequest.getWeekday()) // ← 파라미터로 받은 요일 사용
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
                    .startTime((String) row[12])
                    .endTime((String) row[13])
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
}
