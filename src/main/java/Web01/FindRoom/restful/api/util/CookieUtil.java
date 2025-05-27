package Web01.FindRoom.restful.api.Util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    // 쿠키 로그인 처리
    public static void setLoginCookie(HttpServletResponse response, String userId) {
        Cookie cookie = new Cookie("userId", userId);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60); // 1시간
        cookie.setPath("/");
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    // 쿠키 로그아웃 처리
    public static void clearLoginCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("userId", "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    // 쿠키에서 userId 가져오기 
    public static String getUserIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("userId".equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.trim().isEmpty()) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
}
