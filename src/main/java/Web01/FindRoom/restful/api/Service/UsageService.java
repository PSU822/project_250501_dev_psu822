package Web01.FindRoom.restful.api.Service;

import java.util.ArrayList;
import java.util.List;

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

            String building = getBuildingByClassId(usageData.getClassId());
            if (building == null) {
                return APIResponseDTO.error("강의실을 찾을 수 없습니다.");
            }
            usageData.setBuilding(building);

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
            UsageDTO usageData = getLatestHistoryData(userId);
            if (usageData == null) {
                return APIResponseDTO.error("사용 기록을 찾을 수 없습니다.");
            }

            String building = getBuildingByClassId(usageData.getClassId());
            if (building == null) {
                return APIResponseDTO.error("강의실을 찾을 수 없습니다.");
            }
            usageData.setBuilding(building);

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
                    "SELECT classId, participant_count, "
                    + "cnt_alone_study, cnt_group_meeting, cnt_quiet, "
                    + "cnt_free_talk, cnt_short_stay, cnt_comfortable "
                    + "FROM history WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")
                    .setParameter(1, userId)
                    .getResultList();

            if (historyResult.isEmpty()) {
                return null;
            }

            Object[] history = historyResult.get(0);

            List<String> selectedHashtags = getSelectedHashtags(
                    (Integer) history[2], // cnt_alone_study
                    (Integer) history[3], // cnt_group_meeting
                    (Integer) history[4], // cnt_quiet
                    (Integer) history[5], // cnt_free_talk
                    (Integer) history[6], // cnt_short_stay
                    (Integer) history[7] // cnt_comfortable
            );

            return UsageDTO.builder()
                    .classId((String) history[0])
                    .participantCount((Integer) history[1])
                    .hashtags(String.join(",", selectedHashtags))
                    .build();

        } catch (Exception e) {
            logger.error("히스토리 조회 중 오류: {}", e.getMessage());
            return null;
        }
    }

    private List<String> getSelectedHashtags(Integer aloneStudy, Integer groupMeeting,
            Integer quiet, Integer freeTalk,
            Integer shortStay, Integer comfortable) {
        List<String> selected = new ArrayList<>();

        if (aloneStudy != null && aloneStudy == 1) {
            selected.add("cnt_alone_study");
        }
        if (groupMeeting != null && groupMeeting == 1) {
            selected.add("cnt_group_meeting");
        }
        if (quiet != null && quiet == 1) {
            selected.add("cnt_quiet");
        }
        if (freeTalk != null && freeTalk == 1) {
            selected.add("cnt_free_talk");
        }
        if (shortStay != null && shortStay == 1) {
            selected.add("cnt_short_stay");
        }
        if (comfortable != null && comfortable == 1) {
            selected.add("cnt_comfortable");
        }

        return selected;
    }

    private String getBuildingByClassId(String classId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> roomResult = entityManager.createNativeQuery(
                    "SELECT building FROM lecture_room WHERE classId = ?")
                    .setParameter(1, classId)
                    .getResultList();

            if (roomResult.isEmpty()) {
                return null;
            }

            return (String) roomResult.get(0)[0];

        } catch (Exception e) {
            logger.error("건물 조회 중 오류: {}", e.getMessage());
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

        String operation = isStart ? " + 1 " : " - 1 ";
        String[] selectedColumns = hashtags.split(",");

        for (String column : selectedColumns) {
            column = column.trim();
            entityManager.createNativeQuery(
                    "UPDATE lecture_room SET " + column + " = " + column + operation
                    + " WHERE classId = ?")
                    .setParameter(1, classId)
                    .executeUpdate();
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
            Integer capacity = (Integer) row[0];
            Integer currentOccupancy = (Integer) row[1];

            return (currentOccupancy + participantCount) <= capacity;

        } catch (Exception e) {
            logger.error("사용 가능 여부 체크 중 오류: {}", e.getMessage());
            return false;
        }
    }

    private boolean canEndUsing(String classId, Integer participantCount) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> result = entityManager.createNativeQuery(
                    "SELECT current_occupancy FROM lecture_room WHERE classId = ?")
                    .setParameter(1, classId)
                    .getResultList();

            if (result.isEmpty()) {
                return false;
            }

            Integer currentOccupancy = (Integer) result.get(0)[0];

            return currentOccupancy >= participantCount;

        } catch (Exception e) {
            logger.error("사용 종료 가능 여부 체크 중 오류: {}", e.getMessage());
            return false;
        }
    }
}
