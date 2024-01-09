package pulleydoreurae.chwijunjindan.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pulleydoreurae.chwijunjindan.auth.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUserId(String userId);
	boolean existsByUserId(String userId);
	boolean existsByEmail(String email);
}
