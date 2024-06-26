package pulleydoreurae.careerquestbackend.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import pulleydoreurae.careerquestbackend.auth.domain.filter.LoginFilter;
import pulleydoreurae.careerquestbackend.auth.domain.filter.LogoutFilter;
import pulleydoreurae.careerquestbackend.auth.domain.handler.LoginFailureHandler;
import pulleydoreurae.careerquestbackend.auth.domain.handler.LoginSuccessHandler;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.JwtTokenProvider;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.filter.JwtAuthenticationFilter;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.repository.JwtAccessTokenRepository;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.repository.JwtRefreshTokenRepository;
import pulleydoreurae.careerquestbackend.auth.repository.UserAccountRepository;

/**
 * 스프링 시큐리티 설정 클래스
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final AuthenticationConfiguration authenticationConfiguration;
	private final LoginSuccessHandler loginSuccessHandler;
	private final LoginFailureHandler loginFailureHandler;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserAccountRepository userAccountRepository;
	private final JwtRefreshTokenRepository jwtRefreshTokenRepository;
	private final JwtAccessTokenRepository jwtAccessTokenRepository;

	@Autowired
	public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
			LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler,
			JwtTokenProvider jwtTokenProvider, UserAccountRepository userAccountRepository,
			JwtRefreshTokenRepository jwtRefreshTokenRepository, JwtAccessTokenRepository jwtAccessTokenRepository) {
		this.authenticationConfiguration = authenticationConfiguration;
		this.loginSuccessHandler = loginSuccessHandler;
		this.loginFailureHandler = loginFailureHandler;
		this.jwtTokenProvider = jwtTokenProvider;
		this.userAccountRepository = userAccountRepository;
		this.jwtRefreshTokenRepository = jwtRefreshTokenRepository;
		this.jwtAccessTokenRepository = jwtAccessTokenRepository;
	}

	@Bean    // 빈으로 등록하면 자동으로 검색해서 AuthenticationProvider 구현체들을 등록하는듯?
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
			Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public LoginFilter loginFilter() throws Exception {
		LoginFilter loginFilter = new LoginFilter("/api/login");
		loginFilter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
		loginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
		loginFilter.setAuthenticationFailureHandler(loginFailureHandler);
		return loginFilter;
	}

	@Bean
	public LogoutFilter logOutFilter() {
		LogoutFilter logoutFilter = new LogoutFilter(jwtAccessTokenRepository, jwtRefreshTokenRepository);
		return logoutFilter;
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
		return new JwtAuthenticationFilter(
				authenticationManager(authenticationConfiguration), userAccountRepository, jwtRefreshTokenRepository,
				jwtAccessTokenRepository, jwtTokenProvider);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests((auth) ->
				auth
						.requestMatchers("/api/login", "/api/users", "/api/verify", "/api/login-kakao/**"
								, "/api/login-kakao/**", "/api/login-google/**", "/api/login-naver/**", "/"
								, "/api/users/username/**", "/api/users/email/**", "/api-test", "/api/users/help/**"
								, "/api/users/details/**", "/api/users/delete", "/api/chat/**", "/api/login-github/**")
						.permitAll()    // 로그인, 회원가입, 루트 페이지는 모두 접근 가능
						.requestMatchers("/docs/index.html", "/repoSaveOk.html")
						.permitAll()    // Spring REST Docs 를 보기 위해 모두 접근 가능
						.requestMatchers(HttpMethod.GET, "/api/certification/**", "/api/certifications/pass-rate/**",
								"/api/certifications/reviews/**", "/api/certifications/reviews/user/**",
								"/api/certifications/likes/**", "/api/certifications/user-certification/**",
								"/api/comments/**", "/api/posts/\\d+/comments", "/api/posts/**", "/api/posts/category/**",
								"/api/posts/user/**", "/api/posts/search/**", "/api/posts/**", "/api/contests/**",
								"/api/posts/images/**", "/api/contests/join/**", "/api/posts/likes/**",
								"/api/search/keyword/**", "/api/search/ranking/**", "/api/search/update/**",
								"/api/teams/history/**", "/api/teams/**", "/api/teams-details/**", "/api/portfolio/**")
						.permitAll()
						.requestMatchers(HttpMethod.POST, "/api/certifications/pass-rate/**", "/api/posts/search/**", "/api/portfolio/**")
						.permitAll()
						.anyRequest()
						.authenticated());    // 나머지는 인증된 사용자만 접근가능

		http.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // JWT 를 사용하므로 세션은 사용하지 않음

		http.csrf((csrf) -> csrf.disable());    // csrf 는 우선 사용하지 않음.

		http.cors(auth -> { // cors 전체 허용
			auth.configurationSource(request -> {
				CorsConfiguration cors = new CorsConfiguration();
				cors.setAllowedOrigins(List.of("*"));
				cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
				cors.setAllowedHeaders(List.of("*"));
				return cors;
			});
		});

		http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class); // 로그인 필터 변경
		http.addFilterAt(logOutFilter(), LoginFilter.class); // 로그아웃 필터 추가
		http.addFilter(jwtAuthenticationFilter()); // JWT 토큰을 확인하는 필터 추가

		return http.build();
	}
}
