package com.project.q_authent.repositories;

import com.project.q_authent.models.nosqls.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    void deleteByPoolId(String poolId);
}
