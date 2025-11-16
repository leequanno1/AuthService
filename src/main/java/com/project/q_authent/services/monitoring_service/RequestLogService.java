package com.project.q_authent.services.monitoring_service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RequestLogService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long TTL_SECONDS = 7 * 24 * 3600; // 1 tuần

    public void recordRequest(String logType, String poolId) {
        String key = "request_log:" + logType + poolId;
        long timestamp = System.currentTimeMillis();
        String value = UUID.randomUUID().toString();

        // Lưu vào sorted set
        redisTemplate.opsForZSet().add(key, value, timestamp);

        // Đặt thời gian sống 1 tuần
        redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));
    }

    public LogRecords getRequestHistogram(String logType, String poolId, long timeStart, long timeEnd) {
        String key = "request_log:" + logType + poolId;
        LogRecords records = new LogRecords(new ArrayList<>());

        long interval = 30000; // 30 seconds in ms

        for (long t = timeStart; t < timeEnd; t += interval) {
            long bucketEnd = Math.min(t + interval, timeEnd);
            Long count = redisTemplate.opsForZSet().count(key, t, bucketEnd - 1);
            String timeString = toStringTime(t);
            records.addLogRecord(new LogRecord(timeString, count == null ? 0L : count));
        }
        return records;
    }

    private String toStringTime(long time) {
        return Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
