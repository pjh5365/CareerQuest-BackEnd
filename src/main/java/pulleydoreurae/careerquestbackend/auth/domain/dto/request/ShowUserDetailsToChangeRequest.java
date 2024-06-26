package pulleydoreurae.careerquestbackend.auth.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 정보 변경한 내용을 담아 변경을 요청하는 Request
 *
 * @author : hanjaeseong
 */
@Getter
@Setter
public class ShowUserDetailsToChangeRequest {

	@NotBlank(message = "사용자 아이디는 필수입니다.")    // null, "", " " 을 허용하지 않는다.
	@Size(min = 5, message = "아이디는 5자 이상으로 입력해주세요.")    // 사용자의 id 길이는 최소 5자
	private String userId;
	@Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식에 맞춰 입력해주세요.")
	private String phoneNum;
	private String smallCategory; // 소분류 - 소분류 -> 중분류 -> 대분류를 순차적으로 찾을 수 있어 소분류 이름을 가져옴
	private List<String> technologyStacks;

	@NotBlank(message = "회원정보 수정 시 비밀번호는 필수입니다.")
	private String password;
}
