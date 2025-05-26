package Web01.FindRoom.restful.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Web01.FindRoom.restful.api.util.CookieUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/usage")
@Transactional
public class UsageController {

    @Autowired
    private EntityManager entityManager;

    @PutMapping("/start")
    public ResponseEntity<Map<String, Object>> startUsingRoom(HttpServletRequest request) {

        try {
            // 쿠키에서 userId 추출
            String userId = CookieUtil.getUserIdFromCookie(request);
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // 해당 유저의 최신 히스토리 조회 (올바른 컬럼명 사용)
            @SuppressWarnings("unchecked")
            List<Object[]> historyResult = entityManager.createNativeQuery(
                    "SELECT classId, participant_count, hashtags "
                    + "FROM history WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")
                    .setParameter(1, userId)
                    .getResultList();

            if (historyResult.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "사용 기록을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Object[] history = historyResult.get(0);
            String classId = (String) history[0];
            Integer participantCount = (Integer) history[1];
            String hashtags = (String) history[2]; // SET 타입은 문자열로 반환됨

            // building 정보도 필요하므로 lecture_room에서 조회
            @SuppressWarnings("unchecked")
            List<Object[]> roomResult = entityManager.createNativeQuery(
                    "SELECT building FROM lecture_room WHERE classId = ?")
                    .setParameter(1, classId)
                    .getResultList();

            if (roomResult.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "강의실을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(errorResponse);
            }

            String building = (String) roomResult.get(0)[0];

            // 1. current_occupancy 증가
            entityManager.createNativeQuery(
                    "UPDATE lecture_room SET current_occupancy = current_occupancy + ? "
                    + "WHERE classId = ?")
                    .setParameter(1, participantCount)
                    .setParameter(2, classId)
                    .executeUpdate();

            // 2. 해시태그별 카운트 증가
            updateHashtagCountsFromSet(classId, hashtags, true); // true = 증가

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "강의실 사용이 시작되었습니다.");
            response.put("classId", classId);
            response.put("building", building);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("강의실 사용 시작 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "강의실 사용 시작 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/end")
    public ResponseEntity<Map<String, Object>> endUsingRoom(HttpServletRequest request) {
        try {
            // 쿠키에서 userId 추출
            String userId = CookieUtil.getUserIdFromCookie(request);
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // 해당 유저의 최신 히스토리 조회 (올바른 컬럼명 사용)
            @SuppressWarnings("unchecked")
            List<Object[]> historyResult = entityManager.createNativeQuery(
                    "SELECT classId, participant_count, hashtags "
                    + "FROM history WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")
                    .setParameter(1, userId)
                    .getResultList();

            if (historyResult.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "사용 기록을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Object[] history = historyResult.get(0);
            String classId = (String) history[0];
            Integer participantCount = (Integer) history[1];
            String hashtags = (String) history[2]; // SET 타입은 문자열로 반환됨

            // building 정보도 필요하므로 lecture_room에서 조회
            @SuppressWarnings("unchecked")
            List<Object[]> roomResult = entityManager.createNativeQuery(
                    "SELECT building FROM lecture_room WHERE classId = ?")
                    .setParameter(1, classId)
                    .getResultList();

            if (roomResult.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "강의실을 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(errorResponse);
            }

            String building = (String) roomResult.get(0)[0];

            // current_occupancy 감소
            entityManager.createNativeQuery(
                    "UPDATE lecture_room SET current_occupancy = current_occupancy - ? "
                    + "WHERE classId = ?")
                    .setParameter(1, participantCount)
                    .setParameter(2, classId)
                    .executeUpdate();

            // 해시태그별 카운트 감소
            updateHashtagCountsFromSet(classId, hashtags, false); // false = 감소

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "강의실 사용이 종료되었습니다.");
            response.put("classId", classId);
            response.put("building", building);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("강의실 사용 시작 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "강의실 사용 시작 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // SET 타입 해시태그를 파싱
    private void updateHashtagCountsFromSet(String classId, String hashtags, boolean isIncrease) {
        if (hashtags == null || hashtags.trim().isEmpty()) {
            return;
        }

        String operation = isIncrease ? " + 1 " : " - 1 ";

        // SET 타입은 "혼자 개인 공부해요,조용하게 있어요" 형태로 반환됨
        String[] hashtagArray = hashtags.split(",");

        for (String hashtag : hashtagArray) {
            hashtag = hashtag.trim();
            String column = mapHashtagToColumn(hashtag);
            if (column != null) {
                entityManager.createNativeQuery(
                        "UPDATE lecture_room SET " + column + " = " + column + operation
                        + " WHERE classId = ?")
                        .setParameter(1, classId)
                        .executeUpdate();
            }
        }
    }

    private String mapHashtagToColumn(String hashtag) {
        return switch (hashtag) {
            case "혼자 개인 공부해요" ->
                "cnt_alone_study";
            case "여럿이서 회의해요" ->
                "cnt_group_meeting";
            case "조용하게 있어요" ->
                "cnt_quiet";
            case "자유롭게 대화해요" ->
                "cnt_free_talk";
            case "아주 잠깐 머물러요" ->
                "cnt_short_stay";
            case "편하게 있어요" ->
                "cnt_comfortable";
            default -> {
                System.err.println("알 수 없는 해시태그: " + hashtag);
                yield null;
            }
        };
    }
}
