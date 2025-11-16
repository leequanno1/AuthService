package com.project.q_authent.repositories;

import com.project.q_authent.models.nosqls.SessionToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionTokenRepository extends MongoRepository<SessionToken, String> {

}
