package Web01.FindRoom.restful.api.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.HistoryDTO;
import Web01.FindRoom.restful.api.Service.HistoryService;
import Web01.FindRoom.restful.api.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @PutMapping("/add")
    public ResponseEntity<APIResponseDTO<Void>> historyAdd(@Valid @RequestBody HistoryDTO req, HttpServletRequest cookie_req) {

        String userId = CookieUtil.getUserIdFromCookie(cookie_req);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(APIResponseDTO.error("로그인이 필요합니다."));
        }

        APIResponseDTO<Void> result = historyService.AddHistory(userId, req);

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
