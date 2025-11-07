package com.project.q_authent.services.authify_services;

import com.project.q_authent.constances.ActiveCodeType;
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
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
     * Required 3 fields: username, password, email
     * @param request {@link AuthifyNMAuthRequest}
     * @return OK if success
     */
    @Transactional
    public String normalSignUp(AuthifyNMAuthRequest request, HttpServletResponse response) throws Exception {

        UserPool userPool = handleGetUserPool();

        List<String> userFields = JsonUtils.fromJson(userPool.getUserFields());
        // check existed email in pool, username
        invalidNMAuthRequestThrow(request, userFields);
        if(!userRepository.findAllByPoolIdAndUsernameAndDelFlagAndIsValidated(userPool.getPoolId(), request.getUsername(), Boolean.FALSE, Boolean.TRUE).isEmpty()) {
            throw new BadException(ErrorCode.ATF_USERNAME_EXISTED);
        }

        if(!Objects.isNull(request.getEmail()) && !userRepository.findAllByPoolIdAndEmailAndDelFlagAndIsValidated(userPool.getPoolId(), request.getEmail(), Boolean.FALSE, Boolean.TRUE).isEmpty()) {
            throw new BadException(ErrorCode.ATF_EMAIL_EXISTED);
        }

        User user = userMapper(request, userFields);
        user.setPoolId(userPool.getPoolId());
        // required fields checked
        noRequiredFieldsThrow(user);
        // if no need validate then default user is validated
        if (Objects.isNull(userPool.getEmailVerify()) || !userPool.getEmailVerify()) {
            user.setIsValidated(true);
        } else {
            // delete all resent code
            List<ActiveCode> activeCodePairs = serviceActiveCodeRepository
                    .findAllByUserPoolIdAndEmailAndType(userPool.getPoolId(),user.getEmail(), ActiveCodeType.ACTIVE_ACCOUNT);
            if (!activeCodePairs.isEmpty()) {
                serviceActiveCodeRepository.deleteAll(activeCodePairs);
            }
            // generate code
            Integer validateCode = CodeGeneratorUtils.generateCode();
            // save code pair
            ActiveCode activeCode = ActiveCode.builder()
                    .userId(user.getUserId())
                    .code(String.valueOf(validateCode))
                    .email(user.getEmail())
                    .userPoolId(userPool.getPoolId())
                    .type(ActiveCodeType.ACTIVE_ACCOUNT)
                    .build();
            serviceActiveCodeRepository.save(activeCode);
            emailService.sendValidationCode(user.getEmail(), validateCode);
            response.addCookie(getSecuredCookie(SecurityUtils.COOKIE_NEED_ACTIVE_ID, user.getUserId(), 10 * 60)); // 10 minutes
        }

        // delete all no activated
        List<User> needDeleteUsers = userRepository.findAllByPoolIdAndUsernameAndDelFlagAndIsValidated(userPool.getPoolId(), request.getUsername(), Boolean.FALSE, Boolean.FALSE);
        if (!needDeleteUsers.isEmpty()) {
            userRepository.deleteAll(needDeleteUsers);
        }

        userRepository.save(user);

        return "OK";
    }

    /**
     * Resend active code by using COOKIE_NEED_ACTIVE_ID.
     * First, delete all last active codes with the same mail.
     * Second, create a new code and save to DB
     * Third, send to registered email
     * @param servletRequest {@link HttpServletRequest}
     * @return OK if not exception throw
     * @throws MessagingException || ATF_AUTH_MISSING_KEY || ATF_AUTH_USERNAME_MISSING
     */
    public String resendActiveCode(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        // TODO: implement resend code logic
        // get userID from cookie
        String userId = SecurityUtils.getCookieValue(servletRequest, SecurityUtils.COOKIE_NEED_ACTIVE_ID);
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_MISSING_KEY);
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING)
        );
        // get email from user
        // delete old code
        List<ActiveCode> validationCodes = serviceActiveCodeRepository.findAllByUserPoolIdAndEmailAndType(handleGetUserPool().getPoolId(),user.getEmail(), ActiveCodeType.ACTIVE_ACCOUNT);
        serviceActiveCodeRepository.deleteAll(validationCodes);
        // resend new code
        Integer codeValue = CodeGeneratorUtils.generateCode();
        ActiveCode activeCode = ActiveCode.builder()
                .email(user.getEmail())
                .code(String.valueOf(codeValue))
                .userId(user.getUserId())
                .userPoolId(handleGetUserPool().getPoolId())
                .type(ActiveCodeType.ACTIVE_ACCOUNT)
                .build();
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_NEED_ACTIVE_ID, user.getUserId(), 10 * 60)); // 10 minutes

        serviceActiveCodeRepository.save(activeCode);
        emailService.sendValidationCode(user.getEmail(), codeValue);

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
                .findByUserIdAndType(userId, ActiveCodeType.ACTIVE_ACCOUNT).orElseThrow(
                        () -> new BadException(ErrorCode.ATF_AUTH_MISSING_KEY)
                );
        // check expired
        if (activeCodePair.getExpiredDate().before(new Date())) {
            throw new BadException(ErrorCode.ATF_AUTH_EXPIRED_CODE);
        }
        // compare active code
        // throw ex if not match
        if (!activeCodePair.getCode().equals(activeCode)) {
            throw new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH);
        }
        // active user
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
        );
        user.setIsValidated(true);
        userRepository.save(user);
        // delete code
        serviceActiveCodeRepository.delete(activeCodePair);

        return "OK";
    }

    /**
     * Sign and set header authorization and refresh header
     * Required 2 fields: username, password
     * @param request {@link AuthifyNMAuthRequest}
     * @return OK if success
     */
    public String normalLogin(AuthifyNMAuthRequest request, HttpServletResponse response) throws Exception {
        // get pool from header
        UserPool userPool = handleGetUserPool();
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
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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
        response.addCookie(getSecuredCookie(SecurityUtils.COOKIE_REFRESH_TOKEN, refreshToken, refreshExpired * 24 * 3600));

        return accessToken;
    }

    /**
     * Check authorization and refresh header.
     * If one valid then refresh both key and set new header
     * @return OK if at last one key valid
     */
    public String validate() throws Exception {
        onValidateFailThrow();

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

        UserPool userPool = handleGetUserPool();
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
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_REFRESH_TOKEN, newRefreshToken, refreshExpired *  24 * 3600));
        return newAccessToken;
    }

    public String changePassword(String newPassword, String oldPassword) throws Exception {
        // extract user from jwt
        User user = onValidateFailThrow();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadException(ErrorCode.ATF_AUTH_WRONG_PASSWORD);
        }
        // change new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return  "OK";
    }

    /**
     * Set need reset password id cookie.
     * Clear all reset code by user's ID
     * Create new code and save
     * send mail to registered email
     * @param username {@link String}
     * @param servletResponse {@link HttpServletResponse}
     * @return OK if success
     */
    public String resetPasswordStep1(String username, HttpServletResponse servletResponse) throws Exception {

        // Set need reset password id cookie.
        UserPool userPool = handleGetUserPool();
        User user = userRepository
                .findUserByPoolIdAndUsernameAndIsValidatedAndDelFlag(
                        userPool.getPoolId(),
                        username,
                        Boolean.TRUE,
                        Boolean.FALSE
                ).orElseThrow(
                        () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
                );
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_NEED_RESET_ID, user.getUserId(), 10 * 60)); // 10 minutes
        // Clear all reset code by user's ID
        serviceActiveCodeRepository.deleteAllByUserIdAndType(user.getUserId(), ActiveCodeType.RESET_ACCOUNT);
        // Create new code and save
        Integer activeCode = CodeGeneratorUtils.generateCode();
        serviceActiveCodeRepository.save(ActiveCode.builder()
                        .userId(user.getUserId())
                        .userPoolId(userPool.getPoolId())
                        .email(user.getEmail())
                        .code(String.valueOf(activeCode))
                        .type(ActiveCodeType.RESET_ACCOUNT)
                        .build());
        // send mail to registered email
        emailService.sendValidationCode(user.getEmail(), activeCode);

        return "OK";
    }

    /**
     * Get User ID from cookie -> get user.
     * Get code and check is code still valid
     * Attach code id to cookie (10 min) and mask as OK
     * @param code {@link String}
     * @param servletRequest {@link HttpServletRequest}
     * @param servletResponse {@link HttpServletResponse}
     * @return OK
     */
    public String resetPasswordStep2(String code, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

        // get user ID
        String userId = SecurityUtils.getCookieValue(servletRequest, SecurityUtils.COOKIE_NEED_RESET_ID);
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
        );
        // Get code and check is code still valid
        ActiveCode activeCode = serviceActiveCodeRepository
                .findByUserIdAndType(user.getUserId(), ActiveCodeType.RESET_ACCOUNT)
                .orElseThrow(
                        () ->  new BadException(ErrorCode.ATF_AUTH_MISSING_KEY)
                );

        if (!activeCode.getCode().equals(code)) {
            throw new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH);
        }
        if (activeCode.getExpiredDate().before(new Date())) {
            throw new BadException(ErrorCode.ATF_AUTH_EXPIRED_CODE);
        }
        // Attach code id to cookie (10 min)
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_RESET_CODE_ID, activeCode.getCodeId(), 10 * 60));

        return "OK";
    }

    /**
     * Get User and Active code from cookie.
     * Set new Password, if Active code is existed
     * Remove cookie
     * @param newPassword {@link String}
     * @param servletRequest {@link HttpServletRequest}
     * @param servletResponse {@link HttpServletResponse}
     * @return OK
     */
    @Transactional
    public String resetPasswordStep3(String newPassword, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        // Get User and Active code from cookie.
        String userId = SecurityUtils.getCookieValue(servletRequest, SecurityUtils.COOKIE_NEED_RESET_ID);
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
        );
        String resetCodeId = SecurityUtils.getCookieValue(servletRequest, SecurityUtils.COOKIE_RESET_CODE_ID);
        if (Objects.isNull(resetCodeId)) {
            throw new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH);
        }
        ActiveCode activeCode = serviceActiveCodeRepository.findById(resetCodeId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH)
        );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        serviceActiveCodeRepository.delete(activeCode);
        // Remove cookie
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_NEED_RESET_ID, Strings.EMPTY, 0));
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_RESET_CODE_ID, Strings.EMPTY, 0));

        return "OK";
    }

    /**
     * Get User and Pool.
     * Delete all last code.
     * Create new code.
     * Resend email.
     * Attach to cookie: need-reset-id, reset-code-id
     * @param servletRequest {@link HttpServletRequest}
     * @param servletResponse {@link HttpServletResponse}
     * @return OK
     */
    public String resendResetCode(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {

        UserPool userPool = handleGetUserPool();
        String userId = SecurityUtils.getCookieValue(servletRequest, SecurityUtils.COOKIE_NEED_RESET_ID);
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
        );
        serviceActiveCodeRepository.deleteAllByUserIdAndType(user.getUserId(), ActiveCodeType.RESET_ACCOUNT);
        Integer activeCode = CodeGeneratorUtils.generateCode();
        serviceActiveCodeRepository.save(ActiveCode.builder()
                        .userPoolId(userPool.getPoolId())
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .code(String.valueOf(activeCode))
                        .type(ActiveCodeType.RESET_ACCOUNT)
                        .build());
        emailService.sendValidationCode(user.getEmail(), activeCode);
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_NEED_RESET_ID, user.getUserId(), 10 * 60)); // 10 mins
        servletResponse.addCookie(getSecuredCookie(SecurityUtils.COOKIE_RESET_CODE_ID, Strings.EMPTY, 0));

        return "OK";
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
     * Throw {@link BadException} whether there is one or more required fields is null or empty
     * @param user {@link User}
     */
    private void noRequiredFieldsThrow(User user) {
        if (Objects.isNull(user.getUsername()) || user.getUsername().isBlank()) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        if (Objects.isNull(user.getPassword()) || user.getPassword().isBlank()) {
            throw new BadException(ErrorCode.ATF_AUTH_PASSWORD_MISSING);
        }
        if (Objects.isNull(user.getEmail()) || user.getEmail().isBlank()) {
            throw new BadException(ErrorCode.ATF_AUTH_EMAIL_MISSING);
        }
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

    /**
     * Handle validate access token to check if a token is still valid.
     * Throw ex if jwt have sign problem or expired.
     * @return User if jwt is valid
     * @throws Exception BadException
     */
    private User onValidateFailThrow() throws Exception {

        UserPool userPool = handleGetUserPool();
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

        return user;
    }

    /**
     * Handle get UserPool's information from header
     * @return UserPool object if pool-key is valid
     * @throws Exception AESGCM decrypt exception | Pool not found Exception
     */
    private UserPool handleGetUserPool() throws Exception {
        String decodePoolKey = aesgcmUtils.decrypt(Objects.requireNonNull(SecurityUtils.getPoolKeyHeader()));
        // TODO load from cache
        return userPoolRepository
                .findUserPoolByPoolKeyAndDelFlag(decodePoolKey, Boolean.FALSE)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
    }



}