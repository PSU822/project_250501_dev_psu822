package Web01.FindRoom.restful.api.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.LoginDTO;
import Web01.FindRoom.restful.api.DTO.RegisterDTO;
import Web01.FindRoom.restful.api.Service.AuthService;
import Web01.FindRoom.restful.api.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Transactional
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<APIResponseDTO<Void>> login(
            @Valid @RequestBody LoginDTO loginRequest,
            HttpServletResponse response) {

        APIResponseDTO<Void> result = authService.login(loginRequest);

        if (result.isSuccess()) {
            CookieUtil.setLoginCookie(response, loginRequest.getUserId());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponseDTO<Void>> register(
            @Valid @RequestBody RegisterDTO registerRequest) {

        APIResponseDTO<Void> result = authService.register(registerRequest);

        if (result.isSuccess()) {
            return ResponseEntity.status(201).body(result);
        } else {
            return ResponseEntity.status(409).body(result);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponseDTO<Void>> logout(HttpServletResponse response) {
        CookieUtil.clearLoginCookie(response);
        return ResponseEntity.ok(APIResponseDTO.success("로그아웃 완료."));
    }

    @GetMapping("/me")
    public ResponseEntity<APIResponseDTO<Map<String, Object>>> getLoginStatus(HttpServletRequest request) {
        String userId = CookieUtil.getUserIdFromCookie(request);

        Map<String, Object> userData = new HashMap<>();
        userData.put("loggedIn", userId != null);
        userData.put("userId", userId);

        if (userId != null) {
            return ResponseEntity.ok(APIResponseDTO.success("로그인 상태 확인 완료", userData));
        } else {
            return ResponseEntity.ok(APIResponseDTO.success("로그인되지 않음", userData));
        }
    }

}
