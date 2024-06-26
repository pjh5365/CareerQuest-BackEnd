package pulleydoreurae.careerquestbackend.team.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pulleydoreurae.careerquestbackend.team.domain.TeamType;
import pulleydoreurae.careerquestbackend.team.domain.entity.Team;

/**
 * 팀을 담당하는 Repository
 *
 * @author : parkjihyeok
 * @since : 2024/05/31
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

	@Query("select t from Team t where t.isDeleted = false "
			+ "order by t.id desc ")
	Page<Team> findAllByOrderByIdDesc(Pageable pageable);

	@Query("select t from Team t where t.isDeleted = false and t.teamType = :teamType "
			+ "order by t.id desc")
	Page<Team> findAllByTeamTypeOrderByIdDesc(TeamType teamType, Pageable pageable);

	@Query("SELECT t FROM Team t WHERE t.teamName LIKE concat('%', :keyword, '%')")
	Page<Team> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

	@Query("select t from Team t where t.teamName = :teamName")
	List<Team> findByTeamName(@Param("teamName") String teamName);

	@Query("select t from Team t where t.teamName = :teamName and t.teamType = :teamType")
	List<Team> findByTeamNameAndTeamType(@Param("teamName") String teamName, @Param("teamType") TeamType teamType);
}
