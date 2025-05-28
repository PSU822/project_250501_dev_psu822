package Web01.FindRoom.restful.api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 응답 형식에 NULL이 있는 것이 프론트에서 보고 쓰기 어렵다는 판단으로 추가
public class APIResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> APIResponseDTO<T> success(String message, T data) {
        return APIResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static APIResponseDTO<Void> success(String message) {
        return APIResponseDTO.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> APIResponseDTO<T> error(String message) {
        return APIResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
