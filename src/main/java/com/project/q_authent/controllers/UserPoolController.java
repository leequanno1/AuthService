package com.project.q_authent.controllers;

import com.project.q_authent.dtos.UserPoolDTO;
import com.project.q_authent.dtos.UserPoolDTOFull;
import com.project.q_authent.requests.userpools.UserPoolRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.constances.AuthField;
import com.project.q_authent.services.pool_services.UserPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User pool controller
 * Last update 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@RestController()
@RequestMapping("/api/user-pool")
@RequiredArgsConstructor
public class UserPoolController {

    private final UserPoolService userPoolService;

    /**
     * Get available authentication field
     * @return JsonResponse<String[]> result
     * @since 1.00
     */
    @GetMapping("/get-auth-field")
    public JsonResponse<String[]> getAuthField(){
        return JsonResponse.success(AuthField.fields);
    }

    /**
     * Create new user pool
     * @param request user pool info
     * @return "Ok" if success
     * @throws Exception if fail when encrypt key or 403
     * @since 1.00
     */
    @PostMapping("/create")
    public JsonResponse<String> createNewUserPool(@RequestBody UserPoolRequest request) throws Exception {
        return JsonResponse.success(userPoolService.createNewUserPool(request));
    }

    /**
     * Update 2 edit available field {poolName, emailVerify}.
     * If emailVerify is true and userFields dont have "email" so add one
     * @param request UserPoolRequest
     * @return JsonResponse<String> result
     * @since 1.00
     */
    @PostMapping("/update")
    public JsonResponse<String> updateUserPool(@RequestBody UserPoolRequest request) {
        return JsonResponse.success(userPoolService.updateUserPool(request.getPoolId(), request.getPoolName(), request.getEmailVerify()));
    }

    /**
     * This api use when a user want to fully modify a user pool.
     * Most using after user create a blank pool so the pool admin can modify later.
     * @param request @{@link UserPoolRequest}
     * @return OK
     */
    @PostMapping("/advance-update")
    public JsonResponse<String> advanceUpdateUserPool(@RequestBody UserPoolRequest request) {
        return JsonResponse.success(userPoolService.advanceUpdateUserPool(request));
    }

    /**
     * Set del_flag for user that delete
     * @param poolId user pool id
     * @return "Ok" if success
     * @since 1.00
     */
    @GetMapping("/delete/{poolId}")
    public JsonResponse<String> deleteUserPool(@PathVariable("poolId") String poolId) {
        return JsonResponse.success(userPoolService.deleteUserPool(poolId));
    }

    /**
     * Return User pool information
     * @param showDeleted show deleted pool, true if user want to show, otherwise false
     * @return JsonResponse<List<UserPoolDTO>> result
     * @since 1.00
     */
    @GetMapping("/get-all/{showDeleted}")
    public JsonResponse<List<UserPoolDTO>> getAllUserPool(@PathVariable("showDeleted") boolean showDeleted){
        return JsonResponse.success(userPoolService.getAllUserPool(showDeleted));
    }

    /**
     * Get more user pool detail by pool id
     * @param poolId User pool id
     * @return JsonResponse<UserPoolDTOFull>
     * @throws Exception if account id is null, pool id is null, user is not the pool owner, key decoded error
     * @since 1.00
     */
    @GetMapping("/get-detail/{poolId}")
    public JsonResponse<UserPoolDTOFull> getPoolDetail(@PathVariable("poolId") String poolId) throws Exception {
        return JsonResponse.success(userPoolService.getPoolDetail(poolId));
    }
}
