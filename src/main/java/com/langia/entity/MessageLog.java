package com.langia.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Data
@Entity
@Table(name = "message_log")
public class MessageLog {

    public enum MsgDirection { IN, OUT }
    public enum MsgStatus { RECEIVED, SENT, DELIVERED, READ, ERROR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MsgDirection direction;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    @Type(JsonBinaryType.class)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    private MsgStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public static MessageLog outSent(Long studentId, String payloadJson) {
        MessageLog m = new MessageLog();
        m.direction = MsgDirection.OUT;
        m.studentId = studentId;
        m.payloadJson = payloadJson;
        m.status = MsgStatus.SENT;
        return m;
    }

    public static MessageLog outError(Long studentId, String payloadJson) {
        MessageLog m = new MessageLog();
        m.direction = MsgDirection.OUT;
        m.studentId = studentId;
        m.payloadJson = payloadJson;
        m.status = MsgStatus.ERROR;
        return m;
    }
}
