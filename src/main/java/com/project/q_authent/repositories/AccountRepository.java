package com.project.q_authent.repositories;
import com.project.q_authent.models.sqls.Account;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailOrUsername(String email, String username);

    @Query("SELECT COUNT(u) FROM Account u " +
            "WHERE u.rootId = :rootId " +
            "AND (u.username = :username OR u.email = :email)")
    int countByRootIdAndUsernameOrEmail(@Param("rootId") String rootId,
                                         @Param("username") String username,
                                         @Param("email") String email);

    List<Account> findAllByParentIdAndDelFlag(String parentId, Boolean delFlag);
}

