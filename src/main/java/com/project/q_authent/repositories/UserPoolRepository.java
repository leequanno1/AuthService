package com.project.q_authent.repositories;

import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.models.sqls.UserPoolPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPoolRepository extends JpaRepository<UserPool, String> {

    Optional<List<UserPool>> findUserPoolsByAccount_AccountId(String accountId);

    Optional<List<UserPool>> findUserPoolsByAccount_AccountIdAndDelFlag(String accountId, Boolean delFlag);

    List<UserPool> findAllByAccountAndPoolName(Account account, String poolName);

    List<UserPool> findAllByPoolIdIsIn(Collection<String> poolIds);

}
