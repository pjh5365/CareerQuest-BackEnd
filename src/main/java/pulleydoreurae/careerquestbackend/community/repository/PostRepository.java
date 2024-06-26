package pulleydoreurae.careerquestbackend.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.community.domain.PostCategory;
import pulleydoreurae.careerquestbackend.community.domain.entity.Post;

/**
 * 게시글을 담당하는 Repository
 *
 * @author : parkjihyeok
 * @since : 2024/03/28
 */
public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findAllByOrderByIdDesc(Pageable pageable);

	Page<Post> findAllByUserAccountOrderByIdDesc(UserAccount userAccount, Pageable pageable);

	Page<Post> findAllByPostCategoryOrderByIdDesc(PostCategory postCategory, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE p.title LIKE concat('%', :keyword, '%') OR p.content LIKE concat('%', :keyword, '%')")
	Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT p FROM Post p WHERE p.postCategory = :category AND (p.title LIKE concat('%', :keyword, '%') OR p.content LIKE concat('%', :keyword, '%'))")
	Page<Post> searchByKeywordAndCategory(@Param("keyword") String keyword,
			@Param("category") PostCategory postCategory, Pageable pageable);
}
