package Web01.FindRoom.restful.api.DTO;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryDTO {

    @NotBlank(message = "클래스 ID가 비어있습니다.")
    private String classId;

    @NotNull(message = "이용 인원 수가 비어있습니다.")
    private Integer participantCount;

    private Set<String> hashtags;

    @NotBlank(message = "종료 시간이 비어있습니다. 디폴트 값 2시간 설정 확인 바랍니다.")
    private String endTime;

    @NotBlank(message = "요일 정보가 빠져있습니다.")
    private String weekday;

    @NotNull(message = "이벤트 타입이 비어있습니다.")
    private EventType eventType;

    public enum EventType {
        usage_start,
        usage_end,
        favorite_add,
        favorite_remove
    }
}
