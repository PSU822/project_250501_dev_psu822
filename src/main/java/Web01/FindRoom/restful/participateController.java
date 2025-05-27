package Web01.FindRoom.restful;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class participateController {
    @GetMapping("/participate")
    public String participatePage(){
        return "pages/participate";
    }

}

