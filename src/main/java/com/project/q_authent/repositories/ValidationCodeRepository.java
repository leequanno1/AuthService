package com.project.q_authent.repositories;

import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.ValidationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidationCodeRepository extends JpaRepository<ValidationCode, String> {

    void deleteAllByTargetAccount(Account account);

    Optional<ValidationCode> findByTargetAccount_AccountIdOrderByExpireTimeDesc(String accountID);
}
