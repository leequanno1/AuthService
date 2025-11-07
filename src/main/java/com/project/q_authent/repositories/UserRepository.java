package com.project.q_authent.repositories;

import com.project.q_authent.models.nosqls.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    void deleteByPoolId(String poolId);

    List<User> findAllByPoolIdAndUsernameAndDelFlagAndIsValidated(String poolId, String username, Boolean delFlag, Boolean isValidated);

    List<User> findAllByPoolIdAndEmailAndDelFlagAndIsValidated(String poolId, String email, Boolean delFlag, Boolean isValidated);

    Optional<User> findUserByPoolIdAndUsernameAndIsValidatedAndDelFlag(String poolId, String username, Boolean isValidated, Boolean delFlag);

    List<User> findAllByPoolIdAndDelFlag(String poolId, Boolean delFlag);
}
