package Web01.FindRoom.restful.api.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.HistoryDTO;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class HistoryService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);

    @Autowired
    private EntityManager entityManager;

    public APIResponseDTO<Void> AddHistory(String userId, HistoryDTO historyData) {
        logger.info("해당 사용자 히스토리 작성 : {}", userId);

        try {
            // 시간 타입 검증
            LocalDateTime endTime;
            try {
                endTime = LocalDateTime.parse(historyData.getEndTime());
            } catch (Exception e) {
                return APIResponseDTO.error("종료 시간 형식이 올바르지 않습니다.");
            }

            String hashtagsStr = "";
            if (historyData.getHashtags() != null && !historyData.getHashtags().isEmpty()) {
                hashtagsStr = String.join(",", historyData.getHashtags());
            }

            entityManager.createNativeQuery(
                    "INSERT INTO history (user_id, classId, weekday, start_time, end_time, "
                    + "participant_count, hashtags, event_type, created_at) "
                    + "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, NOW())")
                    .setParameter(1, userId)
                    .setParameter(2, historyData.getClassId())
                    .setParameter(3, historyData.getWeekday())
                    .setParameter(4, Timestamp.valueOf(endTime))
                    .setParameter(5, historyData.getParticipantCount())
                    .setParameter(6, hashtagsStr)
                    .setParameter(7, historyData.getEventType().name())
                    .executeUpdate();

            logger.info("히스토리 작성 완료: {} - {}", historyData.getEventType(), historyData.getClassId());
            return APIResponseDTO.success("히스토리가 기록되었습니다.");

        } catch (Exception e) {
            logger.error("히스토리 작성 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("히스토리 작성 중 오류가 발생했습니다.");
        }
    }
}
