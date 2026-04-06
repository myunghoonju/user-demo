package org.edu.user_demo.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.edu.user_demo.adapter.out.persistence.config.PiiAttributeConverter;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.MemberRole;
import org.edu.user_demo.domain.MemberStatus;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String name;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static MemberJpaEntity from(Member member) {
        MemberJpaEntity entity = new MemberJpaEntity();
        entity.id = member.getId();
        entity.email = member.getEmail().getValue();
        entity.password = member.getPassword().getValue();
        entity.name = member.getName();
        entity.phoneNumber = member.getPhoneNumber().getValue();
        entity.role = member.getRole();
        entity.status = member.getStatus();
        return entity;
    }

    public Member toDomain() {
        Member member = Member.create(
                Email.of(this.email),
                Password.ofEncoded(this.password),
                this.name,
                PhoneNumber.of(this.phoneNumber)
        );
        member.assignId(this.id);
        member.assignRole(this.role);
        member.assignStatus(this.status);
        return member;
    }
}
