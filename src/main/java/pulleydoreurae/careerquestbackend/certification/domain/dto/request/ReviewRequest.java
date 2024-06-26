package pulleydoreurae.careerquestbackend.certification.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 자격증 후기 request
 *
 * @author : parkjihyeok
 * @since : 2024/05/12
 */
@Getter
@AllArgsConstructor
@Builder
public class ReviewRequest {

	@NotBlank(message = "사용자 아이디는 필수입니다.")
	@Size(min = 5, message = "유효하지 않은 아이디입니다.") // 작성자의 아이디는 5자보다 작을 수 없다.
	private String userId; // 작성자

	@NotBlank(message = "자격증 이름은 필수입니다.")
	private String certificationName; // 자격증이름

	@NotBlank(message = "제목은 필수입니다.")
	private String title; // 제목

	@NotBlank(message = "내용은 필수입니다.")
	private String content; // 내용
}
