package com.project.q_authent.repositories;

import com.project.q_authent.models.sqls.UserPoolPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPoolPolicyRepository extends JpaRepository<UserPoolPolicy,String> {

    Optional<UserPoolPolicy> findByAccount_AccountIdAndUserPool_PoolId(String accountAccountId, String userPoolPoolId);

    Optional<List<UserPoolPolicy>> findAllByAccount_AccountId(String accountAccountId);

    Optional<List<UserPoolPolicy>> findAllByCreator_AccountIdAndUserPool_PoolId(String creatorAccountId, String userPoolPoolId);
}
