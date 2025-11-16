package com.project.q_authent.services.monitoring_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class LogRecords {

    private List<LogRecord> logRecords;

    public void addLogRecord(LogRecord logRecord) {
        logRecords.add(logRecord);
    }

}
