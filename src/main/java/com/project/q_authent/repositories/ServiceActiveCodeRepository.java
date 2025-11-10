package com.project.q_authent.repositories;

import com.project.q_authent.models.nosqls.ActiveCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceActiveCodeRepository extends MongoRepository<ActiveCode,String> {
    Optional<ActiveCode> findByUserId(String userId);

    List<ActiveCode> findAllByUserPoolIdAndEmailAndType(String userPoolId, String email, Integer type);
    Optional<ActiveCode> findByUserPoolIdAndEmailAndType(String userPoolId, String email, Integer type);
    List<ActiveCode> findAllByUserIdAndType(String userId, Integer type);
    Optional<ActiveCode> findByUserIdAndTypeAndCode(String userId, Integer type, String code);

    void deleteAllByUserIdAndType(String userId, Integer type);
}
