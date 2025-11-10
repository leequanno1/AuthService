package com.project.q_authent.controllers;

import com.project.q_authent.dtos.authify.UserDTO;
import com.project.q_authent.requests.account.ChangePasswordRequest;
import com.project.q_authent.requests.authify.*;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.responses.authify.AuthifyTokenResponse;
import com.project.q_authent.responses.authify.NeedCodeValidateResponse;
import com.project.q_authent.services.authify_services.AuthifySecurityService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authify")
@AllArgsConstructor
public class AuthifyController {

    private final AuthifySecurityService authifySecurityService;

    @PostMapping("/nm-sign-up")
    public JsonResponse<NeedCodeValidateResponse> normalSignUp(@RequestBody AuthifyNMAuthRequest request) throws Exception {
        return JsonResponse.success(authifySecurityService.normalSignUp(request));
    }

    @PostMapping("/active-sign-up-user")
    public JsonResponse<String> activeUser(@RequestBody AuthifyActiveUserRequest request) {
        return JsonResponse.success(authifySecurityService.activeUser(request.getUserId(), request.getActiveCode()));
    }

    /**
     * Resend Active code or Reset account code depend on NeedActive or NeedReset flag.
     * @param request {@link NeedCodeValidationRequest}
     * @return NeedCodeValidateResponse
     * @throws Exception ex
     */
    @PostMapping("/resend-code")
    public JsonResponse<NeedCodeValidateResponse> resendCode(@RequestBody NeedCodeValidationRequest request) throws Exception {

        if (request.getNeedActive()) {
            // is resend active code
            return JsonResponse.success(authifySecurityService.resendActiveCode(request.getUserId()));
        } else {
            // is resend reset account code
            return JsonResponse.success(authifySecurityService.resendResetCode(request.getUserId()));
        }
    }

    @PostMapping("/nm-login")
    public JsonResponse<AuthifyTokenResponse> normalLogin(@RequestBody AuthifyNMAuthRequest request) throws Exception {
        return JsonResponse.success(authifySecurityService.normalLogin(request));
    }

    @PostMapping("/verify")
    public JsonResponse<String> verify(@RequestBody String token) throws Exception {
        return JsonResponse.success(authifySecurityService.validate(token));
    }

    @PostMapping("/refresh")
    public JsonResponse<AuthifyTokenResponse> refresh(@RequestBody String refreshToken) throws Exception {
        return JsonResponse.success(authifySecurityService.refresh(refreshToken));
    }

    @PostMapping("/change-password")
    public JsonResponse<String> changePassword(@RequestBody ChangePasswordRequest request) throws Exception {
        return JsonResponse.success(authifySecurityService.changePassword(request.getAccessToken(), request.getNewPassword(), request.getOldPassword()));
    }

    // Enter username
    @PostMapping("/reset-password-1")
    public JsonResponse<NeedCodeValidateResponse> resetPasswordStep1(@RequestBody String username) throws Exception {
        return JsonResponse.success(authifySecurityService.resetPasswordStep1(username));
    }

    // Enter reset code
    @PostMapping("/reset-password-2")
    public JsonResponse<NeedCodeValidateResponse> resetPasswordStep2(@RequestBody NeedCodeValidationRequest request) {
        return JsonResponse.success(authifySecurityService.resetPasswordStep2(request.getCode(), request.getUserId()));
    }

    // Enter new password
    @PostMapping("/reset-password-3")
    public JsonResponse<String> resetPasswordStep3(@RequestBody ResetPasswordRequest request) {
        return JsonResponse.success(authifySecurityService.resetPasswordStep3(request.getNewPassword(), request.getVerifyInfo().getUserId(), request.getVerifyInfo().getCodeId()));
    }

    @PostMapping("/user-info")
    public JsonResponse<UserDTO> getUserInfo(@RequestBody String accessToken) throws Exception {
        return JsonResponse.success(authifySecurityService.getUserInfo(accessToken));
    }

    @PostMapping("/update-user")
    public JsonResponse<UserDTO> updateUser(@RequestBody UpdateUserRequest request) throws Exception {
        return JsonResponse.success(authifySecurityService.updateUser(request));
    }
}
