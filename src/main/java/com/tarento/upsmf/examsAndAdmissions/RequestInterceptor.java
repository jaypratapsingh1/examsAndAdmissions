package com.tarento.upsmf.examsAndAdmissions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.tarento.upsmf.examsAndAdmissions.controller.BaseController;
import com.tarento.upsmf.examsAndAdmissions.util.AccessTokenValidator;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import com.tarento.upsmf.examsAndAdmissions.util.ResponseCode;

@Component
@Slf4j
public class RequestInterceptor extends BaseController implements HandlerInterceptor {

	@Autowired
	private AccessTokenValidator accessTokenValidator;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(request.getRequestURI().endsWith("login")) {
			return Boolean.TRUE;
		}
		log.info("Request type - {}", request.getMethod());
		if(request.getMethod().equalsIgnoreCase("options")){
			return Boolean.TRUE;
		}
		// read auth token from header
		if(request.getHeader(Constants.Parameters.X_USER_TOKEN) == null
				|| request.getHeader(Constants.Parameters.X_USER_TOKEN).isBlank()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(handleResponse(false, ResponseCode.TOKEN_MISSING));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		String authToken = request.getHeader(Constants.Parameters.X_USER_TOKEN);
		if (StringUtils.isBlank(authToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(handleResponse(false, ResponseCode.TOKEN_MISSING));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		// authentication
		System.out.println("request_token :"+ authToken);
		String userId = verifyRequestData(authToken, request.getRequestURI());
		//String userId = "userId";

		System.out.println("userId :"+ userId);
		if (userId.equalsIgnoreCase(Constants.Parameters.UNAUTHORIZED)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write(handleResponse(false, ResponseCode.UNAUTHORIZED));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		request.setAttribute(Constants.Parameters.USER_ID, userId);
		return Boolean.TRUE;
	}

	private String verifyRequestData(String accessToken, String uri) {
		System.out.println("verifyRequestData () "+accessToken);
		String clientAccessTokenId = accessTokenValidator.verifyUserToken(accessToken, true, uri);
		System.out.println("verifyRequestData clientAccessTokenId (): "+clientAccessTokenId);
		return StringUtils.isBlank(clientAccessTokenId) ? Constants.Parameters.UNAUTHORIZED : clientAccessTokenId;
	}

}
