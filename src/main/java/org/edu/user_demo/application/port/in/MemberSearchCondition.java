package org.edu.user_demo.application.port.in;

import lombok.Getter;
import org.edu.user_demo.domain.MemberRole;
import org.edu.user_demo.domain.MemberStatus;

@Getter
public class MemberSearchCondition {

    private final String name;
    private final MemberStatus status;
    private final MemberRole role;

    private MemberSearchCondition(String name, MemberStatus status, MemberRole role) {
        this.name = name;
        this.status = status;
        this.role = role;
    }

    public static MemberSearchCondition of(String name, MemberStatus status, MemberRole role) {
        return new MemberSearchCondition(name, status, role);
    }

    public static MemberSearchCondition empty() {
        return new MemberSearchCondition(null, null, null);
    }
}
