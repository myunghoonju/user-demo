package org.edu.user_demo.application.port.in;

import lombok.Getter;

@Getter
public class UpdateMemberCommand {

    private final String name;
    private final String phoneNumber;

    public UpdateMemberCommand(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
