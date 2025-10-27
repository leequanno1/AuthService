package com.project.q_authent.controllers;

import com.project.q_authent.dtos.AccountDTO;
import com.project.q_authent.requests.account.ChangePasswordRequest;
import com.project.q_authent.requests.account.CreateSubUserRequest;
import com.project.q_authent.requests.account.ForgotPasswordRequest;
import com.project.q_authent.requests.account.ValidateCodeRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.services.account_service.AccountService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authentication controller
 * Last update 2025/10/02
 * @since 1.00
 * @author leequanno1
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Send validation code to target account's email
     * @param emailOrUsername email or username of account {@link String}
     * @return Account id if send success {@link String}
     * @since 1.00
     */
    @PostMapping("/send-code/{emailOrUsername}")
    public JsonResponse<String> sendValidationCode(@PathVariable("emailOrUsername") String emailOrUsername) throws MessagingException {

        return JsonResponse.success(accountService.sendCode(emailOrUsername));
    }

    /**
     * Validate code by using accountID and code, use after /send-code
     * @param request include accountID and validationCode {@link String}
     * @return validationCodeID {@link String}
     */
    @PostMapping("/validate-code")
    public JsonResponse<String> validateEmailCode(@RequestBody ValidateCodeRequest request) {

        return JsonResponse.success(accountService.validationEmailCode(request.getAccountId(), request.getCode()));
    }

    /**
     * Reset password by send validationID and password, do after /validate-code
     * @param request include validationID and new password
     * @return OK
     */
    @PostMapping("/reset-forgot-password")
    public JsonResponse<String> resetForgotPassword(@RequestBody ForgotPasswordRequest request) {

        return JsonResponse.success(accountService.resetForgotPassword(request.getValidationID(), request.getNewPassword()));
    }

    @PostMapping("/change-password")
    public JsonResponse<String> changePassword(@RequestBody ChangePasswordRequest request) {

        return JsonResponse.success(accountService.changePassword(request.getOldPassword(), request.getNewPassword()));
    }

    /**
     * Create subuser by using root account
     * @param request include username, password, email {@link String}
     * @return sub-user ID
     */
    @PostMapping("/create-subuser")
    public JsonResponse<String> createSubUser(@RequestBody CreateSubUserRequest request) {

        return JsonResponse.success(accountService.createSubUser(request.getUsername(), request.getPassword(), request.getEmail()));
    }

    /**
     * Show all subaccounts of a parent account's ID.
     * First check current account is equal or higher level than the target parent.
     * Then show all subaccounts
     * @param parentId {@link String}
     * @return {@link List} of {@link AccountDTO}
     */
    @GetMapping("/get-all/{parent-id}")
    public JsonResponse<List<AccountDTO>> getSubAccountByParentId(@PathVariable("parent-id") String parentId) {

        return JsonResponse.success(accountService.getSubAccountByParentId(parentId));
    }

    /**
     * Check exist account, throw exception if exist
     * @param rootId rootID
     * @param email email
     * @param username username
     * @return OK weather have no account in root ID have email and username
     */
    @GetMapping("/check-existed/{root-id}/{email}/{username}")
    public JsonResponse<String> checkAccountExists(
            @PathVariable("root-id") String rootId,
            @PathVariable("email") String email,
            @PathVariable("username") String username) {

        return JsonResponse.success(accountService.checkAccountExists(rootId, email, username));
    }

    @GetMapping("/get-root")
    public JsonResponse<AccountDTO> getRootAccount() {
        return  JsonResponse.success(accountService.getRootAccount());
    }
}
