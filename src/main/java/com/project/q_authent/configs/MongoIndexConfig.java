package com.project.q_authent.configs;

import com.project.q_authent.models.nosqls.RequestLog;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Configuration
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mappingContext;

    public MongoIndexConfig(MongoTemplate mongoTemplate, MongoMappingContext mappingContext) {
        this.mongoTemplate = mongoTemplate;
        this.mappingContext = mappingContext;
    }

    @PostConstruct
    public void initIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps("request_logs");

        Index ttlIndex = new Index()
                .on("timestamp", org.springframework.data.domain.Sort.Direction.ASC)
                .expire(Duration.ofDays(7));

        indexOps.createIndex(ttlIndex);

        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        resolver.resolveIndexFor(RequestLog.class).forEach(indexOps::createIndex);
    }
}

