package Web01.FindRoom.restful.api.Service;

import java.util.ArrayList;
import java.util.List;

import Web01.FindRoom.restful.api.DTO.FavDTO;
import Web01.FindRoom.restful.api.DTO.MyPageUserInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import jakarta.persistence.EntityManager;

@Service
@Transactional
public class MyPageUserInfoService {

    private static final Logger logger = LoggerFactory.getLogger(MyPageUserInfoService.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FavService favService;

    public APIResponseDTO<MyPageUserInfoDTO> getMyPageUserInfo(String userId) {
        logger.info("마이페이지 정보 조회: {}", userId);

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> userResult = entityManager.createNativeQuery(
                    "SELECT name, user_type FROM user WHERE user_id = ?")
                    .setParameter(1, userId)
                    .getResultList();

            if (userResult.isEmpty()) {
                return APIResponseDTO.error("사용자를 찾을 수 없습니다.");
            }

            Object[] userInfo = userResult.get(0);
            String name = (String) userInfo[0];
            String userType = (String) userInfo[1];

            APIResponseDTO<List<FavDTO>> favoritesResponse = favService.getFavorites(userId);
            List<FavDTO> favorites = favoritesResponse.isSuccess()
                    ? favoritesResponse.getData() : new ArrayList<>();

            MyPageUserInfoDTO dto = MyPageUserInfoDTO.builder()
                    .userId(userId)
                    .name(name)
                    .userType(userType)
                    .favorites(favorites)
                    .build();

            return APIResponseDTO.success("마이페이지 정보 조회 성공", dto);

        } catch (Exception e) {
            logger.error("마이페이지 정보 조회 중 오류: {}", e.getMessage());
            return APIResponseDTO.error("마이페이지 정보 조회 중 오류가 발생했습니다.");
        }
    }
}
