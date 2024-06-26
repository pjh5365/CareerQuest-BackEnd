package pulleydoreurae.careerquestbackend.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pulleydoreurae.careerquestbackend.auth.domain.UserRole;
import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.mail.entity.EmailAuthentication;
import pulleydoreurae.careerquestbackend.mail.entity.UserInfoUserId;
import pulleydoreurae.careerquestbackend.mail.repository.EmailAuthenticationRepository;
import pulleydoreurae.careerquestbackend.mail.repository.UserInfoUserIdRepository;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

/**
 * 이메일 전송 및 인증을 담당하는 Service
 *
 * @author : hanjaeseong
 * @since : 2024/02/04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

	private final EmailAuthenticationRepository emailAuthenticationRepository;
	private final UserInfoUserIdRepository userInfoUserIdRepository;
	private final SpringTemplateEngine templateEngine;
	@Value("${spring.mail.domain}")
	private String domain;
	@Value("${spring.mail.URL}")
	private String url;
	@Value("${spring.mail.OFFICE_TOKEN}")
	private String office_token;
	@Value("${spring.mail.USER_ID}")
	private String user_Id;
	@Value("${VERIFY_EMAIL_PATH}")
	private String verifyEmailPath;

	/**
	 * 인증번호를 생성하는 메서드
	 *
	 * @return 인증번호를 돌려준다.
	 */
	public String createNumber() {
		String result;
		do {
			int num;
			try {
				num = SecureRandom.getInstanceStrong().nextInt(999999);
			} catch (NoSuchAlgorithmException e) {
				// 알고리즘에서 예외가 발생하면 Random 클래스를 사용해 인증번호를 뽑는다.
				do {
					Random random = new Random();
					num = random.nextInt(999999);
					result = String.valueOf(num);
				} while (result.length() != 6);
			}
			result = String.valueOf(num);
		} while (result.length() != 6);

		return result;
	}

	/**
	 * 이메일 인증 메서드
	 *
	 * @param userId   회원아이디
	 * @param userName 회원이름
	 * @param phoneNum 전화번호
	 * @param email    이메일
	 * @param password 비밀번호
	 * @param birth    생년월일
	 * @param gender   성별
	 */
	public void emailAuthentication(String userId, String userName, String phoneNum, String email
		, String password, String birth, String gender, Boolean isMarketed) {

		String number = createNumber();
		String verification_url = domain + verifyEmailPath + number + "&email=" + email;

		sendMail(email, verification_url, "mailForm", "취준진담 이메일 인증");

		emailAuthenticationRepository.save(
			new EmailAuthentication(email, userId, userName, phoneNum, password, birth, gender, number, isMarketed));
		// 인증을 기다리는 동안 userId 선점하기 (인증을 기다리는 동안 다른 사용자가 같은 userId 로 회원가입을 막기 위함)
		// email 의 경우 이메일인증의 키값이므로 따로 저장할 필요 X
		userInfoUserIdRepository.save(new UserInfoUserId(userId));
		log.info("[회원가입 - 인증] : {} 의 회원가입을 위한 객체저장 및 메일전송", email);
	}

	/**
	 * 이메일 인증 재전송 메서드
	 *
	 * @param userId   회원아이디
	 * @param userName 회원이름
	 * @param phoneNum 전화번호
	 * @param email    이메일
	 * @param password 비밀번호
	 * @param birth    생년월일
	 * @param gender   성별
	 */
	public void sendAgainAuthenticationEmail(String userId, String userName, String phoneNum, String email
		, String password, String birth, String gender, Boolean isMarketed) {

		if (isEmailExists(email)) {
			emailAuthenticationRepository.deleteById(email);
		}
		emailAuthentication(userId, userName, phoneNum, email, password, birth, gender, isMarketed);

		log.info("[회원가입 - 인증] : {} 의 회원가입을 위한 객체저장 및 메일 재전송", email);
	}

	/**
	 * 이메일을 전송하는 메서드
	 *
	 * @param email    받는 이메일
	 * @param url_link 메일 폼에 들어가는 링크
	 * @param subject  이메일 제목
	 */
	public void sendMail(String email, String url_link, String template, String subject) {

		//template 명: src/main/resources/templates 하단에 작성된 html 파일 명
		Context context = new Context();

		String content = templateEngine.process(template, context);
		content = content.replace("@{verification_url}", url_link);
		String saveSentMail = "Y";


		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.add("Authorization", "Bearer " + office_token);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("to", email);
		body.add("user_id", user_Id);
		body.add("cc", "");
		body.add("bcc", "");
		body.add("subject", subject);
		body.add("content", content);
		body.add("save_sent_mail", saveSentMail);

		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<String> response = new RestTemplate().exchange(
				url,
				HttpMethod.POST,
				requestEntity,
				String.class
			);
			log.info(response.getBody());
		} catch (Exception e) {
			log.error("[회원가입-이메일 전송] : 메일 전송 실패 --- {}", e.getMessage());
		}

	}

	/**
	 * 인증을 확인하는 메서드
	 *
	 * @param email               인증을 요청한 이메일
	 * @param certificationNumber 인증을 요청한 이메일의 인증번호
	 * @return 인증번호가 맞다면 true / 아닐경우 false
	 */
	public boolean verifyEmail(String email, String certificationNumber) {
		return isVerify(email, certificationNumber);
	}

	/**
	 * Redis 에 인증을 요청한 이메일을 검증하는 메서드
	 *
	 * @param email               인증을 요청한 이메일
	 * @param certificationNumber 인증을 요청한 이메일의 인증번호
	 * @return 인증 정보가 일치하는지 확인
	 */
	private boolean isVerify(String email, String certificationNumber) {
		Optional<EmailAuthentication> authentication = emailAuthenticationRepository.findById(email);
		if (!isEmailExists(email) || authentication.isEmpty()) {
			// 이메일이 redis 에 없거나 인증정보를 불러올 수 없다면
			log.warn("[회원가입 - 인증] : {} 의 회원가입을 위한 정보를 불러올 수 없음 (1차 확인)", email);
			return false;
		}
		EmailAuthentication getAuthentication = authentication.get();
		return getAuthentication.getCode().equals(certificationNumber);
	}

	/**
	 * 이메일 존재 여부 확인 메서드
	 *
	 * @param email 요청한 이메일
	 * @return 존재 여부 확인
	 */
	private boolean isEmailExists(String email) {
		return emailAuthenticationRepository.existsById(email);
	}

	/**
	 * 요청한 이메일의 유저 정보 반환 메서드
	 * 유저 정보를 확인할 수 있다면 회원가입에 성공하므로 redis 에 제거해주기
	 *
	 * @param email 요청한 이메일
	 * @return 이메일의 유저 정보
	 */
	public UserAccount getVerifiedUser(String email) {
		Optional<EmailAuthentication> authentication = emailAuthenticationRepository.findById(email);

		if (authentication.isEmpty()) {
			log.warn("[회원가입 - 인증] : {} 의 회원가입을 위한 정보를 불러올 수 없음 (2차 확인)", email);
			return null;
		}
		EmailAuthentication getAuthentication = authentication.get();
		return UserAccount.builder()
			.userId(getAuthentication.getUserId())
			.email(getAuthentication.getEmail())
			.userName(getAuthentication.getUserName())
			.phoneNum(getAuthentication.getPhoneNum())
			.password(getAuthentication.getPassword())
			.birth(getAuthentication.getBirth())
			.gender(getAuthentication.getGender())
			.role(UserRole.ROLE_TEMPORARY_USER)
			.build();
	}

	/**
	 * 인증에 완료한 사용자 정보를 redis 에서 삭제하는 메서드
	 *
	 * @param userId 중복을 막기위해 선점했던 userId
	 * @param email  redis 에 저장된 key 정보
	 */
	public void removeVerifiedUser(String userId, String email) {
		// 실제 데이터베이스에 저장되었으므로 redis 에 삭제하여도 중복이 발생하지 않는다.
		userInfoUserIdRepository.deleteById(userId);
		emailAuthenticationRepository.deleteById(email);
	}
}
