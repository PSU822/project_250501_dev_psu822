package Web01.FindRoom.restful;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class mypageController {
    @GetMapping("/mypage")
    public String participatePage(){
        return "pages/mypage";
    }

}