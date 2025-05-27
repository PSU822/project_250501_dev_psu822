package Web01.FindRoom.restful;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class infoController {
    @GetMapping("/info")
    public String infoPage(){
        return "pages/info";
    }
}
