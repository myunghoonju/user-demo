package org.edu.user_demo.domain;

import lombok.Getter;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.PhoneNumber;

import java.time.LocalDateTime;

@Getter
public class WithdrawnMember {

    private Long id;

    private Long originalMemberId;

    private Email email;

    private String name;

    private PhoneNumber phoneNumber;

    private LocalDateTime withdrawnAt;

    private LocalDateTime scheduledDeletionAt;

    private WithdrawnMember(Long originalMemberId,
                            Email email,
                            String name,
                            PhoneNumber phoneNumber) {
        this.originalMemberId = originalMemberId;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.withdrawnAt = LocalDateTime.now();
        this.scheduledDeletionAt = this.withdrawnAt.plusYears(1);
    }

    public static WithdrawnMember of(Long originalMemberId,
                                     Email email,
                                     String name,
                                     PhoneNumber phoneNumber) {
        if (originalMemberId == null) {
            throw new IllegalArgumentException("원본 회원 ID는 필수입니다.");
        }

        return new WithdrawnMember(originalMemberId, email, name, phoneNumber);
    }
}
