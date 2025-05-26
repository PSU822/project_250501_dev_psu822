package Web01.FindRoom.restful.api.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Web01.FindRoom.restful.api.entity.User;
import Web01.FindRoom.restful.api.entity.UserType;
import Web01.FindRoom.restful.api.util.CookieUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
@Transactional
public class AuthController {

    @Autowired
    private EntityManager entityManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request,
            HttpServletResponse response) {

        String userId = request.get("userId");
        String password = request.get("password");
        String userTypeStr = request.getOrDefault("user_type", "undergraduate");

        // 입력값 검증
        if (userId == null || password == null || userId.trim().isEmpty() || password.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "아이디 또는 비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // 사용자 조회
            User user = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.userId = :userId AND u.userType = :userType", User.class)
                    .setParameter("userId", userId)
                    .setParameter("userType", UserType.valueOf(userTypeStr))
                    .getSingleResult();

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, user.getPassword())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "비밀번호가 일치하지 않습니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // 쿠키 설정
            CookieUtil.setLoginCookie(response, user.getUserId());

            // 성공 응답
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "로그인 성공.");

            System.out.println("사용자 로그인 성공: " + userId);
            return ResponseEntity.ok(successResponse);

        } catch (NoResultException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자를 찾을 수 없습니다.");
            return ResponseEntity.status(404).body(errorResponse);

        } catch (Exception e) {
            System.err.println("로그인 처리중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String userId = request.get("userId");
        String password = request.get("password");
        String userTypeStr = request.getOrDefault("user_type", "undergraduate");

        // 입력값 검증
        if (userId == null || password == null || name == null
                || userId.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "아이디, 패스워드, 이름은 필수 항목입니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // 중복 검사
            Long count = entityManager.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.userId = :userId", Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            if (count > 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "이미 존재하는 ID 입니다.");
                return ResponseEntity.status(409).body(errorResponse);
            }

            // 비밀번호 암호화, 해시 사용
            String hashedPassword = passwordEncoder.encode(password);

            // 사용자 생성
            User newUser = new User();
            newUser.setUserId(userId);
            newUser.setName(name);
            newUser.setPassword(hashedPassword);
            newUser.setUserType(UserType.valueOf(userTypeStr));
            newUser.setCreatedAt(LocalDateTime.now());

            entityManager.persist(newUser);

            // 성공 응답
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", newUser.getUserId());
            userInfo.put("name", newUser.getName());
            userInfo.put("user_type", newUser.getUserType());

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "회원가입이 완료되었습니다.");
            successResponse.put("user", userInfo);

            return ResponseEntity.status(201).body(successResponse);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "잘못된 사용자 타입입니다.");
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            System.err.println("회원가입 중 오류: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        // 쿠키 유효기간 0
        CookieUtil.clearLoginCookie(response);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "성공적으로 로그아웃 되었습니다.");

        System.out.println("사용자 로그아웃 성공");
        return ResponseEntity.ok(successResponse);
    }

    // 현재 로그인한 사용자 정보 가져오기. 테스트용 api
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String userId = CookieUtil.getUserIdFromCookie(request);

        if (userId == null || userId.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(errorResponse);
        }

        try {
            User user = entityManager.createQuery(
                    "SELECT u FROM User u WHERE u.userId = :userId", User.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("name", user.getName());
            userInfo.put("user_type", user.getUserType());

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("user", userInfo);

            return ResponseEntity.ok(successResponse);

        } catch (NoResultException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자를 찾을 수 없습니다.");
            return ResponseEntity.status(404).body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
