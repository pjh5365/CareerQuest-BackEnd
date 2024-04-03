package pulleydoreurae.careerquestbackend.community.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.auth.repository.UserAccountRepository;
import pulleydoreurae.careerquestbackend.community.domain.dto.request.PostLikeRequest;
import pulleydoreurae.careerquestbackend.community.domain.dto.response.PostResponse;
import pulleydoreurae.careerquestbackend.community.domain.entity.Post;
import pulleydoreurae.careerquestbackend.community.domain.entity.PostLike;
import pulleydoreurae.careerquestbackend.community.repository.CommentRepository;
import pulleydoreurae.careerquestbackend.community.repository.PostLikeRepository;
import pulleydoreurae.careerquestbackend.community.repository.PostRepository;

/**
 * @author : parkjihyeok
 * @since : 2024/04/03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("좋아요 Service 테스트")
class PostLikeServiceTest {

	@InjectMocks
	PostLikeService postLikeService;
	@Mock
	UserAccountRepository userAccountRepository;
	@Mock
	PostRepository postRepository;
	@Mock
	CommentRepository commentRepository;
	@Mock
	PostLikeRepository postLikeRepository;

	@Test
	@DisplayName("1. 좋아요 증가 테스트 (실패 - 회원정보를 찾을 수 없음)")
	void postLikePlusFail1Test() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.build();
		Post post = Post.builder()
				.userAccount(user)
				.id(10000L)
				.title("제목1")
				.build();
		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.empty());
		given(postRepository.findById(10000L)).willReturn(Optional.of(post));

		// When
		PostLikeRequest request = PostLikeRequest.builder()
				.postId(10000L)
				.userId("testId")
				.isLiked(0)
				.build();

		// Then
		boolean result = postLikeService.changePostLike(request);

		assertFalse(result);
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("2. 좋아요 증가 테스트 (실패 - 게시글 정보를 찾을 수 없음)")
	void postLikePlusFail2Test() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.build();
		Post post = Post.builder()
				.userAccount(user)
				.id(10000L)
				.title("제목1")
				.build();
		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.of(user));
		given(postRepository.findById(10000L)).willReturn(Optional.empty());

		// When
		PostLikeRequest request = PostLikeRequest.builder()
				.postId(10000L)
				.userId("testId")
				.isLiked(0)
				.build();
		boolean result = postLikeService.changePostLike(request);

		// Then
		assertFalse(result);
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("3. 좋아요 증가 테스트 (성공)")
	void postLikePlusSuccessTest() {
	    // Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.build();
		Post post = Post.builder()
				.userAccount(user)
				.id(10000L)
				.title("제목1")
				.build();
		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.of(user));
		given(postRepository.findById(10000L)).willReturn(Optional.of(post));


	    // When
		PostLikeRequest request = PostLikeRequest.builder()
				.postId(10000L)
				.userId("testId")
				.isLiked(0)
				.build();

		// Then
		boolean result = postLikeService.changePostLike(request);

		assertTrue(result);
		verify(postLikeRepository).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("4. 좋아요 감소 테스트 (실패 - 회원정보를 찾을 수 없음)")
	void postLikeMinusFail1Test() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.build();
		Post post = Post.builder()
				.userAccount(user)
				.id(10000L)
				.title("제목1")
				.build();
		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.empty());
		given(postRepository.findById(10000L)).willReturn(Optional.of(post));

		// When
		PostLikeRequest request = PostLikeRequest.builder()
				.postId(10000L)
				.userId("testId")
				.isLiked(1)
				.build();

		// Then
		boolean result = postLikeService.changePostLike(request);

		assertFalse(result);
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("5. 좋아요 감소 테스트 (실패 - 게시글 정보를 찾을 수 없음)")
	void postLikeMinusFail2Test() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.build();
		Post post = Post.builder()
				.userAccount(user)
				.id(10000L)
				.title("제목1")
				.build();
		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.of(user));
		given(postRepository.findById(10000L)).willReturn(Optional.empty());

		// When
		PostLikeRequest request = PostLikeRequest.builder()
				.postId(10000L)
				.userId("testId")
				.isLiked(1)
				.build();
		boolean result = postLikeService.changePostLike(request);

		// Then
		assertFalse(result);
		verify(postLikeRepository, never()).save(any());
	}

	@Test
	@DisplayName("6. 좋아요 감소 테스트 (성공)")
	void postLikeMinusSuccessTest() {
		// Given
		UserAccount user = UserAccount.builder()
				.userId("testId")
				.build();
		Post post = Post.builder()
				.userAccount(user)
				.id(10000L)
				.title("제목1")
				.build();
		PostLike postLike = PostLike.builder()
				.userAccount(user)
				.post(post)
				.build();
		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.of(user));
		given(postRepository.findById(10000L)).willReturn(Optional.of(post));
		given(postLikeRepository.findByPostAndUserAccount(post, user)).willReturn(Optional.of(postLike));

		// When
		PostLikeRequest request = PostLikeRequest.builder()
				.postId(10000L)
				.userId("testId")
				.isLiked(1)
				.build();

		// Then
		boolean result = postLikeService.changePostLike(request);

		assertTrue(result);
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository).delete(any());
	}

	@Test
	@DisplayName("7. 한 회원이 좋아요 누른 게시글 불러오기")
	void findAllPostLikeByUserAccountTest() {
	    // Given
		UserAccount user = UserAccount.builder().userId("testId").build();
		Post post1 = Post.builder().userAccount(user).id(10001L).title("제목1").build();
		Post post2 = Post.builder().userAccount(user).id(10002L).title("제목2").build();
		Post post3 = Post.builder().userAccount(user).id(10003L).title("제목3").build();
		Post post4 = Post.builder().userAccount(user).id(10004L).title("제목4").build();
		Post post5 = Post.builder().userAccount(user).id(10005L).title("제목5").build();
		PostLike postLike1 = PostLike.builder().userAccount(user).post(post1).build();
		PostLike postLike2 = PostLike.builder().userAccount(user).post(post2).build();
		PostLike postLike3 = PostLike.builder().userAccount(user).post(post3).build();
		PostLike postLike4 = PostLike.builder().userAccount(user).post(post4).build();
		PostLike postLike5 = PostLike.builder().userAccount(user).post(post5).build();

		given(userAccountRepository.findByUserId("testId")).willReturn(Optional.of(user));
		given(postRepository.findById(10001L)).willReturn(Optional.of(post1));
		given(postRepository.findById(10002L)).willReturn(Optional.of(post2));
		given(postRepository.findById(10003L)).willReturn(Optional.of(post3));
		given(postRepository.findById(10004L)).willReturn(Optional.of(post4));
		given(postRepository.findById(10005L)).willReturn(Optional.of(post5));
		given(postLikeRepository.findAllByUserAccount(user))
				.willReturn(List.of(postLike1, postLike2, postLike3, postLike4, postLike5));

	    // When
		List<PostResponse> result = postLikeService.findAllPostLikeByUserAccount(user.getUserId());

		// Then
		assertEquals(5, result.size());
		assertThat(result).contains(
				postToPostResponse(post1),
				postToPostResponse(post2),
				postToPostResponse(post3),
				postToPostResponse(post4),
				postToPostResponse(post5)
		);
		verify(postLikeRepository).findAllByUserAccount(user);
	}

	// Post -> PostResponse 변환 메서드
	PostResponse postToPostResponse(Post post) {
		return PostResponse.builder()
				.userId(post.getUserAccount().getUserId())
				.title(post.getTitle())
				.content(post.getContent())
				.hit(post.getHit())
				.commentCount((long)commentRepository.findAllByPost(post).size())
				.postLikeCount((long)postLikeRepository.findAllByPost(post).size())
				.category(post.getCategory())
				.build();
	}
}