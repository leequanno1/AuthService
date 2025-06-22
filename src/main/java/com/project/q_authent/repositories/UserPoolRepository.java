package com.project.q_authent.repositories;

import com.project.q_authent.models.sqls.UserPool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPoolRepository extends JpaRepository<UserPool, String> {
}
