package Web01.FindRoom.restful.api.Controller;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.FavDTO;
import Web01.FindRoom.restful.api.Service.FavService;
import Web01.FindRoom.restful.api.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavController {

    @Autowired
    private FavService favService;

    @PostMapping("/add")
    public ResponseEntity<APIResponseDTO<Void>> addFavorite(@RequestBody FavDTO dto, HttpServletRequest request) {
        String userId = CookieUtil.getUserIdFromCookie(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(APIResponseDTO.error("로그인이 필요합니다."));
        }
        APIResponseDTO<Void> result = favService.addFavorite(userId, dto);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            int statusCode = getStatusCode(result.getMessage());
            return ResponseEntity.status(statusCode).body(result);
        }
    }

    @DeleteMapping("/del")
    public ResponseEntity<APIResponseDTO<Void>> removeFavorite(@RequestBody FavDTO dto, HttpServletRequest request) {
        String userId = CookieUtil.getUserIdFromCookie(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(APIResponseDTO.error("로그인이 필요합니다."));
        }
        APIResponseDTO<Void> result = favService.removeFavorite(userId, dto);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            int statusCode = getStatusCode(result.getMessage());
            return ResponseEntity.status(statusCode).body(result);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<APIResponseDTO<List<FavDTO>>> getFavorites(HttpServletRequest request) {
        String userId = CookieUtil.getUserIdFromCookie(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(APIResponseDTO.error("로그인이 필요합니다."));
        }
        APIResponseDTO<List<FavDTO>> result = favService.getFavorites(userId);
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
        } else if (message.contains("최대 인원") || message.contains("초과")) {
            return 409;
        } else {
            return 500;
        }
    }
}
