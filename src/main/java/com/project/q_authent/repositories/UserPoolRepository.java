package com.project.q_authent.repositories;

import com.project.q_authent.models.sqls.UserPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPoolRepository extends JpaRepository<UserPool, String> {

    Optional<List<UserPool>> findUserPoolsByAccount_AccountId(String accountId);

    Optional<List<UserPool>> findUserPoolsByAccount_AccountIdAndDelFlag(String accountId, Boolean delFlag);

    Optional<UserPool> findByAccount_AccountId(String accountId);
}
