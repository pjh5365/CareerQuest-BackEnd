package pulleydoreurae.careerquestbackend.community.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.common.service.CommonService;
import pulleydoreurae.careerquestbackend.community.domain.dto.request.PostLikeRequest;
import pulleydoreurae.careerquestbackend.community.domain.dto.response.PostResponse;
import pulleydoreurae.careerquestbackend.community.domain.entity.Post;
import pulleydoreurae.careerquestbackend.community.domain.entity.PostLike;
import pulleydoreurae.careerquestbackend.community.exception.PostNotFoundException;
import pulleydoreurae.careerquestbackend.community.repository.PostLikeRepository;

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
	PostLikeRepository postLikeRepository;
	@Mock
	CommonCommunityService commonCommunityService;
	@Mock
	CommonService commonService;

	@Test
	@DisplayName("1. 좋아요 증가 테스트 (실패 - 회원정보를 찾을 수 없음)")
	void postLikePlusFail1Test() {
		// Given
		UserAccount user = UserAccount.builder().userId("testId").build();

		given(commonService.findUserAccount("testId", true))
				.willThrow(new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

		// When
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(false).build();

		// Then
		assertThrows(UsernameNotFoundException.class, () -> postLikeService.changePostLike(request));
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("2. 좋아요 증가 테스트 (실패 - 게시글 정보를 찾을 수 없음)")
	void postLikePlusFail2Test() {
		// Given
		UserAccount user = UserAccount.builder().userId("testId").build();

		given(commonService.findUserAccount("testId", true)).willReturn(user);
		given(commonCommunityService.findPost(10000L))
				.willThrow(new PostNotFoundException("게시글 정보를 찾을 수 없습니다."));

		// When
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(false).build();

		// Then
		assertThrows(PostNotFoundException.class, () -> postLikeService.changePostLike(request));
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("3. 좋아요 증가 테스트 (성공)")
	void postLikePlusSuccessTest() {
		// Given
		UserAccount user = UserAccount.builder().userId("testId").build();
		Post post = Post.builder().userAccount(user).id(10000L).title("제목1").build();

		given(commonService.findUserAccount("testId", true)).willReturn(user);
		given(commonCommunityService.findPost(10000L)).willReturn(post);

		// When
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(false).build();

		// Then
		postLikeService.changePostLike(request);

		verify(postLikeRepository).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("4. 좋아요 감소 테스트 (실패 - 회원정보를 찾을 수 없음)")
	void postLikeMinusFail1Test() {
		// Given
		UserAccount user = UserAccount.builder().userId("testId").build();

		given(commonService.findUserAccount("testId", true))
				.willThrow(new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

		// When
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(true).build();

		// Then
		assertThrows(UsernameNotFoundException.class, () -> postLikeService.changePostLike(request));
		verify(postLikeRepository, never()).save(any());
		verify(postLikeRepository, never()).delete(any());
	}

	@Test
	@DisplayName("5. 좋아요 감소 테스트 (실패 - 게시글 정보를 찾을 수 없음)")
	void postLikeMinusFail2Test() {
		// Given
		UserAccount user = UserAccount.builder().userId("testId").build();

		given(commonService.findUserAccount("testId", true)).willReturn(user);
		given(commonCommunityService.findPost(10000L))
				.willThrow(new PostNotFoundException("게시글 정보를 찾을 수 없습니다."));

		// When
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(true).build();

		// Then
		assertThrows(PostNotFoundException.class, () -> postLikeService.changePostLike(request));
		verify(postLikeRepository, never()).save(any());
	}

	@Test
	@DisplayName("6. 좋아요 감소 테스트 (성공)")
	void postLikeMinusSuccessTest() {
		// Given
		UserAccount user = UserAccount.builder().userId("testId").build();
		Post post = Post.builder().userAccount(user).id(10000L).title("제목1").build();
		PostLike postLike = PostLike.builder().userAccount(user).post(post).build();

		given(commonService.findUserAccount("testId", true)).willReturn(user);
		given(commonCommunityService.findPost(10000L)).willReturn(post);
		given(commonCommunityService.findPostLike(post, user)).willReturn(postLike);

		// When
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(true).build();

		// Then
		postLikeService.changePostLike(request);

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

		Pageable pageable = PageRequest.of(0, 3); // 한 페이지에 3개씩 자르기
		Page<PostLike> list = new PageImpl<>(
				List.of(postLike3, postLike4, postLike5), pageable, 3); // 3개씩 자른다면 마지막 3개가 반환되어야 함

		given(commonService.findUserAccount("testId", false)).willReturn(user);
		given(commonCommunityService.postToPostResponse(post3, false)).willReturn(postToPostResponse(post3));
		given(commonCommunityService.postToPostResponse(post4, false)).willReturn(postToPostResponse(post4));
		given(commonCommunityService.postToPostResponse(post5, false)).willReturn(postToPostResponse(post5));
		given(postLikeRepository.findAllByUserAccountOrderByIdDesc(user, pageable))
				.willReturn(list);

		// When
		List<PostResponse> result = postLikeService.findAllPostLikeByUserAccount(user.getUserId(), pageable);

		// Then
		assertEquals(3, result.size());
		System.out.println(result.get(0));
		assertThat(result).contains(
				postToPostResponse(post3),
				postToPostResponse(post4),
				postToPostResponse(post5)
		);
		verify(postLikeRepository).findAllByUserAccountOrderByIdDesc(user, pageable);
		verify(commonCommunityService).postToPostResponse(post3, false);
		verify(commonCommunityService).postToPostResponse(post4, false);
		verify(commonCommunityService).postToPostResponse(post5, false);
	}

	// Post -> PostResponse 변환 메서드
	PostResponse postToPostResponse(Post post) {
		return PostResponse.builder()
				.userId(post.getUserAccount().getUserId())
				.title(post.getTitle())
				.content(post.getContent())
				.view(post.getView())
				.commentCount(0L)
				.postLikeCount(0L)
				.postCategory(post.getPostCategory())
				.build();
	}
}
