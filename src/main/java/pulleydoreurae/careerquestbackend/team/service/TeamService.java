package pulleydoreurae.careerquestbackend.team.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pulleydoreurae.careerquestbackend.auth.domain.entity.UserAccount;
import pulleydoreurae.careerquestbackend.common.service.CommonService;
import pulleydoreurae.careerquestbackend.team.domain.TeamType;
import pulleydoreurae.careerquestbackend.team.domain.dto.request.KickRequest;
import pulleydoreurae.careerquestbackend.team.domain.dto.request.TeamDeleteRequest;
import pulleydoreurae.careerquestbackend.team.domain.dto.request.TeamMemberRequest;
import pulleydoreurae.careerquestbackend.team.domain.dto.request.TeamRequest;
import pulleydoreurae.careerquestbackend.team.domain.dto.response.EmptyTeamMemberResponse;
import pulleydoreurae.careerquestbackend.team.domain.dto.response.TeamDetailResponse;
import pulleydoreurae.careerquestbackend.team.domain.dto.response.TeamMemberHistoryResponse;
import pulleydoreurae.careerquestbackend.team.domain.dto.response.TeamMemberResponse;
import pulleydoreurae.careerquestbackend.team.domain.dto.response.TeamResponse;
import pulleydoreurae.careerquestbackend.team.domain.dto.response.TeamResponseWithPageInfo;
import pulleydoreurae.careerquestbackend.team.domain.entity.EmptyTeamMember;
import pulleydoreurae.careerquestbackend.team.domain.entity.Team;
import pulleydoreurae.careerquestbackend.team.domain.entity.TeamMember;
import pulleydoreurae.careerquestbackend.team.repository.EmptyTeamMemberRepository;
import pulleydoreurae.careerquestbackend.team.repository.TeamMemberRepository;
import pulleydoreurae.careerquestbackend.team.repository.TeamRepository;

/**
 * 팀 매칭을 담당하는 Service
 *
 * @author : parkjihyeok
 * @since : 2024/05/31
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 모든 메서드에 트랜잭션 적용
public class TeamService {

	private final TeamRepository teamRepository;
	private final EmptyTeamMemberRepository emptyTeamMemberRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final CommonService commonService;

	/**
	 * 한 회원이 팀에 참여했던 정보를 전달하는 메서드
	 *
	 * @param userId 회원ID
	 * @return 각 팀에 참여했던 정보
	 */
	@Transactional(readOnly = true)
	public List<TeamMemberHistoryResponse> findMemberHistory(String userId) {
		return teamMemberRepository.findByUserId(userId)
				.stream()
				.map(teamMember ->
						TeamMemberHistoryResponse.builder()
								.userId(userId)
								.isTeamLeader(teamMember.isTeamLeader())
								.position(teamMember.getPosition())
								.teamId(teamMember.getTeam().getId())
								.teamName(teamMember.getTeam().getTeamName())
								.teamType(teamMember.getTeam().getTeamType())
								.build()
				).toList();
	}

	/**
	 * 전체 팀을 반환하는 메서드
	 *
	 * @param pageable 페이지 정보
	 * @return 팀을 담은 객체
	 */
	@Transactional(readOnly = true)
	public TeamResponseWithPageInfo findAll(Pageable pageable) {
		Page<Team> allByOrderByIdDesc = teamRepository.findAllByOrderByIdDesc(pageable);

		return makeTeamResponse(allByOrderByIdDesc);
	}

	/**
	 * 팀 타입에 맞는 팀들을 반환하는 메서드
	 *
	 * @param teamType 팀 타입
	 * @param pageable 페이지 정보
	 * @return 팀을 담은 객체
	 */
	@Transactional(readOnly = true)
	public TeamResponseWithPageInfo findAllByTeamType(TeamType teamType, Pageable pageable) {
		Page<Team> allByOrderByIdDesc = teamRepository.findAllByTeamTypeOrderByIdDesc(teamType, pageable);

		return makeTeamResponse(allByOrderByIdDesc);
	}

	/**
	 * 한 팀에 대한 세부정보를 반환하는 메서드
	 *
	 * @param teamId 팀ID
	 * @return 요청한 팀에 대한 세부정보
	 */
	@Transactional(readOnly = true)
	public TeamDetailResponse findByTeamId(Long teamId) {
		Team team = findTeam(teamId);
		TeamResponse teamResponse = teamToTeamResponse(team);

		List<TeamMember> teamMembers = teamMemberRepository.findAllByTeamId(teamId);
		List<EmptyTeamMember> emptyTeamMembers = emptyTeamMemberRepository.findAllByTeamId(teamId);

		return makeTeamDetailResponse(teamResponse, teamMembers, emptyTeamMembers);
	}

	/**
	 * 팀장이 팀을 생성하는 메서드
	 *
	 * @param makeRequest 팀장의 정보 + 팀에 대한 정보
	 */
	public void makeTeam(TeamRequest makeRequest) {
		UserAccount user = commonService.findUserAccount(makeRequest.getTeamLeaderId(), true); // 팀장의 정보 불러오기
		Team team = Team.builder()
				.teamName(makeRequest.getTeamName())
				.teamContent(makeRequest.getTeamContent())
				.teamType(makeRequest.getTeamType())
				.maxMember(makeRequest.getMaxMember())
				.startDate(makeRequest.getStartDate())
				.endDate(makeRequest.getEndDate())
				.isOpened(true)
				.isDeleted(false)
				.build();

		teamRepository.save(team);

		savePositions(makeRequest.getPositions(), team); // 선호 포지션 저장
		saveTeamMember(user, team, makeRequest.getPosition(), true); // 팀장의 정보 팀원에 저장
	}

	/**
	 * 팀 정보를 수정하는 메서드
	 *
	 * @param updateRequest 수정 요청
	 */
	public void updateTeam(TeamRequest updateRequest) {
		Team team = findTeam(updateRequest.getTeamId());
		Team newTeam = Team.builder()
				.id(team.getId())
				.teamName(updateRequest.getTeamName())
				.teamContent(updateRequest.getTeamContent())
				.teamType(updateRequest.getTeamType())
				.maxMember(updateRequest.getMaxMember())
				.startDate(updateRequest.getStartDate())
				.endDate(updateRequest.getEndDate())
				.isOpened(updateRequest.isOpened())
				.isDeleted(false)
				.build();

		teamRepository.save(newTeam); // 팀 정보 업데이트

		List<EmptyTeamMember> emptyTeamMembers = emptyTeamMemberRepository.findAllByTeamId(team.getId());
		emptyTeamMemberRepository.deleteAll(emptyTeamMembers); // 선호했던 팀원의 포지션 전체 삭제
		savePositions(updateRequest.getPositions(), team); // 선호 포지션 저장
	}

	/**
	 * 팀 제거 메서드
	 *
	 * @param deleteRequest 제거 요청
	 */
	public void deleteTeam(TeamDeleteRequest deleteRequest) {
		commonService.checkAuth(deleteRequest.getTeamLeaderId());

		// 팀 정보가 있는지 확인
		Team team = findTeam(deleteRequest.getTeamId());

		// 팀을 제거하더라도 팀에 대한 정보는 남아있어야 한다.
		team.changeStatus(false);
		team.delete();
	}

	/**
	 * 새로운 팀원이 팀에 참여하는 메서드
	 *
	 * @param request 팀원의 요청
	 */
	public void joinTeam(TeamMemberRequest request) {
		UserAccount user = commonService.findUserAccount(request.getUserId(), true);
		Team team = findTeam(request.getTeamId());

		checkDate(team); // 팀 모집 종료일로 설정한 날짜 확인

		if (!team.isOpened()) { // 팀이 활성화 상태가 아니라면
			throw new IllegalArgumentException("팀이 현재 팀원을 모집하고 있지 않습니다.");
		}

		deleteEmptyTeamMember(request, team); // 선호 팀원 포지션에서 새로 참여할 팀원의 포지션 제거
		saveTeamMember(user, team, request.getPosition(), false); // 새로운 팀원 저장
	}

	/**
	 * 팀원이 팀을 떠나는 메서드
	 *
	 * @param request 팀원의 요청
	 */
	public void leaveTeam(TeamMemberRequest request) {
		UserAccount user = commonService.findUserAccount(request.getUserId(), true);

		TeamMember teamMember = findTeamMember(request.getTeamId(), user);
		Team team = teamMember.getTeam();

		saveEmptyTeamMember(team, teamMember.getPosition()); // 팀에 빈자리 추가하기
		teamMemberRepository.delete(teamMember); // 팀에서 팀원 제거
	}

	// 팀원 추방
	public void kickMember(KickRequest request) {
		UserAccount leader = commonService.findUserAccount(request.getTeamLeaderId(), true);
		UserAccount target = commonService.findUserAccount(request.getTargetId(), false);

		TeamMember teamLeader = findTeamMember(request.getTeamId(), leader);
		Team team = teamLeader.getTeam();

		if (!teamLeader.isTeamLeader()) {
			throw new IllegalAccessError("추방 권한이 없습니다!");
		}

		TeamMember targetMember = findTeamMember(team.getId(), target);

		saveEmptyTeamMember(team, request.getPosition()); // 팀에 빈자리 추가
		teamMemberRepository.delete(targetMember); // 팀원 제거
	}

	/**
	 * 팀에 설정한 종료일자를 넘겼다면 팀 닫기
	 *
	 * @param team 팀
	 */
	private void checkDate(Team team) {
		if (LocalDate.now().isAfter(team.getEndDate())) {
			team.changeStatus(false);
		}
	}

	/**
	 * Team -> TeamResponse 변환 메서드
	 *
	 * @param team 팀
	 * @return TeamResponse 객체
	 */
	private TeamResponse teamToTeamResponse(Team team) {
		return TeamResponse.builder()
				.teamId(team.getId())
				.teamName(team.getTeamName())
				.teamContent(team.getTeamContent())
				.teamType(team.getTeamType())
				.maxMember(team.getMaxMember())
				.startDate(team.getStartDate())
				.endDate(team.getEndDate())
				.isOpened(team.isOpened())
				.build();
	}

	/**
	 * 팀의 정보를 담은 페이지를 반환하는 메서드
	 *
	 * @param searchResult 검색결과
	 * @return 변환결과
	 */
	private TeamResponseWithPageInfo makeTeamResponse(Page<Team> searchResult) {
		int totalPages = searchResult.getTotalPages();

		TeamResponseWithPageInfo response = new TeamResponseWithPageInfo(totalPages);
		searchResult.forEach(team -> {
			TeamResponse detail = teamToTeamResponse(team);
			response.getTeamResponse().add(detail);
		});

		return response;
	}

	/**
	 * 팀에 대한 세부 정보를 응답으로 만드는 메서드
	 *
	 * @param teamResponse     팀에 대한 기본 정보
	 * @param teamMembers      팀원에 대한 정보
	 * @param emptyTeamMembers 선호하는 팀원 포지션
	 * @return 팀에 대한 전체 내용을 담은 객체
	 */
	private TeamDetailResponse makeTeamDetailResponse(TeamResponse teamResponse, List<TeamMember> teamMembers,
			List<EmptyTeamMember> emptyTeamMembers) {

		TeamDetailResponse response = new TeamDetailResponse(teamResponse);

		teamMembers.forEach(teamMember -> {
			response.getTeamMemberResponses().add(TeamMemberResponse.builder()
					.userId(teamMember.getUserAccount().getUserId())
					.isTeamLeader(teamMember.isTeamLeader())
					.position(teamMember.getPosition())
					.build());
		});

		emptyTeamMembers.forEach(emptyTeamMember -> {
			response.getEmptyTeamMemberResponses().add(EmptyTeamMemberResponse.builder()
					.position(emptyTeamMember.getPosition())
					.build());
		});

		return response;
	}

	/**
	 * 팀원을 저장하는 메서드
	 *
	 * @param user         팀원으로 저장될 회원정보
	 * @param team         팀 정보
	 * @param position     팀원의 포지션
	 * @param isTeamLeader 팀장여부
	 */
	private void saveTeamMember(UserAccount user, Team team, String position, boolean isTeamLeader) {
		TeamMember teamMember = TeamMember.builder()
				.userAccount(user)
				.isTeamLeader(isTeamLeader)
				.team(team)
				.position(position)
				.build();

		teamMemberRepository.save(teamMember); // 팀원 저장
	}

	/**
	 * 팀장이 지정한 팀원의 선호 포지션을 저장하는 메서드
	 *
	 * @param positions 포지션 정보
	 * @param team      팀 정보
	 */
	private void savePositions(List<String> positions, Team team) {
		// 선호 포지션 저장
		positions.forEach(position -> saveEmptyTeamMember(team, position));
	}

	/**
	 * 팀을 찾아오는 메서드
	 *
	 * @param teamId 팀ID
	 * @return 팀이 있으면 팀을 없으면 예외를 던짐
	 */
	private Team findTeam(Long teamId) {
		Optional<Team> byId = teamRepository.findById(teamId);

		if (byId.isEmpty() || byId.get().isDeleted()) {
			throw new IllegalArgumentException("팀에 대한 정보를 찾을 수 없습니다.");
		}

		return byId.get();
	}

	/**
	 * 미리 선점한 팀원자리 제거
	 *
	 * @param request 팀원 정보
	 * @param team    팀 정보
	 */
	private void deleteEmptyTeamMember(TeamMemberRequest request, Team team) {
		// 선호 포지션에서 새로 참여한 팀원의 포지션정보로 정보 찾아오기
		List<EmptyTeamMember> findEmptyTeamMembers = emptyTeamMemberRepository
				.findAllByTeamIdAndPosition(team.getId(), request.getPosition());

		// 찾아온 정보중 아무거나 1개 선택
		Optional<EmptyTeamMember> emptyTeamMember = findEmptyTeamMembers.stream().findAny();

		// 선호포지션을 미리 지정한 자리에서 찾을 수 없다면
		if (emptyTeamMember.isEmpty()) {
			// 팀장이 미리 지정한 선호 포지션중 아무거나 1개 선택
			emptyTeamMember = emptyTeamMemberRepository.findAllByTeamId(team.getId()).stream().findAny();

			// 팀원 포지션을 선택할 수 없다면 팀에 빈 자리가 없는것이므로 예외 던지기
			if (emptyTeamMember.isEmpty()) {
				throw new IllegalArgumentException("팀에 빈 자리가 없습니다!");
			}
		}

		// 선호 포지션에서 제거
		emptyTeamMemberRepository.delete(emptyTeamMember.get());
	}

	/**
	 * 팀원을 불러오는 메서드
	 *
	 * @param teamId 팀ID
	 * @param user   팀원의 회원 정보
	 * @return 저장된 팀원
	 */
	private TeamMember findTeamMember(Long teamId, UserAccount user) {
		Optional<TeamMember> optionalTeamMember = teamMemberRepository
				.findByUserIdAndTeamId(user.getUserId(), teamId);

		if (optionalTeamMember.isEmpty()) {
			throw new IllegalArgumentException("팀에 회원의 정보를 찾을 수 없습니다.");
		}
		return optionalTeamMember.get();
	}

	/**
	 * 팀에 빈자리를 추가하는 메서드
	 *
	 * @param team     팀 정보
	 * @param position 선호 팀원 포지션
	 */
	private void saveEmptyTeamMember(Team team, String position) {
		EmptyTeamMember emptyTeamMember = EmptyTeamMember.builder()
				.team(team)
				.position(position)
				.build();

		emptyTeamMemberRepository.save(emptyTeamMember); // 빈자리 저장
	}
}
