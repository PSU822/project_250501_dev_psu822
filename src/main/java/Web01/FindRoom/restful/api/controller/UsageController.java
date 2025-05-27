package Web01.FindRoom.restful.api.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.Service.UsageService;
import Web01.FindRoom.restful.api.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    @Autowired
    private UsageService usageService;

    @PutMapping("/start")
    public ResponseEntity<APIResponseDTO<Void>> start(HttpServletRequest request) {

        // 쿠키에서 userId 추출
        String userId = CookieUtil.getUserIdFromCookie(request);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(APIResponseDTO.error("로그인이 필요합니다."));
        }

        APIResponseDTO<Void> result = usageService.start(userId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            int statusCode = getStatusCode(result.getMessage());
            return ResponseEntity.status(statusCode).body(result);
        }
    }

    @PutMapping("/end")
    public ResponseEntity<APIResponseDTO<Void>> end(HttpServletRequest request) {

        // 쿠키에서 userId 추출
        String userId = CookieUtil.getUserIdFromCookie(request);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(APIResponseDTO.error("로그인이 필요합니다."));
        }

        APIResponseDTO<Void> result = usageService.end(userId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            int statusCode = getStatusCode(result.getMessage());
            return ResponseEntity.status(statusCode).body(result);
        }
    }

    // HTTP 코드 보조 함수
    private int getStatusCode(String message) {
        if (message.contains("찾을 수 없습니다")) {
            return 404;
        } else if (message.contains("최대 인원") || message.contains("초과")) {
            return 409;
        } else {
            return 500;
        }
    }
}
