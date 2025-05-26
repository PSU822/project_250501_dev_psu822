package Web01.FindRoom.restful.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;

@RestController
@RequestMapping("/api/lectureroom")
@Transactional
public class LectureRoomController {

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchAvailableClassrooms(
            @RequestParam String building,
            @RequestParam String weekday,
            @RequestParam String time) {

        if (building == null || weekday == null || time == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "필수 파라미터가 누락되었습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // 테스트 환경 DB 문제로 Native SQL로 빈 강의실 조회
            @SuppressWarnings("unchecked")
            List<String> availableRooms = entityManager.createNativeQuery(
                    "SELECT lr.classId FROM lecture_room lr " // ← 테이블명 사용
                    + "WHERE lr.building = ? "
                    + "AND lr.classId NOT IN ("
                    + "SELECT DISTINCT ls.classId FROM lecture_schedule ls " // ← 테이블명 사용
                    + "WHERE ls.weekday = ? "
                    + "AND ? >= ls.start_time "
                    + "AND ? < ls.end_time"
                    + ")")
                    .setParameter(1, building)
                    .setParameter(2, weekday)
                    .setParameter(3, time)
                    .setParameter(4, time)
                    .getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available_rooms", availableRooms);
            response.put("count", availableRooms.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("강의실 검색 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "강의실 검색 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/select")
    public ResponseEntity<Map<String, Object>> selectClassroom(
            @RequestParam String building,
            @RequestParam String classId) {

        if (building == null || classId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "필수 파라미터가 누락되었습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Native SQL
            @SuppressWarnings("unchecked")
            List<Object[]> result = entityManager.createNativeQuery(
                    "SELECT classId, building, room, floor, capacity, current_occupancy, "
                    + "cnt_alone_study, cnt_group_meeting, cnt_quiet, cnt_free_talk, "
                    + "cnt_short_stay, cnt_comfortable "
                    + "FROM lecture_room WHERE building = ? AND classId = ?")
                    .setParameter(1, building)
                    .setParameter(2, classId)
                    .getResultList();

            if (result.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "해당 강의실을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(errorResponse);
            }

            // 결과 매핑
            Object[] row = result.get(0);
            Map<String, Object> classroomData = new HashMap<>();
            classroomData.put("classId", row[0]);
            classroomData.put("building", row[1]);
            classroomData.put("room", row[2]);
            classroomData.put("floor", row[3]);
            classroomData.put("capacity", row[4]);
            classroomData.put("current_occupancy", row[5]);
            classroomData.put("cnt_alone_study", row[6]);
            classroomData.put("cnt_group_meeting", row[7]);
            classroomData.put("cnt_quiet", row[8]);
            classroomData.put("cnt_free_talk", row[9]);
            classroomData.put("cnt_short_stay", row[10]);
            classroomData.put("cnt_comfortable", row[11]);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("classroom", classroomData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("강의실 조회 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "강의실 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
