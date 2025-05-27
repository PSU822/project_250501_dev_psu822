package Web01.FindRoom.restful.api.Controller;

import Web01.FindRoom.restful.api.Service.LectureRoomService;

import Web01.FindRoom.restful.api.DTO.APIResponseDTO;
import Web01.FindRoom.restful.api.DTO.LectureRoomDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectureroom")
public class LectureRoomController {

    @Autowired
    private LectureRoomService lectureRoomService;

    @GetMapping("/search")
    public ResponseEntity<APIResponseDTO<LectureRoomDTO>> search(@RequestParam String building,
            @RequestParam String weekday,
            @RequestParam String time) {

        LectureRoomDTO searchRequest = LectureRoomDTO.forSearch(building, weekday, time, time);

        APIResponseDTO<LectureRoomDTO> result = lectureRoomService.search(searchRequest);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(400).body(result);
        }
    }

    @GetMapping("/select")
    public ResponseEntity<APIResponseDTO<LectureRoomDTO>> select(@RequestParam String building,
            @RequestParam String classId) {

        LectureRoomDTO selectRequest = LectureRoomDTO.forSelect(building, classId);
        APIResponseDTO<LectureRoomDTO> result = lectureRoomService.select(selectRequest);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(404).body(result);
        }
    }
}
