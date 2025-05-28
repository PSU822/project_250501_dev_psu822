package Web01.FindRoom.restful.api.DTO;

import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyPageUserInfoDTO {

    private String userId;
    private String name;
    private String userType;

    private List<FavDTO> favorites;
}
