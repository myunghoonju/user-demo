package org.edu.user_demo.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.edu.user_demo.adapter.out.persistence.config.PiiAttributeConverter;
import org.edu.user_demo.domain.WithdrawnMember;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.PhoneNumber;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawn_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawnMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long originalMemberId;

    @Column(nullable = false)
    private String email;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String name;

    @Convert(converter = PiiAttributeConverter.class)
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDateTime withdrawnAt;

    @Column(nullable = false)
    private LocalDateTime scheduledDeletionAt;

    public static WithdrawnMemberJpaEntity from(WithdrawnMember domain) {
        WithdrawnMemberJpaEntity entity = new WithdrawnMemberJpaEntity();
        entity.originalMemberId = domain.getOriginalMemberId();
        entity.email = domain.getEmail().getValue();
        entity.name = domain.getName();
        entity.phoneNumber = domain.getPhoneNumber().getValue();
        entity.withdrawnAt = domain.getWithdrawnAt();
        entity.scheduledDeletionAt = domain.getScheduledDeletionAt();
        return entity;
    }

    public WithdrawnMember toDomain() {
        return WithdrawnMember.of(
                this.originalMemberId,
                Email.of(this.email),
                this.name,
                PhoneNumber.of(this.phoneNumber)
        );
    }
}
