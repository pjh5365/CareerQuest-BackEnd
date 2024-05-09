package pulleydoreurae.careerquestbackend.common.community.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.common.community.domain.dto.request.PostLikeRequest;
import pulleydoreurae.careerquestbackend.common.community.domain.dto.response.PostResponse;
import pulleydoreurae.careerquestbackend.basiccommunity.domain.entity.BasicPostLike;
import pulleydoreurae.careerquestbackend.common.community.repository.PostLikeRepository;
import pulleydoreurae.careerquestbackend.common.community.domain.entity.Post;
import pulleydoreurae.careerquestbackend.common.community.domain.entity.PostLike;

/**
 * 좋아요 Service
 *
 * @author : parkjihyeok
 * @since : 2024/04/03
 */
@Slf4j
@Service
public class PostLikeService {

	private final PostLikeRepository postLikeRepository;
	private final CommonCommunityService commonCommunityService;

	@Autowired
	public PostLikeService(PostLikeRepository postLikeRepository, CommonCommunityService commonCommunityService) {
		this.postLikeRepository = postLikeRepository;
		this.commonCommunityService = commonCommunityService;
	}

	/**
	 * 좋아요 상태를 변경하는 메서드
	 *
	 * @param postLikeRequest 좋아요 요청 (isLiked 가 0일땐 증가, 1일땐 감소)
	 * @return 상태변경에 성공하면 true, 실패하면 false
	 */
	public boolean changePostLike(PostLikeRequest postLikeRequest) {
		UserAccount user = commonCommunityService.findUserAccount(postLikeRequest.getUserId());
		Post post = commonCommunityService.findPost(postLikeRequest.getPostId());

		if (postLikeRequest.getIsLiked() == 0) { // 증가
			PostLike postLike = BasicPostLike.builder()
					.userAccount(user)
					.post(post)
					.build();
			postLikeRepository.save(postLike);
		} else if (postLikeRequest.getIsLiked() == 1) { // 감소
			PostLike postLike = commonCommunityService.findPostLike(post, user);
			postLikeRepository.delete(postLike);
		} else { // 잘못된 요청일때
			return false;
		}
		return true;
	}

	/**
	 * 한 사용자가 좋아요 누른 게시글 리스트를 반환하는 메서드
	 *
	 * @param userId   사용자 아이디
	 * @param pageable 페이지
	 * @return 게시글 리스트
	 */
	public List<PostResponse> findAllPostLikeByUserAccount(String userId, Pageable pageable) {
		UserAccount user = commonCommunityService.findUserAccount(userId);
		List<PostResponse> list = new ArrayList<>();

		postLikeRepository.findAllByUserAccountOrderByIdDesc(user, pageable)
				.forEach(postLike -> {
					Post post = postLike.getPost();
					// 게시글 리스트를 반환할땐 좋아요 상태를 사용하지 않는다. (0 으로 지정)
					PostResponse postResponse = commonCommunityService.postToPostResponse(post, 0);
					list.add(postResponse);
				});

		return list;
	}
}