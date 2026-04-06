package org.edu.user_demo.adapter.in.web.dto;

import lombok.Getter;
import org.edu.user_demo.domain.Member;

@Getter
public class MemberResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final String phoneNumber;
    private final String role;
    private final String status;

    private MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail().getValue();
        this.name = member.getName();
        this.phoneNumber = member.getPhoneNumber().getValue();
        this.role = member.getRole().name();
        this.status = member.getStatus().name();
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(member);
    }
}
