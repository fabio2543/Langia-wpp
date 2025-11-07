package com.langia.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "message_log")
@Data                      // ✅ gera automaticamente getters/setters/toString/hashCode/equals
@NoArgsConstructor          // ✅ construtor padrão
@AllArgsConstructor         // ✅ construtor completo
public class MessageLog {

    public enum Direction { IN, OUT }
    public enum Status { RECEIVED, SENT, DELIVERED, READ, ERROR }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(name = "student_id")
    private Long studentId;

    @JdbcTypeCode(SqlTypes.JSON)              // <- chave para JSONB
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;


}
