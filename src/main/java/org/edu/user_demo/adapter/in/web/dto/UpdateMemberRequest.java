package org.edu.user_demo.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMemberRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;
}
