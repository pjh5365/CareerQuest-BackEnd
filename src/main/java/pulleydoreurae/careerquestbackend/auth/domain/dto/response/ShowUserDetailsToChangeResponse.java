package pulleydoreurae.careerquestbackend.auth.domain.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ShowUserDetailsToChangeResponse {

	private String userId;
	private String phoneNum;
	private String majorCategory; // 대분류
	private String middleCategory; // 중분류
	private String smallCategory; // 소분류
	private List<String> technologyStacks;
}