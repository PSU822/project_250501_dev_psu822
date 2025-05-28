package Web01.FindRoom.restful.api.Controller;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.MyPageUserInfoDTO;
import Web01.FindRoom.restful.api.Service.MyPageUserInfoService;
import Web01.FindRoom.restful.api.Util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
public class MyPageUserInfoController {

    @Autowired
    private MyPageUserInfoService myPageUserInfoService;

    @PostMapping("/info")
    public ResponseEntity<APIResponseDTO<MyPageUserInfoDTO>> getMyPageUserInfo(HttpServletRequest request) {
        String userId = CookieUtil.getUserIdFromCookie(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(APIResponseDTO.error("로그인이 필요합니다."));
        }

        APIResponseDTO<MyPageUserInfoDTO> result = myPageUserInfoService.getMyPageUserInfo(userId);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            int statusCode = getStatusCode(result.getMessage());
            return ResponseEntity.status(statusCode).body(result);
        }
    }

    private int getStatusCode(String message) {
        if (message.contains("찾을 수 없습니다")) {
            return 404;
        } else {
            return 500;
        }
    }
}
