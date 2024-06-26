package pulleydoreurae.careerquestbackend.auth.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pulleydoreurae.careerquestbackend.auth.domain.UserRole;
import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.config.QueryDSLConfig;

/**
 * UserAccountRepository 클래스를 테스트하는 클래스
 */
@DataJpaTest
@Import(QueryDSLConfig.class)
class UserAccountRepositoryTest {

	@Autowired
	private UserAccountRepository userAccountRepository;

	@AfterEach    // 각각의 테스트가 종료되면 데이터베이스에 저장된 내용 삭제하기
	public void after() {
		userAccountRepository.deleteAll();
	}

	@Test
	@DisplayName("1. 사용자 아이디로 데이터베이스에 검색하기")
	void findByUserIdTest() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.userName("testName")
				.email("test@email.com")
				.phoneNum("010-1111-2222")
				.password("testPassword")
				.role(UserRole.ROLE_TEMPORARY_USER)
				.build();

		// When
		userAccountRepository.save(user);

		// Then
		UserAccount getUser = userAccountRepository.findByUserId("testId").orElseThrow(() -> {
			return new UsernameNotFoundException("사용자 정보를 찾을 수 없음");
		});
		assertAll(
				() -> assertEquals(user.getId(), getUser.getId()),
				() -> assertEquals(user.getUserId(), getUser.getUserId()),
				() -> assertEquals(user.getUserName(), getUser.getUserName()),
				() -> assertEquals(user.getEmail(), getUser.getEmail()),
				() -> assertEquals(user.getPhoneNum(), getUser.getPhoneNum()),
				() -> assertEquals(user.getRole(), getUser.getRole())
		);
	}

	@Test
	@DisplayName("2. 저장한 사용자 정보가 존재하는지 테스트")
	void existsByUserIdTest() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.userName("testName")
				.email("test@email.com")
				.phoneNum("010-1111-2222")
				.password("testPassword")
				.role(UserRole.ROLE_TEMPORARY_USER)
				.build();

		// When
		userAccountRepository.save(user);

		// Then
		assertFalse(userAccountRepository.existsByUserId("testI"));
		assertTrue(userAccountRepository.existsByUserId("testId"));
	}

	@Test
	@DisplayName("3. 저장한 사용자 정보를 email 로 검색할 수 있는지 테스트")
	void existsByEmailTest() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.userName("testName")
				.email("test@email.com")
				.phoneNum("010-1111-2222")
				.password("testPassword")
				.role(UserRole.ROLE_TEMPORARY_USER)
				.build();

		// When
		userAccountRepository.save(user);

		// Then
		assertFalse(userAccountRepository.existsByEmail("test@email.co"));
		assertTrue(userAccountRepository.existsByEmail("test@email.com"));
	}
}
