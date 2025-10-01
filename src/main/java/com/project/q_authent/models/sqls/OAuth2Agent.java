package com.project.q_authent.models.sqls;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth2_agents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth2_cd", nullable = false, unique = true)
    private Integer oauth2Cd;

    @Column(name = "agent_name", nullable = false, unique = true, length = 100)
    private String agentName;
}
