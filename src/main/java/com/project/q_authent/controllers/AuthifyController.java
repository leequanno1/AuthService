package com.project.q_authent.controllers;

import com.project.q_authent.requests.account.ChangePasswordRequest;
import com.project.q_authent.requests.authify.AuthifyActiveUserRequest;
import com.project.q_authent.requests.authify.AuthifyNMAuthRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.services.authify_services.AuthifySecurityService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authify")
@AllArgsConstructor
public class AuthifyController {

    private final AuthifySecurityService authifySecurityService;

    @PostMapping("/nm-sign-up")
    public JsonResponse<String> normalSignUp(@RequestBody AuthifyNMAuthRequest request, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.normalSignUp(request, servletResponse));
    }

    @PostMapping("/active-user")
    public JsonResponse<String> activeUser(@RequestBody AuthifyActiveUserRequest request, HttpServletRequest servletRequest) throws Exception {
        return JsonResponse.success(authifySecurityService.activeUser(servletRequest, request.getActiveCode()));
    }

    @PostMapping("/resend-active-code")
    public JsonResponse<String> resendActiveCode(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.resendActiveCode(servletRequest, servletResponse));
    }

    @PostMapping("/nm-login")
    public JsonResponse<String> normalLogin(@RequestBody AuthifyNMAuthRequest request, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.normalLogin(request, servletResponse));
    }

    @PostMapping("/validate")
    public JsonResponse<String> validate() throws Exception {
        return JsonResponse.success(authifySecurityService.validate());
    }

    @PostMapping("/refresh")
    public JsonResponse<String> refresh(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.refresh(servletRequest, servletResponse));
    }

    @PostMapping("/change-password")
    public JsonResponse<String> changePassword(@RequestBody ChangePasswordRequest request) throws Exception {
        return JsonResponse.success(authifySecurityService.changePassword(request.getNewPassword(), request.getOldPassword()));
    }

    // Enter username
    @PostMapping("/reset-password-1")
    public JsonResponse<String> resetPasswordStep1(@RequestBody String username, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.resetPasswordStep1(username, servletResponse));
    }

    // Enter reset code
    @PostMapping("/reset-password-2")
    public JsonResponse<String> resetPasswordStep2(@RequestBody String code, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.resetPasswordStep2(code, servletRequest, servletResponse));
    }

    // Enter new password
    @PostMapping("/reset-password-3")
    public JsonResponse<String> resetPasswordStep3(@RequestBody String newPassword, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        return JsonResponse.success(authifySecurityService.resetPasswordStep3(newPassword, servletRequest, servletResponse));
    }

    // Resend reset code
    @PostMapping("/resend-reset-code")
    public JsonResponse<String> resendResetCode(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.resendResetCode(servletRequest, servletResponse));
    }
}
