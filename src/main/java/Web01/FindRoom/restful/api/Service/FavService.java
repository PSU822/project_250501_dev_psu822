package Web01.FindRoom.restful.api.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.FavDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class FavService {

    private static final Logger logger = LoggerFactory.getLogger(FavService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public APIResponseDTO<Void> addFavorite(String userId, FavDTO dto) {
        logger.info("즐겨찾기 추가: {}", userId);

        try {
            Long count = ((Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM user_favorite WHERE user_id = ? AND classId = ? AND weekday = ? AND start_time = ? AND end_time = ?")
                    .setParameter(1, userId)
                    .setParameter(2, dto.getClassId())
                    .setParameter(3, dto.getWeekday())
                    .setParameter(4, LocalTime.parse(dto.getStartTime()))
                    .setParameter(5, LocalTime.parse(dto.getEndTime()))
                    .getSingleResult()).longValue();

            if (count > 0) {
                return APIResponseDTO.error("이미 동일한 즐겨찾기가 존재합니다.");
            }

            entityManager.createNativeQuery(
                    "INSERT INTO user_favorite (user_id, classId, weekday, start_time, end_time, participant_count) "
                    + "VALUES (?, ?, ?, ?, ?, ?)")
                    .setParameter(1, userId)
                    .setParameter(2, dto.getClassId())
                    .setParameter(3, dto.getWeekday())
                    .setParameter(4, LocalTime.parse(dto.getStartTime()))
                    .setParameter(5, LocalTime.parse(dto.getEndTime()))
                    .setParameter(6, dto.getParticipantCount())
                    .executeUpdate();

            return APIResponseDTO.success("즐겨찾기가 추가되었습니다.");
        } catch (Exception e) {
            logger.error("즐겨찾기 추가 중 오류 발생: {}", e.getMessage());
            return APIResponseDTO.error("즐겨찾기 추가 중 문제가 발생했습니다.");
        }
    }

    public APIResponseDTO<Void> removeFavorite(String userId, FavDTO dto) {
        logger.info("즐겨찾기 제거: {}", userId);

        try {
            int deleted = entityManager.createNativeQuery(
                    "DELETE FROM user_favorite WHERE user_id = ? AND classId = ? AND weekday = ? AND start_time = ? AND end_time = ?")
                    .setParameter(1, userId)
                    .setParameter(2, dto.getClassId())
                    .setParameter(3, dto.getWeekday())
                    .setParameter(4, LocalTime.parse(dto.getStartTime()))
                    .setParameter(5, LocalTime.parse(dto.getEndTime()))
                    .executeUpdate();

            if (deleted > 0) {
                return APIResponseDTO.success("즐겨찾기가 삭제되었습니다.");
            } else {
                return APIResponseDTO.error("해당 즐겨찾기를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return APIResponseDTO.error("즐겨찾기 삭제 중 오류: " + e.getMessage());
        }
    }

    public APIResponseDTO<List<FavDTO>> getFavorites(String userId) {
        logger.info("즐겨찾기 조회: {}", userId);

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT uf.classId, uf.weekday, uf.start_time, uf.end_time, uf.participant_count, "
                    + "lr.building, lr.room, lr.floor, uf.favorited_at "
                    + "FROM user_favorite uf "
                    + "JOIN lecture_room lr ON uf.classId = lr.classId "
                    + "WHERE uf.user_id = ? "
                    + "ORDER BY uf.favorited_at DESC")
                    .setParameter(1, userId)
                    .getResultList();

            List<FavDTO> dtoList = results.stream().map(row -> FavDTO.builder()
                    .classId((String) row[0])
                    .weekday((String) row[1])
                    .startTime(row[2].toString())
                    .endTime(row[3].toString())
                    .participantCount(((Number) row[4]).intValue())
                    .building((String) row[5])
                    .room((String) row[6])
                    .floor(((Number) row[7]).intValue())
                    .favoritedAt(row[8].toString())
                    .build()).collect(Collectors.toList());

            return APIResponseDTO.success("즐겨찾기 조회 성공", dtoList);

        } catch (Exception e) {
            logger.error("즐겨찾기 조회 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("즐겨찾기 조회 중 오류: " + e.getMessage());
        }
    }
}
