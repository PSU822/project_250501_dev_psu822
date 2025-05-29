package Web01.FindRoom.restful.api.Service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.LoginDTO;
import Web01.FindRoom.restful.api.DTO.RegisterDTO;
import Web01.FindRoom.restful.api.Entity.User;
import Web01.FindRoom.restful.api.Entity.UserType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

@Service
@Transactional
public class AuthService {

    @Autowired
    private EntityManager entityManager;
    // 로깅용 변수
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public APIResponseDTO<Void> login(LoginDTO loginRequest) {
        logger.info("로그인 시도: {}", loginRequest.getUserId());
        try {
            // 사용자 조회
            User user = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.userId = :userId AND u.userType = :userType", User.class)
                    .setParameter("userId", loginRequest.getUserId())
                    .setParameter("userType", UserType.valueOf(loginRequest.getUser_type()))
                    .getSingleResult();

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return APIResponseDTO.error("비밀번호가 일치하지 않습니다.");
            }
            logger.info("로그인 성공: {}", loginRequest.getUserId());
            return APIResponseDTO.success("로그인 성공.");

        } catch (NoResultException e) {
            logger.warn("사용자 찾을 수 없음: {}", loginRequest.getUserId());
            return APIResponseDTO.error("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            logger.error("로그인 중 오류 발생: {}", e.getMessage());
            return APIResponseDTO.error("서버 오류가 발생했습니다.");
        }
    }

    public APIResponseDTO<Void> register(RegisterDTO registerRequest) {
        try {
            // 중복 검사
            Long count = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.userId = :userId", Long.class)
                    .setParameter("userId", registerRequest.getUserId())
                    .getSingleResult();

            if (count > 0) {
                logger.warn("사용자 중복: {}", registerRequest.getUserId());
                return APIResponseDTO.error("이미 존재하는 ID입니다.");
            }

            String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

            // 사용자 생성
            User newUser = new User();
            newUser.setUserId(registerRequest.getUserId());
            newUser.setName(registerRequest.getName());
            newUser.setPassword(hashedPassword);
            newUser.setUserType(UserType.valueOf(registerRequest.getUser_type()));
            newUser.setCreatedAt(LocalDateTime.now());

            entityManager.persist(newUser);

            return APIResponseDTO.success("회원가입이 완료되었습니다.");

        } catch (Exception e) {
            logger.error("회원가입 중 오류 발생: {}", e.getMessage());
            return APIResponseDTO.error("서버 오류가 발생했습니다.");
        }
    }
}
