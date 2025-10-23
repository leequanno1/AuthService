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

//    @Query("SELECT COUNT(u) FROM Account u " +
//            "WHERE u.rootId = :rootId " +
//            "AND (u.username = :username OR u.email = :email) ")
//    int countByRootIdAndUsernameOrEmail(@Param("rootId") String rootId,
//                                                 @Param("username") String username,
//                                                 @Param("email") String email);

    List<Account> findAllByParentIdAndDelFlag(String parentId, Boolean delFlag);

    long countAccountsByRootIdAndEmail(String rootId, String email);

    long countAccountsByRootIdAndUsername(String rootId, String username);

    @Query("SELECT COUNT(u) FROM Account u " +
            "WHERE u.rootId IS NULL " +
            "AND u.email = :email " +
            "AND u.active = true ")
    long countAccountsByRootIdIsNullAndEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM Account u " +
            "WHERE u.rootId IS NULL " +
            "AND u.username = :username " +
            "AND u.active = true ")
    long countAccountsByRootIdIsNullAndUsername(@Param("username") String username);

    Optional<List<Account>> findAllByRootIdAndUsername(String rootId, String username);

    Optional<List<Account>> findAllByRootIdAndEmail(String rootId, String email);
}

