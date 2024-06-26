package pulleydoreurae.careerquestbackend.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pulleydoreurae.careerquestbackend.auth.domain.MBTI;
import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUserId(String userId);

	Optional<UserAccount> findByEmail(String email);

	boolean existsByUserId(String userId);

	boolean existsByEmail(String email);

	@Query("select u.mbti from UserAccount u where u.userId = :userId")
	MBTI findMBTIByUserId(String userId);
}
