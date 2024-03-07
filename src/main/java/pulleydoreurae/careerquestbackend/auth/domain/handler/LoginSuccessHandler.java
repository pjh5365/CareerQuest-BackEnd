package pulleydoreurae.careerquestbackend.auth.domain.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.JwtTokenProvider;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.dto.JwtTokenResponse;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.entity.JwtAccessToken;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.entity.JwtRefreshToken;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.repository.JwtAccessTokenRepository;
import pulleydoreurae.careerquestbackend.auth.domain.jwt.repository.JwtRefreshTokenRepository;

/**
 * 로그인 성공 핸들러 클래스
 *
 * @author : parkjihyeok
 * @since : 2024/01/17
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtRefreshTokenRepository jwtRefreshTokenRepository;
	private final JwtAccessTokenRepository jwtAccessTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final Gson gson;

	@Autowired
	public LoginSuccessHandler(JwtRefreshTokenRepository jwtRefreshTokenRepository,
			JwtAccessTokenRepository jwtAccessTokenRepository, JwtTokenProvider jwtTokenProvider,
			Gson gson) {
		this.jwtRefreshTokenRepository = jwtRefreshTokenRepository;
		this.jwtAccessTokenRepository = jwtAccessTokenRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.gson = gson;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		// 로그인에 성공하면 redis 에 저장할 객체 생성
		JwtTokenResponse jwtTokenResponse = jwtTokenProvider.createJwtResponse(authentication.getName());

		// 로그인한 id 를 키 값으로 생성한 액세스토큰을 redis 에 저장
		jwtAccessTokenRepository
				.save(new JwtAccessToken(authentication.getName(), jwtTokenResponse.getAccess_token()));

		// 로그인한 id 를 키 값으로 생성된 리프레시토큰 을 redis 에 저장
		jwtRefreshTokenRepository
				.save(new JwtRefreshToken(authentication.getName(), jwtTokenResponse.getRefresh_token()));

		// 생성된 객체를 클라이언트에게 전달
		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json;charset=UTF-8");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		response.getWriter().write(gson.toJson(jwtTokenResponse));
	}
}