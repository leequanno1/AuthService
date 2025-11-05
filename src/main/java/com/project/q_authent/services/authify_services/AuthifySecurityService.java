package com.project.q_authent.services.authify_services;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.nosqls.ActiveCode;
import com.project.q_authent.models.nosqls.User;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.repositories.ServiceActiveCodeRepository;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.repositories.UserRepository;
import com.project.q_authent.requests.authify.AuthifyNMAuthRequest;
import com.project.q_authent.services.notificaton_service.EmailService;
import com.project.q_authent.utils.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AuthifySecurityService {

    private final AuthifyJwtService authifyJwtService;
    private final AESGCMUtils aesgcmUtils;
    private final UserPoolRepository userPoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ServiceActiveCodeRepository serviceActiveCodeRepository;

    /**
     * Normal sign up, add secured cookie active-id to account
     * If field need active then default isValidated value is false.
     * Then send active code to registered email.
     * @param request {@link AuthifyNMAuthRequest}
     * @return OK if success
     */
    @Transactional
    public String normalSignUp(AuthifyNMAuthRequest request, HttpServletResponse response) throws Exception {

        String decodePoolKey = aesgcmUtils.decrypt(Objects.requireNonNull(SecurityUtils.getPoolKeyHeader()));
        // TODO load from cache
        UserPool userPool = userPoolRepository
                .findUserPoolByPoolKeyAndDelFlag(decodePoolKey, Boolean.FALSE)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));

        List<String> userFields = JsonUtils.fromJson(userPool.getUserFields());
        // check existed email in pool, username
        if(!userRepository.findAllByPoolIdAndUsernameAndDelFlagAndIsValidated(userPool.getPoolId(), request.getUsername(), Boolean.FALSE, Boolean.TRUE).isEmpty()) {
            throw new BadException(ErrorCode.ATF_USERNAME_EXISTED);
        }

        if(!Objects.isNull(request.getEmail()) && !userRepository.findAllByPoolIdAndEmailAndDelFlag(userPool.getPoolId(), request.getEmail(), Boolean.FALSE).isEmpty()) {
            throw new BadException(ErrorCode.ATF_EMAIL_EXISTED);
        }

        User user = userMapper(request, userFields);
        user.setPoolId(userPool.getPoolId());
        // if no need validate then default user is validated
        if (Objects.isNull(userPool.getEmailVerify()) || !userPool.getEmailVerify()) {
            user.setIsValidated(true);
        } else {
            // delete all resent code
            List<ActiveCode> activeCodePairs = serviceActiveCodeRepository
                    .findAllByUserId(user.getUserId());
            if (!activeCodePairs.isEmpty()) {
                serviceActiveCodeRepository.deleteAll(activeCodePairs);
            }
            // generate code
            Integer validateCode = CodeGeneratorUtils.generateCode();
            // save code pair
            ActiveCode activeCode = ActiveCode.builder()
                    .userId(user.getUserId())
                    .code(String.valueOf(validateCode))
                    .build();
            serviceActiveCodeRepository.save(activeCode);
            emailService.sendValidationCode(user.getEmail(), validateCode);
            response.addCookie(getSecuredCookie(SecurityUtils.COOKIE_NEED_ACTIVE_ID, user.getUserId(), 10 * 3600));
        }

        userRepository.save(user);
        // delete all no activated
        List<User> needDeleteUsers = userRepository.findAllByPoolIdAndUsernameAndDelFlagAndIsValidated(userPool.getPoolId(), request.getUsername(), Boolean.FALSE, Boolean.FALSE);
        if (!needDeleteUsers.isEmpty()) {
            userRepository.deleteAll(needDeleteUsers);
        }

        return "OK";
    }

    /**
     * Active user.
     * if no userId, then throw ex
     * if no code pair, then throw ex
     * if not match code, then throw ex
     * @param request contain cookie active user id
     * @param activeCode String code
     * @return OK if success
     */
    public String activeUser(HttpServletRequest request, String activeCode) {

        // get userId
        String userId = SecurityUtils.getCookieValue(request, SecurityUtils.COOKIE_NEED_ACTIVE_ID);
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_MISSING_KEY);
        }
        // get active code
        ActiveCode activeCodePair = serviceActiveCodeRepository
                .findByUserId(userId).orElseThrow(
                        () -> new BadException(ErrorCode.ATF_AUTH_MISSING_KEY)
                );
        // compare active code
        // throw ex if not match
        if (!activeCodePair.getCode().equals(activeCode)) {
            throw new BadException(ErrorCode.ATF_AUTH_ACTIVE_NO_MATCH);
        }

        return "OK";
    }

    /**
     * Sign and set header authorization and refresh header
     * @param request {@link AuthifyNMAuthRequest}
     * @return OK if success
     */
    public String normalLogin(AuthifyNMAuthRequest request, HttpServletResponse response) throws Exception {
        // get pool from header
        String decodePoolKey = aesgcmUtils.decrypt(Objects.requireNonNull(SecurityUtils.getPoolKeyHeader()));
        // TODO load from cache
        UserPool userPool = userPoolRepository
                .findUserPoolByPoolKeyAndDelFlag(decodePoolKey, Boolean.FALSE)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        List<String> authFields = JsonUtils.fromJson(userPool.getAuthorizeFields());
        // check valid request
        invalidNMAuthRequestThrow(request, authFields);
        // get user by username
        User user = userRepository
                .findUserByPoolIdAndUsernameAndIsValidatedAndDelFlag(userPool.getPoolId(), request.getUsername(), Boolean.TRUE, Boolean.FALSE)
                .orElseThrow(
                        () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
                );
        // check password match
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadException(ErrorCode.ATF_AUTH_WRONG_PASSWORD);
        }
        // check optional match
        optionalNotMatchThrow(request, user, authFields);
        // get access key, and refresh key
        String accessKey = aesgcmUtils.decrypt(userPool.getPrivateAccessKey());
        String refreshKey = aesgcmUtils.decrypt(userPool.getPrivateRefreshKey());
        Integer accessExpired = userPool.getAccessExpiredMinutes();
        Integer refreshExpired = userPool.getRefreshExpiredDays();
        // sign
        String accessToken = authifyJwtService.generateAccessToken(user, accessKey, accessExpired);
        String refreshToken = authifyJwtService.generateRefreshToken(user, refreshKey, refreshExpired);
        // set cookie refresh token
        response.addCookie(getSecuredCookie("refresh_token", refreshToken, refreshExpired * 24 * 3600));

        return accessToken;
    }

    /**
     * Check authorization and refresh header.
     * If one valid then refresh both key and set new header
     * @return OK if at last one key valid
     */
    public String validate() throws Exception {
        String decodePoolKey = aesgcmUtils.decrypt(Objects.requireNonNull(SecurityUtils.getPoolKeyHeader()));
        // TODO load from cache
        UserPool userPool = userPoolRepository
                .findUserPoolByPoolKeyAndDelFlag(decodePoolKey, Boolean.FALSE)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        String accessKey = aesgcmUtils.encrypt(userPool.getPrivateAccessKey());
        // get authorization header
        String token = SecurityUtils.getAuthorizationHeader();
        if (Objects.isNull(token)) {
            throw new BadException(ErrorCode.ATF_AUTH_NO_AUTHORIZATION_HEADER);
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new BadException(ErrorCode.ATF_AUTH_INVALID_AUTHORIZATION_HEADER);
        }
        String userId;
        try {
            User tempUSer = authifyJwtService.extractUser(token, accessKey);
            userId = tempUSer.getUserId();
        }  catch (Exception e) {
            throw new BadException(ErrorCode.ATF_AUTH_INVALID_AUTHORIZATION_HEADER);
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_INVALID_AUTHORIZATION_HEADER)
        );

        if (!authifyJwtService.isTokenValid(token, user, accessKey)) {
            if (authifyJwtService.isTokenExpired(token, accessKey)) {
                throw new BadException(ErrorCode.ATF_AUTH_ACCESS_EXPIRED);
            }
            throw new BadException(ErrorCode.ATF_AUTH_INVALID_AUTHORIZATION_HEADER);
        }

        return "OK";
    }

    /**
     * Check valid refresh token.
     * Sign new refresh and access token
     * return new access token and attach refresh token to http-only cookie
     * otherwise throw ex
     * @param servletRequest {@link HttpServletRequest}
     * @param servletResponse {@link HttpServletResponse}
     * @return new access token
     * @throws Exception BadException
     */
    public String refresh(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {

        String decodePoolKey = aesgcmUtils.decrypt(Objects.requireNonNull(SecurityUtils.getPoolKeyHeader()));
        // TODO load from cache
        UserPool userPool = userPoolRepository
                .findUserPoolByPoolKeyAndDelFlag(decodePoolKey, Boolean.FALSE)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        String refreshKey = aesgcmUtils.decrypt(userPool.getPrivateRefreshKey());
        String refreshToken = SecurityUtils.getCookieValue(servletRequest, SecurityUtils.COOKIE_REFRESH_TOKEN);
        if (Objects.isNull(refreshToken)) {
            throw new BadException(ErrorCode.ATF_AUTH_NO_AUTHORIZATION_HEADER);
        }

        String userId;

        try {
            User tempUSer = authifyJwtService.extractUser(refreshToken, refreshKey);
            userId = tempUSer.getUserId();
        }  catch (Exception e) {
            throw new BadException(ErrorCode.ATF_AUTH_INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_INVALID_REFRESH_TOKEN)
        );

        if (!authifyJwtService.isTokenValid(refreshToken, user, refreshKey)) {
            if (authifyJwtService.isTokenExpired(refreshToken, refreshKey)) {
                throw new BadException(ErrorCode.ATF_AUTH_REFRESH_EXPIRED);
            }
            throw new BadException(ErrorCode.ATF_AUTH_INVALID_REFRESH_TOKEN);
        }
        // sign new key
        String accessKey = aesgcmUtils.decrypt(userPool.getPrivateAccessKey());
        Integer accessExpired = userPool.getAccessExpiredMinutes();
        Integer refreshExpired = userPool.getRefreshExpiredDays();
        String newAccessToken = authifyJwtService.generateAccessToken(user, accessKey, accessExpired);
        String newRefreshToken = authifyJwtService.generateRefreshToken(user, refreshKey, refreshExpired *  24 * 60);
        servletResponse.addCookie(getSecuredCookie("refresh_token", newRefreshToken, refreshExpired *  24 * 3600));
        return newAccessToken;
    }

//    ==============================================================================================

    private User userMapper(AuthifyNMAuthRequest request, List<String> userFields) {
        User user = new User();
        user.setUserId(IDUtil.getID(TableIdHeader.USER_HEADER));

        return userMapper(request, userFields, user);
    }

    /**
     * Mapping {@link AuthifyNMAuthRequest} request to {@link User}
     * @param request {@link AuthifyNMAuthRequest}
     * @param userFields {@link List}
     * @param user {@link User}
     * @return {@link User}
     */
    private User userMapper(AuthifyNMAuthRequest request, List<String> userFields, User user) {
        for (String userField : userFields) {
            switch (userField) {
                case "username":
                    user.setUsername(request.getUsername());
                    break;
                case "email":
                    user.setEmail(request.getEmail());
                    break;
                case "phoneNumber":
                    user.setPhoneNumber(request.getPhoneNumber());
                    break;
                case "telCountryCode":
                    user.setTelCountryCode(request.getTelCountryCode());
                    break;
                case "password":
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    break;
                case "lastName":
                    user.setLastName(request.getLastName());
                    break;
                case "firstName":
                    user.setFirstName(request.getFirstName());
                    break;
                case "avatarImg":
                    user.setAvatarImg(request.getAvatarImg());
                    break;
                case "backgroundImg":
                    user.setBackgroundImg(request.getBackgroundImg());
                    break;
                case "displayName":
                    user.setDisplayName(request.getDisplayName());
                    break;
                case "gender":
                    user.setGender(request.getGender());
                    break;
            }
        }

        return user;
    }

    /**
     * Check if request have all the field in authorization fields.
     * If any null or empty then throw ex.
     * @param request {@link AuthifyNMAuthRequest}
     * @param authField List<String>
     */
    private void invalidNMAuthRequestThrow(AuthifyNMAuthRequest request, List<String> authField) {
        if (Objects.isNull(request.getUsername()) || request.getUsername().isEmpty()) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        if (Objects.isNull(request.getPassword()) || request.getPassword().isEmpty()) {
            throw new BadException(ErrorCode.ATF_AUTH_PASSWORD_MISSING);
        }
        boolean isErrorThrow;
        for (String field : authField) {
            isErrorThrow = switch (field) {
                case "email" -> Objects.isNull(request.getEmail()) || request.getEmail().isEmpty();
                case "phoneNumber" -> Objects.isNull(request.getPhoneNumber()) || request.getPhoneNumber().isEmpty();
                case "telCountryCode" ->
                        Objects.isNull(request.getTelCountryCode()) || request.getTelCountryCode().isEmpty();
                case "lastName" -> Objects.isNull(request.getLastName()) || request.getLastName().isEmpty();
                case "firstName" -> Objects.isNull(request.getFirstName()) || request.getFirstName().isEmpty();
                case "displayName" -> Objects.isNull(request.getDisplayName()) || request.getDisplayName().isEmpty();
                case "gender" -> Objects.isNull(request.getGender());
                default -> false;
            };
            if (isErrorThrow) {
                throw new BadException(ErrorCode.ATF_AUTH_OPTIONAL_MISSING);
            }
        }
    }

    /**
     * Check all optional field in request to match with user store in DB.
     * If one not match then throw error.
     * @param request {@link AuthifyNMAuthRequest}
     * @param user {@link User}
     * @param authField {@link List}
     */
    private void optionalNotMatchThrow(AuthifyNMAuthRequest request, User user, List<String> authField) {

        boolean isErrorThrow;
        for (String field : authField) {
            isErrorThrow = switch (field) {
                case "email" -> !user.getEmail().equals(request.getEmail());
                case "phoneNumber" -> !user.getPhoneNumber().equals(request.getPhoneNumber());
                case "telCountryCode" -> !user.getTelCountryCode().equals(request.getTelCountryCode());
                case "lastName" -> !user.getLastName().equals(request.getLastName());
                case "firstName" -> !user.getFirstName().equals(request.getFirstName());
                case "displayName" -> !user.getDisplayName().equals(request.getDisplayName());
                case "gender" -> !user.getGender().equals(request.getGender());
                default -> false;
            };
            if (isErrorThrow) {
                throw new BadException(ErrorCode.ATF_AUTH_WRONG_OPTIONAL);
            }
        }
    }

    private Cookie getSecuredCookie(String key, String value, Integer maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        return cookie;
    }


}