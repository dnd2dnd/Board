package com.dnd.board.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@Table(name="memeber")
public class member extends BaseTime{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    private UUID memberId;

    @Column(name="id")
    private String id;

    @Column(name="password")
    private String password;

    @Column(name="nickname")
    private String nickname;
}
