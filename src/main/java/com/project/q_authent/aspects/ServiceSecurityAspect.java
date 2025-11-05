package com.project.q_authent.aspects;

import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.utils.AESGCMUtils;
import com.project.q_authent.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@AllArgsConstructor
public class ServiceSecurityAspect {

    private final AESGCMUtils aesgcmUtils;

    private final UserPoolRepository userPoolRepository;

    @Pointcut("within(com.project.q_authent.controllers.AuthifyController)")
    public void serviceControllerMethods() {}

    @Before("serviceControllerMethods()")
    public void logHeaders() {

        String poolKey = SecurityUtils.getPoolKeyHeader();

        if (Objects.isNull(poolKey) || poolKey.isEmpty()) {
            throw new BadException(ErrorCode.POOL_KEY_MISSING);
        } else {
            try {
                String encryptedPoolKey = aesgcmUtils.decrypt(poolKey);
                // TODO: light-weigh load from cache
                userPoolRepository.findUserPoolByPoolKeyAndDelFlag(encryptedPoolKey, Boolean.FALSE).orElseThrow(
                        () -> new BadException(ErrorCode.POOL_KEY_INVALID)
                );
            } catch (Exception ex) {
                throw new BadException(ErrorCode.POOL_KEY_INVALID);
            }
        }
    }
}
