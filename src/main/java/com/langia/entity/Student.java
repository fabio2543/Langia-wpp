package com.langia.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String phone;

    private String language;
    private String timezone;
    private String source;

    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    private boolean active = true; // 🔹 adiciona o campo que faltava
}
