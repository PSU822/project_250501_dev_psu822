package Web01.FindRoom.restful.api.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.UsageDTO;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class UsageService {

    private static final Logger logger = LoggerFactory.getLogger(UsageService.class);

    @Autowired
    private EntityManager entityManager;

    public APIResponseDTO<Void> start(String userId) {
        logger.info("강의실 사용 시작: {}", userId);

        try {
            UsageDTO usageData = getLatestHistoryData(userId);
            if (usageData == null) {
                return APIResponseDTO.error("사용 기록을 찾을 수 없습니다.");
            }

            if (!updateRoomUsage(usageData, true)) {
                return APIResponseDTO.error("강의실 최대 인원을 초과했습니다.");
            }

            logger.info("강의실 사용 시작 완료: {}-{}", usageData.getBuilding(), usageData.getClassId());
            return APIResponseDTO.success("강의실 사용이 시작되었습니다.");

        } catch (Exception e) {
            logger.error("강의실 사용 시작 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("강의실 사용 시작 중 오류가 발생했습니다.");
        }
    }

    public APIResponseDTO<Void> end(String userId) {
        logger.info("강의실 사용 종료: {}", userId);

        try {
            UsageDTO usageData = getLatestHistoryDataForEnd(userId);
            if (usageData == null) {
                return APIResponseDTO.error("사용 기록을 찾을 수 없습니다.");
            }

            if (!updateRoomUsage(usageData, false)) {
                return APIResponseDTO.error("이미 강의실 인원이 0입니다.");
            }

            logger.info("강의실 사용 종료 완료: {}-{}", usageData.getBuilding(), usageData.getClassId());
            return APIResponseDTO.success("강의실 사용이 종료되었습니다.");

        } catch (Exception e) {
            logger.error("강의실 사용 종료 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("강의실 사용 종료 중 오류가 발생했습니다.");
        }
    }

    // api 보조 함수들
    private UsageDTO getLatestHistoryData(String userId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> historyResult = entityManager.createNativeQuery(
                    "SELECT classId, participant_count, hashtags "
                    + "FROM history WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")
                    .setParameter(1, userId)
                    .getResultList();

            if (historyResult.isEmpty()) {
                return null;
            }

            Object[] history = historyResult.get(0);

            UsageDTO historyData = UsageDTO.builder()
                    .classId((String) history[0])
                    .participantCount((Integer) history[1])
                    .hashtags((String) history[2])
                    .build();
            return historyData;

        } catch (Exception e) {
            logger.error("히스토리 조회 중 오류: {}", e.getMessage());
            return null;
        }
    }

    // 위의 메소드와 구조적으로 동일, earlyend 예외 상황 추가
    private UsageDTO getLatestHistoryDataForEnd(String userId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> historyResult = entityManager.createNativeQuery(
                    "SELECT history_id, classId, participant_count, hashtags, "
                    + "start_time, end_time "
                    + "FROM history WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")
                    .setParameter(1, userId)
                    .getResultList();

            if (historyResult.isEmpty()) {
                logger.info("사용자 {}의 히스토리 존재하지않음.", userId);
                return null;
            }

            Object[] history = historyResult.get(0);

            // early end 처리
            Long historyId = ((Number) history[0]).longValue();
            Timestamp originalEndTime = (Timestamp) history[5];
            LocalDateTime endTime = originalEndTime.toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();

            UsageDTO historyEndResult = UsageDTO.builder()
                    .classId((String) history[1])
                    .participantCount((Integer) history[2])
                    .hashtags((String) history[3])
                    .build();

            if (endTime.isAfter(now)) {
                logger.info("사용자 {}가 예정 시간보다 일찍 종료. end_time 업데이트: {} -> {}", userId, endTime, now);

                entityManager.createNativeQuery(
                        "UPDATE history SET end_time = ? WHERE history_id = ?")
                        .setParameter(1, Timestamp.valueOf(now))
                        .setParameter(2, historyId)
                        .executeUpdate();
            }

            return historyEndResult;

        } catch (Exception e) {
            logger.error("히스토리 조회 중 오류: {}", e.getMessage());
            return null;
        }
    }

    private boolean updateRoomUsage(UsageDTO usageData, boolean isStart) {
        try {
            if (isStart) {
                if (!canStartUsing(usageData.getClassId(), usageData.getParticipantCount())) {
                    return false;
                }
            } else {
                if (!canEndUsing(usageData.getClassId(), usageData.getParticipantCount())) {
                    return false;
                }
            }

            updateOccupancy(usageData.getClassId(), usageData.getParticipantCount(), isStart);

            updateHashtagCounts(usageData.getClassId(), usageData.getHashtags(), isStart);

            return true;

        } catch (Exception e) {
            logger.error("강의실 사용 정보 업데이트 중 오류: {}", e.getMessage());
            return false;
        }
    }

    private void updateOccupancy(String classId, Integer participantCount, boolean isStart) {
        String operation = isStart ? "+" : "-";

        entityManager.createNativeQuery(
                "UPDATE lecture_room SET current_occupancy = current_occupancy " + operation + " ? "
                + "WHERE classId = ?")
                .setParameter(1, participantCount)
                .setParameter(2, classId)
                .executeUpdate();
    }

    private void updateHashtagCounts(String classId, String hashtags, boolean isStart) {
        if (hashtags == null || hashtags.trim().isEmpty()) {
            return;
        }

        Map<String, String> hashtagToColumn = Map.of(
                "혼자 개인 공부해요", "cnt_alone_study",
                "여럿이서 회의해요", "cnt_group_meeting",
                "조용하게 있어요", "cnt_quiet",
                "자유롭게 대화해요", "cnt_free_talk",
                "아주 잠깐 머물러요", "cnt_short_stay",
                "편하게 있어요", "cnt_comfortable"
        );

        String operation = isStart ? " + 1 " : " - 1 ";

        String[] selectedHashtags = hashtags.split(",");

        for (String hashtag : selectedHashtags) {
            hashtag = hashtag.trim();
            String column = hashtagToColumn.get(hashtag);

            if (column != null) {
                entityManager.createNativeQuery(
                        "UPDATE lecture_room SET " + column + " = " + column + operation
                        + " WHERE classId = ?")
                        .setParameter(1, classId)
                        .executeUpdate();
            }
        }
    }

    private boolean canStartUsing(String classId, Integer participantCount) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> result = entityManager.createNativeQuery(
                    "SELECT capacity, current_occupancy FROM lecture_room WHERE classId = ?")
                    .setParameter(1, classId)
                    .getResultList();

            if (result.isEmpty()) {
                return false;
            }

            Object[] row = result.get(0);
            UsageDTO roomInfo = UsageDTO.builder()
                    .capacity((Integer) row[0])
                    .currentOccupancy((Integer) row[1])
                    .build();

            return (roomInfo.getCurrentOccupancy() + participantCount) <= roomInfo.getCapacity();

        } catch (Exception e) {
            logger.error("사용 가능 여부 체크 중 오류: {}", e.getMessage());
            return false;
        }
    }

    private boolean canEndUsing(String classId, Integer participantCount) {
        try {
            Integer currentOccupancy = (Integer) entityManager.createNativeQuery(
                    "SELECT current_occupancy FROM lecture_room WHERE classId = ?")
                    .setParameter(1, classId)
                    .getSingleResult();  // getResultList() → getSingleResult()

            return currentOccupancy >= participantCount;

        } catch (Exception e) {
            logger.error("사용 종료 가능 여부 체크 중 오류: {}", e.getMessage());
            return false;
        }
    }
}
