package com.project.q_authent.repositories;

import com.project.q_authent.models.nosqls.ActiveCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceActiveCodeRepository extends MongoRepository<ActiveCode,String> {
    Optional<ActiveCode> findByUserId(String userId);

    List<ActiveCode> findAllByUserId(String userId);
}
