package com.project.q_authent.models.nosqls;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request_logs")
public class RequestLog {

    @Id
    private String logId;

    @Indexed
    private String poolId;

    @Indexed
    private Instant timestamp;

    private String endpoint;

    private String method;

    private String logContent;

    private MetaData meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private String ip;
        private String userAgent;
    }
}


