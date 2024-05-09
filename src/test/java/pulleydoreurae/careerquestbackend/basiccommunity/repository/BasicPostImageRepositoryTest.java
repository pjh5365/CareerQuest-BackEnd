package pulleydoreurae.careerquestbackend.basiccommunity.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.auth.repository.UserAccountRepository;
import pulleydoreurae.careerquestbackend.basiccommunity.domain.entity.BasicPost;
import pulleydoreurae.careerquestbackend.basiccommunity.domain.entity.BasicPostImage;
import pulleydoreurae.careerquestbackend.common.community.domain.entity.Post;
import pulleydoreurae.careerquestbackend.common.community.domain.entity.PostImage;
import pulleydoreurae.careerquestbackend.common.community.repository.PostImageRepository;
import pulleydoreurae.careerquestbackend.common.community.repository.PostRepository;

/**
 * @author : parkjihyeok
 * @since : 2024/04/09
 */
@DataJpaTest
@DisplayName("사진 Repository 테스트")
class BasicPostImageRepositoryTest {

	@Autowired
	PostImageRepository postImageRepository;
	@Autowired
	PostRepository postRepository;
	@Autowired
	UserAccountRepository userAccountRepository;

	@AfterEach
	void tearDown() {
		postImageRepository.deleteAll();
		postRepository.deleteAll();
		userAccountRepository.deleteAll();
	}

	@Test
	@DisplayName("사진 저장 및 조회 테스트")
	void saveImageTest() {
		// Given
		UserAccount user = UserAccount.builder().userId("test").password("test!").build();
		userAccountRepository.save(user);

		Post post = BasicPost.builder()
				.title("제목").content("내용").userAccount(user).view(0L).category(1L).build();
		postRepository.save(post);

		// When
		PostImage image = BasicPostImage.builder().post(post).fileName("파일_저장명.txt").build();
		postImageRepository.save(image);

		// Then
		assertEquals(1, postImageRepository.findAll().size());
		assertEquals(image, postImageRepository.findAllByPost(post).get(0));
		assertTrue(postImageRepository.existsByFileName("파일_저장명.txt"));
	}

	@Test
	@DisplayName("사진 수정 테스트")
	void updateImagesTest() {
		// Given
		UserAccount user = UserAccount.builder().userId("test").password("test!").build();
		userAccountRepository.save(user);

		Post post = BasicPost.builder()
				.title("제목").content("내용").userAccount(user).view(0L).category(1L).build();
		postRepository.save(post);

		PostImage image1 = BasicPostImage.builder().post(post).fileName("파일_저장명1.txt").build();
		PostImage image2 = BasicPostImage.builder().post(post).fileName("파일_저장명2.txt").build();
		PostImage image3 = BasicPostImage.builder().post(post).fileName("파일_저장명3.txt").build();
		PostImage image4 = BasicPostImage.builder().post(post).fileName("파일_저장명4.txt").build();
		PostImage image5 = BasicPostImage.builder().post(post).fileName("파일_저장명5.txt").build();

		postImageRepository.save(image1);
		postImageRepository.save(image2);
		postImageRepository.save(image3);
		postImageRepository.save(image4);
		postImageRepository.save(image5);

		// When (저장된 파일 중 삭제할 파일명과 새롭게 추가할 파일명을 전달받음)
		postImageRepository.deleteByFileName("파일_저장명2.txt");
		postImageRepository.deleteByFileName("파일_저장명3.txt");

		PostImage image6 = BasicPostImage.builder().post(post).fileName("파일_저장명6.txt").build();
		postImageRepository.save(image6);

		// Then
		assertEquals(4, postImageRepository.findAllByPost(post).size());
		assertAll(
				() -> assertEquals("파일_저장명1.txt", postImageRepository.findAllByPost(post).get(0).getFileName()),
				() -> assertEquals("파일_저장명4.txt", postImageRepository.findAllByPost(post).get(1).getFileName()),
				() -> assertEquals("파일_저장명5.txt", postImageRepository.findAllByPost(post).get(2).getFileName()),
				() -> assertEquals("파일_저장명6.txt", postImageRepository.findAllByPost(post).get(3).getFileName())
		);
	}

	@Test
	@DisplayName("사진 삭제 테스트")
	void deleteImageTest() {
		// Given
		UserAccount user = UserAccount.builder().userId("test").password("test!").build();
		userAccountRepository.save(user);

		Post post = BasicPost.builder()
				.title("제목").content("내용").userAccount(user).view(0L).category(1L).build();
		postRepository.save(post);

		PostImage image = BasicPostImage.builder().post(post).fileName("파일_저장명.txt").build();
		postImageRepository.save(image);

		// When
		postImageRepository.deleteByFileName("파일_저장명.txt");

		// Then
		assertEquals(0, postImageRepository.findAllByPost(post).size());
	}
}