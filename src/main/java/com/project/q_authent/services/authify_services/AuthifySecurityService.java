package com.project.q_authent.services.authify_services;

import com.project.q_authent.constances.ActiveCodeType;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.authify.UserDTO;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.nosqls.ActiveCode;
import com.project.q_authent.models.nosqls.User;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.repositories.ServiceActiveCodeRepository;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.repositories.UserRepository;
import com.project.q_authent.requests.authify.AuthifyNMAuthRequest;
import com.project.q_authent.requests.authify.UpdateUserRequest;
import com.project.q_authent.responses.authify.AuthifyTokenResponse;
import com.project.q_authent.responses.authify.NeedCodeValidateResponse;
import com.project.q_authent.services.notificaton_service.EmailService;
import com.project.q_authent.utils.*;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
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
    public NeedCodeValidateResponse normalSignUp(AuthifyNMAuthRequest request) throws Exception {

        UserPool userPool = handleGetUserPool();
        List<String> userFields = JsonUtils.fromJson(userPool.getUserFields());
        List<String> userAuthFields = JsonUtils.fromJson(userPool.getAuthorizeFields());
        Boolean needActiveAccount = Boolean.FALSE;

        // check existed email in pool, username
        invalidNMAuthRequestThrow(request, userAuthFields);
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
            needActiveAccount = Boolean.TRUE;
        }

        // delete all no activated
        List<User> needDeleteUsers = userRepository.findAllByPoolIdAndUsernameAndDelFlagAndIsValidated(userPool.getPoolId(), request.getUsername(), Boolean.FALSE, Boolean.FALSE);
        if (!needDeleteUsers.isEmpty()) {
            userRepository.deleteAll(needDeleteUsers);
        }

        userRepository.save(user);

        return NeedCodeValidateResponse.builder()
                .needActive(needActiveAccount)
                .userId(user.getUserId())
                .build();
    }

    /**
     * Resend active code by using COOKIE_NEED_ACTIVE_ID.
     * First, delete all last active codes with the same mail.
     * Second, create a new code and save to DB
     * Third, send to registered email
     * @param userId {@link String}
     * @return OK if not exception throw
     * @throws MessagingException || ATF_AUTH_MISSING_KEY || ATF_AUTH_USERNAME_MISSING
     */
    public NeedCodeValidateResponse resendActiveCode(String userId) throws Exception {
        // TODO: implement resend code logic
        // get userID from cookie
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

        serviceActiveCodeRepository.save(activeCode);
        emailService.sendValidationCode(user.getEmail(), codeValue);

        return NeedCodeValidateResponse.builder()
                .needActive(Boolean.TRUE)
                .userId(user.getUserId())
                .build();
    }

    /**
     * Active user.
     * if no userId, then throw ex
     * if no code pair, then throw ex
     * if not match code, then throw ex
     * @param userId String user id
     * @param activeCode String code
     * @return OK if success
     */
    public String activeUser(String userId, String activeCode) {

        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_MISSING_KEY);
        }
        // get active code
        ActiveCode activeCodePair = serviceActiveCodeRepository
                .findByUserIdAndTypeAndCode(userId, ActiveCodeType.ACTIVE_ACCOUNT, activeCode).orElseThrow(
                        () -> new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH)
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
    public AuthifyTokenResponse normalLogin(AuthifyNMAuthRequest request) throws Exception {
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

        return new AuthifyTokenResponse(accessToken, refreshToken);
    }

    /**
     * Check authorization and refresh header.
     * If one valid then refresh both key and set new header
     * @return OK if at last one key valid
     */
    public String validate(String token) throws Exception {
        onValidateFailThrow(token);

        return "OK";
    }

    /**
     * Check valid refresh token.
     * Sign new refresh and access token
     * return new access token and refresh token
     * otherwise throw ex
     * @param refreshToken String Token
     * @return new access token
     * @throws Exception BadException
     */
    public AuthifyTokenResponse refresh(String refreshToken) throws Exception {

        UserPool userPool = handleGetUserPool();
        String refreshKey = aesgcmUtils.decrypt(userPool.getPrivateRefreshKey());
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

        return new AuthifyTokenResponse(newAccessToken, newRefreshToken);
    }

    public String changePassword(String token, String newPassword, String oldPassword) throws Exception {
        // extract user from jwt
        User user = onValidateFailThrow(token);
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
     * @return OK if success
     */
    public NeedCodeValidateResponse resetPasswordStep1(String username) throws Exception {

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

        return NeedCodeValidateResponse.builder()
                .needReset(Boolean.TRUE)
                .userId(user.getUserId())
                .build();
    }

    /**
     * Get User ID from cookie -> get user.
     * Get code and check is code still valid
     * Attach code id to cookie (10 min) and mask as OK
     * @param code {@link String}
     * @return userID, codeID
     */
    public NeedCodeValidateResponse resetPasswordStep2(String code, String userId) {

        // get user ID
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
        );
        // Get code and check is code still valid
        ActiveCode activeCode = serviceActiveCodeRepository
                .findByUserIdAndTypeAndCode(user.getUserId(), ActiveCodeType.RESET_ACCOUNT, code)
                .orElseThrow(
                        () ->  new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH)
                );

        if (activeCode.getExpiredDate().before(new Date())) {
            throw new BadException(ErrorCode.ATF_AUTH_EXPIRED_CODE);
        }
        // Attach code id to cookie (10 min)

        return NeedCodeValidateResponse.builder()
                .userId(user.getUserId())
                .codeId(activeCode.getCodeId())
                .build();
    }

    /**
     * Get User and Active code from cookie.
     * Set new Password, if Active code is existed
     * Remove cookie
     * @param newPassword {@link String}
     * @return OK
     */
    @Transactional
    public String resetPasswordStep3(String newPassword, String userId, String codeId) {
        // Get User and Active code from cookie.
        if (Objects.isNull(userId)) {
            throw new BadException(ErrorCode.ATF_AUTH_USERNAME_MISSING);
        }
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_WRONG_USERNAME)
        );
        if (Objects.isNull(codeId)) {
            throw new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH);
        }
        ActiveCode activeCode = serviceActiveCodeRepository.findById(codeId).orElseThrow(
                () -> new BadException(ErrorCode.ATF_AUTH_CODE_NO_MATCH)
        );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        serviceActiveCodeRepository.delete(activeCode);

        return "OK";
    }

    /**
     * Get User and Pool.
     * Delete all last code.
     * Create new code.
     * Resend email.
     * @param userId {@link String}
     * @return OK
     */
    public NeedCodeValidateResponse resendResetCode(String userId) throws Exception {

        UserPool userPool = handleGetUserPool();
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

        return NeedCodeValidateResponse.builder()
                .needReset(Boolean.TRUE)
                .userId(user.getUserId())
                .build();
    }

    public UserDTO getUserInfo(String accessToken) throws Exception {

        User user = onValidateFailThrow(accessToken);

        return new UserDTO(user);
    }

    public UserDTO updateUser(UpdateUserRequest request) throws Exception {
        // get user
        UserPool userPool = handleGetUserPool();
        AuthifyNMAuthRequest userData = request.getUserData();
        User user = onValidateFailThrow(request.getAccessToken());
        List<String> userFields = JsonUtils.fromJson(userPool.getUserFields());
        userFields.remove("username");
        userFields.remove("password");
        for (String field : userFields) {
            switch (field) {
                case "email":
                    if (!user.getEmail().equals(userData.getEmail())) {
                        // find email in user-pool and isActive and delFlag
                        List<User> users = userRepository.findAllByPoolIdAndEmailAndDelFlagAndIsValidated(userPool.getPoolId(), userData.getEmail(), Boolean.FALSE, Boolean.TRUE);
                        if (users.isEmpty()) {
                            user.setEmail(userData.getEmail());
                        } else {
                            throw new BadException(ErrorCode.ATF_EMAIL_EXISTED);
                        }
                    }
                    break;
                case "phoneNumber":
                    user.setPhoneNumber(userData.getPhoneNumber());
                    break;
                case "telCountryCode":
                    user.setTelCountryCode(userData.getTelCountryCode());
                    break;
                case "lastName":
                    user.setLastName(userData.getLastName());
                    break;
                case "firstName":
                    user.setFirstName(userData.getFirstName());
                    break;
                case "avatarImg":
                    user.setAvatarImg(userData.getAvatarImg());
                    break;
                case "backgroundImg":
                    user.setBackgroundImg(userData.getBackgroundImg());
                    break;
                case "displayName":
                    user.setDisplayName(userData.getDisplayName());
                    break;
                case "gender":
                    user.setGender(userData.getGender());
                    break;
            }
        }
        userRepository.save(user);
        return new UserDTO(user);
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
    private User onValidateFailThrow(String token) throws Exception {

        UserPool userPool = handleGetUserPool();
        String accessKey = aesgcmUtils.decrypt(userPool.getPrivateAccessKey());
        // get authorization header
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