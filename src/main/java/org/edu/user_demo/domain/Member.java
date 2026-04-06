package org.edu.user_demo.domain;

import lombok.Getter;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;

@Getter
public class Member {

    private Long id;

    private Email email;

    private Password password;

    private String name;

    private PhoneNumber phoneNumber;

    private MemberRole role;

    private MemberStatus status;

    private Member(Email email,
                   Password password,
                   String name,
                   PhoneNumber phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = MemberRole.USER;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member create(Email email, Password password, String name, PhoneNumber phoneNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        return new Member(email, password, name, phoneNumber);
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void assignRole(MemberRole role) {
        this.role = role;
    }

    public void assignStatus(MemberStatus status) {
        this.status = status;
    }

    public void update(String name, PhoneNumber phoneNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }

        this.status = MemberStatus.WITHDRAWN;
    }
}
