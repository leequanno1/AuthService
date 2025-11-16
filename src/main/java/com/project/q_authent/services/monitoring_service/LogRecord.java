package com.project.q_authent.services.monitoring_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class LogRecord {

    private String time;

    private Long counter;

}
