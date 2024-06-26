package pulleydoreurae.careerquestbackend.community.service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.common.service.CommonService;
import pulleydoreurae.careerquestbackend.common.service.FileManagementService;
import pulleydoreurae.careerquestbackend.community.domain.PostCategory;
import pulleydoreurae.careerquestbackend.community.domain.dto.request.PostRequest;
import pulleydoreurae.careerquestbackend.community.domain.dto.response.PostResponse;
import pulleydoreurae.careerquestbackend.community.domain.entity.Post;
import pulleydoreurae.careerquestbackend.community.domain.entity.PostImage;
import pulleydoreurae.careerquestbackend.community.domain.entity.PostViewCheck;
import pulleydoreurae.careerquestbackend.community.exception.FileSaveException;
import pulleydoreurae.careerquestbackend.community.exception.PostDeleteException;
import pulleydoreurae.careerquestbackend.community.exception.PostSaveException;
import pulleydoreurae.careerquestbackend.community.exception.PostUpdateException;
import pulleydoreurae.careerquestbackend.community.repository.PostImageRepository;
import pulleydoreurae.careerquestbackend.community.repository.PostLikeRepository;
import pulleydoreurae.careerquestbackend.community.repository.PostRepository;
import pulleydoreurae.careerquestbackend.community.repository.PostViewCheckRepository;

/**
 * 게시판을 담당하는 Service
 *
 * @author : parkjihyeok
 * @since : 2024/03/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final CommonCommunityService commonCommunityService;
	private final PostLikeRepository postLikeRepository;
	private final PostViewCheckRepository postViewCheckRepository;
	private final PostImageRepository postImageRepository;
	private final FileManagementService fileManagementService;
	private final CommonService commonService;

	@Value("${IMAGES_SAVE_PATH}")
	protected String IMAGES_SAVE_PATH;

	/**
	 * 게시글 리스트를 불러오는 메서드
	 *
	 * @param pageable 페이지
	 * @return Repository 에서 가져온 리스트 반환
	 */
	public List<PostResponse> getPostResponseList(Pageable pageable) {
		return commonCommunityService.postListToPostResponseList(postRepository.findAllByOrderByIdDesc(pageable));
	}

	/**
	 * 게시글의 카테고리로 리스트를 불러오는 메서드
	 *
	 * @param postCategory 카테고리 번호
	 * @param pageable     페이지
	 * @return 카테고리에 맞는 리스트 반환
	 */
	public List<PostResponse> getPostResponseListByCategory(PostCategory postCategory, Pageable pageable) {

		return commonCommunityService.postListToPostResponseList(
				postRepository.findAllByPostCategoryOrderByIdDesc(postCategory, pageable));
	}

	/**
	 * 한 사용자가 작성한 리스트를 불러오는 메서드 (15 개 씩 페이지로 나눠서 호출함)
	 *
	 * @param userId   회원아이디
	 * @param pageable 페이지
	 * @return 회원정보에 맞는 리스트 반환
	 */
	public List<PostResponse> getPostListByUserAccount(String userId, Pageable pageable) {
		UserAccount user = commonService.findUserAccount(userId, false);

		return commonCommunityService.postListToPostResponseList(
				postRepository.findAllByUserAccountOrderByIdDesc(user, pageable));
	}

	/**
	 * 게시글 검색 메서드
	 *
	 * @param keyword      키워드
	 * @param postCategory 카테고리 (필수값 X)
	 * @param pageable     페이지
	 * @return 검색결과
	 */
	public List<PostResponse> searchPosts(String keyword, PostCategory postCategory, Pageable pageable) {

		// 카테고리 없이 전체 검색
		if (postCategory == null) {
			return commonCommunityService.postListToPostResponseList(postRepository.searchByKeyword(keyword, pageable));
		}

		// 카테고리 포함 검색
		return commonCommunityService.postListToPostResponseList(
				postRepository.searchByKeywordAndCategory(keyword, postCategory, pageable));
	}

	/**
	 * 하나의 게시글을 불러오는 메서드
	 *
	 * @param postId 게시글 id
	 * @return 게시글 dto 를 반환
	 */
	@Transactional
	public PostResponse findByPostId(HttpServletRequest request, HttpServletResponse response, Long postId) {
		Post post = commonCommunityService.findPost(postId);

		String userId = checkView(request, response, postId, post);
		// 게시글 단건 요청은 게시글 좋아요 정보가 필요하므로 좋아요 정보를 넘기기
		return commonCommunityService.postToPostResponse(post, getIsLiked(userId, post));
	}

	/**
	 * 저장된 사진을 반환하는 메서드
	 *
	 * @param fileName 저장된 파일명
	 * @return 전달받은 파일명으로 UrlResource 생성하여 반환
	 * @throws MalformedURLException 저장된 파일명이 아닌 경우 예외를 던진다.
	 */
	public UrlResource getImageResource(String fileName) throws MalformedURLException {
		if (!postImageRepository.existsByFileName(fileName)) {
			throw new MalformedURLException("잘못된 URL 요청입니다.");
		}
		return new UrlResource("file:" + IMAGES_SAVE_PATH + fileName);
	}

	/**
	 * 조회수에 대한 처리를 담당하는 메서드
	 *
	 * @param request  요청
	 * @param response 응답
	 * @param postId   게시글 id
	 * @param post     게시글 정보
	 * @return 현재 로그인한 사용자 정보
	 */
	public String checkView(HttpServletRequest request, HttpServletResponse response, Long postId, Post post) {
		// 회원정보(userId) 가져오기
		String name;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// 인증 정보가 있고 로그인한 사용자 일때
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			name = authentication.getName();
		} else { // 회원정보가 없다면 쿠키와 UUID 를 사용해 식별한다.
			name = commonCommunityService.getUUID(request, response);
		}

		PostViewCheck postViewCheck = commonCommunityService.findPostViewCheck(name);
		if (postViewCheck == null || !postViewCheck.getPostId().equals(postId)) { // Redis 에 저장되어 있지 않다면 저장하고 조회수 증가
			post.setView(post.getView() + 1); // 조회수 증가
			postViewCheckRepository.save(mackPostViewCheck(postId, name)); // 지정한 시간동안 저장
		}
		return name;
	}

	/**
	 * 좋아요 상태를 반환하는 메서드
	 *
	 * @param userId 현재 요청한 사용자 정보
	 * @param post   게시글 정보
	 * @return false -> X, true -> O
	 */
	public Boolean getIsLiked(String userId, Post post) {
		// userId 로 회원을 가져온다.
		UserAccount user = commonService.findUserAccount(userId, false);
		// user 가 null 이거나 좋아요 누른 정보를 가져올 수 없다면 false, 눌렀다면 true
		return postLikeRepository.existsByPostAndUserAccount(post, user);
	}

	/**
	 * 이미지 저장 메서드
	 *
	 * @param images 컨트롤러로부터 전달받은 이미지 파일들
	 * @return 한장이라도 실패하면 null, 성공하면 저장한 이미지 파일들의 파일명을 리스트 형태로 반환
	 */
	public List<String> saveImage(List<MultipartFile> images) {
		List<String> successFiles = new ArrayList<>();
		images.forEach(image -> {
			String savedFile = fileManagementService.saveFile(image, IMAGES_SAVE_PATH);
			// 파일 저장에 한건이라도 실패한다면
			if (savedFile == null) {
				fileManagementService.deleteFile(successFiles, IMAGES_SAVE_PATH);
				throw new FileSaveException("사진 저장에 실패했습니다.");
			}
			successFiles.add(savedFile);
		});
		return successFiles;
	}

	/**
	 * 게시글 저장 메서드
	 *
	 * @param postRequest 게시글 요청
	 */
	@Transactional // 사진 저장과 게시글 저장을 하나의 트랜잭션으로 묶음
	public Long savePost(PostRequest postRequest) {
		List<String> fileNames = postRequest.getImages();
		try {
			UserAccount user = commonService.findUserAccount(postRequest.getUserId(), true);
			Post post = commonCommunityService.postRequestToPost(postRequest, user);
			try {
				postRepository.save(post);
				// 게시글 저장이 무사히 완료되고 사진이 서버에 저장되어 있다면 데이터베이스에 해당 정보 입력
				if (fileNames != null) {
					saveImages(fileNames, post);
				}
				return post.getId();
			} catch (Exception e) {
				log.error("게시글 저장 실패 {}", e.getMessage());
				// 예외가 발생해 게시글 저장에 실패한 경우 미리 저장한 사진 정보를 삭제한다.
				if (fileNames != null) {
					fileManagementService.deleteFile(fileNames, IMAGES_SAVE_PATH);
				}
				throw new PostSaveException("게시글 저장에 실패했습니다.");
			}
		} catch (UsernameNotFoundException e) {
			// 사용자를 찾지 못한 경우
			// 서버에 저장된 사진이 있을경우 삭제
			if (fileNames != null) {
				fileManagementService.deleteFile(fileNames, IMAGES_SAVE_PATH);
			}
			throw e;
		}
	}

	/**
	 * 서버에 저장된 파일들을 데이터베이스에 저장하는 메서드
	 *
	 * @param fileNames 서버에 저장된 파일명 리스트
	 * @param post      게시글 정보
	 */
	private void saveImages(List<String> fileNames, Post post) {
		fileNames.forEach(fileName -> {
			PostImage image = mackPostImage(post, fileName);
			postImageRepository.save(image);
		});
	}

	/**
	 * 게시글 수정 메서드 (수정에 실패하면 예외를 던진다.)
	 *
	 * @param postId      게시글 id
	 * @param postRequest 게시글 수정요청
	 */
	public void updatePost(Long postId, PostRequest postRequest) {
		Post post = commonCommunityService.findPost(postId);
		UserAccount user = commonService.findUserAccount(postRequest.getUserId(), true);
		// 작성자와 수정자가 다르다면 실패
		if (!post.getUserAccount().getUserId().equals(user.getUserId())) {
			throw new PostUpdateException("게시글 수정에 실패했습니다.");
		}

		// 이미지 내용이 있다면 수정처리
		if (postRequest.getImages() != null) {
			updateImages(postRequest.getImages(), post);
		}

		Post updatedPost = commonCommunityService.postRequestToPostForUpdate(post, postRequest, user);
		postRepository.save(updatedPost);
	}

	/**
	 * 게시글 삭제 메서드
	 *
	 * @param postId 게시글 id
	 * @param userId 삭제 요청자
	 */
	public void deletePost(Long postId, String userId) {
		UserAccount user = commonService.findUserAccount(userId, true);
		Post post = commonCommunityService.findPost(postId);
		// 작성자와 요청자가 다르다면 실패 (권한 없음)
		if (!post.getUserAccount().getUserId().equals(user.getUserId())) {
			throw new PostDeleteException("게시글 삭제에 실패했습니다.");
		}
		postRepository.deleteById(postId);

		List<PostImage> fileNames = postImageRepository.findAllByPost(post);
		// 저장된 사진 파일이 존재한다면 게시글 삭제하면서 사진도 삭제
		if (!fileNames.isEmpty()) {
			fileManagementService.deleteFile(fileNames.stream().map(PostImage::getFileName).toList(), IMAGES_SAVE_PATH);
		}
	}

	/**
	 * 새롭게 전달된 사진 정보를 바탕으로 삭제될 파일을 서버와 데이터베이스에서 제거하고 새로운 내용 데이터베이스에 추가하는 메서드
	 *
	 * @param images 업데이트된 사진정보
	 * @param post   게시글 정보
	 */
	public void updateImages(List<String> images, Post post) {
		// 서버에서 삭제할 파일명 찾기
		List<String> fileNamesToDelete = postImageRepository.findAllByPost(post).stream()
				.map(PostImage::getFileName)
				.filter(fileName -> !images.contains(fileName)).toList();

		// 서버에서 먼저 파일들 삭제
		fileManagementService.deleteFile(fileNamesToDelete, IMAGES_SAVE_PATH);
		// 삭제한 파일들 데이터베이스에서 삭제
		fileNamesToDelete.forEach(postImageRepository::deleteByFileName);

		// 새로 추가된 파일명들만 데이터베이스에 저장
		images.stream()
				.filter(fileName -> !postImageRepository.existsByFileName(fileName))
				.forEach(fileName -> {
					PostImage image = mackPostImage(post, fileName);
					postImageRepository.save(image);
				});
	}

	/**
	 * 이미지 엔티티 만들기
	 *
	 * @param post     게시글
	 * @param fileName 파일명
	 * @return 이미지 엔티티
	 */
	public PostImage mackPostImage(Post post, String fileName) {
		return PostImage.builder()
				.post(post)
				.fileName(fileName)
				.build();
	}

	/**
	 * 조회수 객체를 만드는 메서드
	 *
	 * @param postId 게시글 정보
	 * @param name   UUID값
	 * @return 조회수 객체
	 */
	public PostViewCheck mackPostViewCheck(Long postId,
			String name) {
		return new PostViewCheck(name, postId);
	}
}
