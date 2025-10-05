package com.project.q_authent.repositories;

import com.project.q_authent.models.sqls.AccountPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountPolicyRepository extends JpaRepository<AccountPolicy, String> {
    Optional<AccountPolicy> findByTargetAccount_AccountId(String targetAccountAccountId);
}
