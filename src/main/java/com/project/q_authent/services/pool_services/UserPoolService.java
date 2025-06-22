package com.project.q_authent.services.pool_services;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.requests.userpools.UserPoolRequest;
import com.project.q_authent.utils.AESGCMUtils;
import com.project.q_authent.utils.IDUtil;
import com.project.q_authent.utils.RandomKeyGenerator;
import com.project.q_authent.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPoolService {

    private final AccountRepository accountRepository;
    private final UserPoolRepository userPoolRepository;
    private final AESGCMUtils aesgcmUtils;

    public String createNewUserPool(UserPoolRequest request) throws Exception {
        String userId = SecurityUtils.getCurrentUserId();
        assert userId != null;
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.USER_NOT_FOUND)
        );
        UserPool userPool = UserPool.builder()
                .poolId(IDUtil.getID(TableIdHeader.USER_POOL_HEADER))
                .account(account)
                .userFields(request.getUserFields())
                .authorizeFields(request.getAuthorizeFields())
                .poolKey(RandomKeyGenerator.generateKeyBase64(128)) // Raw key
                .privateAccessKey(aesgcmUtils.encrypt(RandomKeyGenerator.generateKeyBase64(128))) //encrypt key
                .privateRefreshKey(aesgcmUtils.encrypt(RandomKeyGenerator.generateKeyBase64(128))) //encrypt key
                .poolName(request.getPoolName())
                .build();

        userPoolRepository.save(userPool);
        return "Ok";
    }

}
