package com.tarento.upsmf.examsAndAdmissions.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.common.util.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AccessTokenValidator {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RestTemplate restTemplate;

	@Resource(name="redisTemplate")
	private HashOperations<String, Integer, User> hashOperations;

	@Value("${api.user.details}")
	private String userInfoUrl;

	@Value("${admin.allowed.endpoints}")
	private String adminAllowedEndpoints;

	@Value("${institute.allowed.endpoints}")
	private String instituteAllowedEndpoints;

	@Value("${student.allowed.endpoints}")
	private String studentAllowedEndpoints;

	@Value("${user.roles}")
	private String userRoles;

	@Value("${user.redis.hash.key}")
	private String userRedisHashKey;

	@Autowired
	private RedisUtil redisUtil;

	public String verifyUserToken(String token, boolean checkActive, String uri) {
		String userId = Constants.Parameters.UNAUTHORIZED;
		try {
			Map<String, Object> payload = validateToken(token, checkActive);
			log.debug(" request interceptor payload - {}", payload);
			if (!CollectionUtils.isEmpty(payload) && checkIss((String) payload.get(Constants.Parameters.ISS))) {
				userId = (String) payload.get(Constants.Parameters.SUB);
				if (StringUtils.isNotBlank(userId)) {
					int pos = userId.lastIndexOf(":");
					userId = userId.substring(pos + 1);
					return matchUserRole(userId, uri);
				}
			}
		} catch (Exception ex) {
			log.error((String) null, "Exception in verifyUserAccessToken: verify ", ex);
		}
		return userId;
	}

	private String matchUserRole(String userId, String uri) {
		List<String> roles = redisUtil.getRolesByUserId(userId);
		if(roles.isEmpty()) {
			log.error("Missing Appropriate Roles.");
			return Constants.Parameters.UNAUTHORIZED;
		}
		List<String> userRoleList = Arrays.asList(userRoles.split(","));
		boolean roleMatches = roles.stream().anyMatch(x -> userRoleList.contains(x.toLowerCase()));
		log.debug("Role matched - {}", roleMatches);
		if(roleMatches) {
			log.info("Role matched for userId - {}", userId);
			boolean isAdmin = roles.stream().anyMatch(x -> "exams_admin".equalsIgnoreCase(x));
			boolean isSuperAdmin = roles.stream().anyMatch(x -> "admin_superadmin".equalsIgnoreCase(x));
			// todo enable later
			/*if(isAdmin) {
				List<String> adminEndpoints = Arrays.asList(adminAllowedEndpoints.split(","));
				if(!adminEndpoints.contains(uri)) {
					return Constants.Parameters.UNAUTHORIZED;
				}
			}
			boolean isInstitute = roles.stream().anyMatch(x -> "exams_institute".equalsIgnoreCase(x));
			if(isInstitute) {
				List<String> adminEndpoints = Arrays.asList(instituteAllowedEndpoints.split(","));
				if(!adminEndpoints.contains(uri)) {
					return Constants.Parameters.UNAUTHORIZED;
				}
			}
			boolean isStudent = roles.stream().anyMatch(x -> "exams_student".equalsIgnoreCase(x.toLowerCase()));
			if(isStudent) {
				List<String> adminEndpoints = Arrays.asList(studentAllowedEndpoints.split(","));
				if(!adminEndpoints.contains(uri)) {
					return Constants.Parameters.UNAUTHORIZED;
				}
			}*/
			return userId;
		}
		return Constants.Parameters.UNAUTHORIZED;
	}

	/**
	 * Extracts, validates token, and checks expiry date if checkActive params is
	 * true
	 *
	 * @param token
	 *            String
	 * @param checkActive
	 *            Boolean
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	private Map<String, Object> validateToken(String token, boolean checkActive) throws Exception {
		String[] tokenElements = token.split("\\.");
		String header = tokenElements[0];
		System.out.println("header : "+header);
		String body = tokenElements[1];
		String signature = tokenElements[2];
		String payLoad = header + Constants.Parameters.DOT_SEPARATOR + body;
		Map<Object, Object> headerData = mapper.readValue(new String(decodeFromBase64(header)), Map.class);
		String keyId = headerData.get(Constants.Parameters.KID).toString();
//		boolean isValid = CryptoUtil.verifyRSASign(payLoad, decodeFromBase64(signature),KeyManager.getPublicKey(keyId).getPublicKey(), Constants.Parameters.SHA_256_WITH_RSA);
		boolean isValid=true;
		if (isValid) {
			Map<String, Object> tokenBody = mapper.readValue(new String(decodeFromBase64(body)), Map.class);
			if (checkActive && isExpired((Integer) tokenBody.get(Constants.Parameters.EXP))) {
				return Collections.emptyMap();
			}
			return tokenBody;
		}
		return Collections.emptyMap();
	}

	private static boolean checkIss(String iss) {
		return (KeyManager.getIssuer().equalsIgnoreCase(iss));
	}

	private static boolean isExpired(Integer expiration) {
		return (Time.currentTime() > expiration);
	}

	private static byte[] decodeFromBase64(String data) {
		return Base64Util.decode(data, 11);
	}
}
