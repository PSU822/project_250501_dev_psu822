package Web01.FindRoom.restful.api.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;

import Web01.FindRoom.restful.api.Entity.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterDTO {

    @NotBlank(message = "사용자 ID는 필수입력입니다.")
    private String userId;
    @NotBlank(message = "이름 항목은 필수입력입니다.")
    private String name;
    @NotBlank(message = "비밀번호는 필수입력입니다.")
    private String password;
    @Pattern(regexp = "undergraduate|postgraduate|staff",
            message = "사용자 타입이 올바르지 않습니다.")
    private String user_type;
    private UserType userType;

    public static RegisterDTO forRegister(String name, String userId, String password, String user_type) {
        return RegisterDTO.builder()
                .name(name)
                .userId(userId)
                .password(password)
                .user_type(user_type)
                .userType(UserType.valueOf(user_type))
                .build();
    }
}
