package org.edu.user_demo.adapter.in.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.user_demo.adapter.in.web.dto.MemberResponse;
import org.edu.user_demo.adapter.in.web.dto.UpdateMemberRequest;
import org.edu.user_demo.application.port.in.ManageMemberUseCase;
import org.edu.user_demo.application.port.in.UpdateMemberCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ManageMemberUseCase manageMemberUseCase;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(MemberResponse.from(manageMemberUseCase.getMyInfo(memberId)));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMyInfo(@AuthenticationPrincipal Long memberId,
                                             @Valid @RequestBody UpdateMemberRequest request) {
        manageMemberUseCase.updateMyInfo(memberId, new UpdateMemberCommand(request.getName(), request.getPhoneNumber()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal Long memberId,
                                         @RequestHeader("Authorization") String authorization) {
        manageMemberUseCase.withdraw(memberId, authorization.substring(BEARER_PREFIX.length()));
        return ResponseEntity.noContent().build();
    }
}
