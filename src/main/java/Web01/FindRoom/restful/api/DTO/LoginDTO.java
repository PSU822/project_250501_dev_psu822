package Web01.FindRoom.restful.api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import Web01.FindRoom.restful.api.Entity.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 응답 형식에 NULL이 있는 것이 프론트에서 보고 쓰기 어렵다는 판단으로 추가
public class LoginDTO {

    @NotBlank(message = "사용자 ID는 필수입력입니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수입력입니다.")
    private String password;

    @Pattern(regexp = "undergraduate|postgraduate|staff",
            message = "사용자 타입이 올바르지 않습니다.")
    private String user_type;

    private UserType userType;

}
