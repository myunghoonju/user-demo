package org.edu.user_demo.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.edu.user_demo.adapter.in.web.dto.MemberResponse;
import org.edu.user_demo.application.port.in.AdminMemberUseCase;
import org.edu.user_demo.application.port.in.MemberSearchCondition;
import org.edu.user_demo.domain.MemberRole;
import org.edu.user_demo.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminMemberUseCase adminMemberUseCase;

    @GetMapping
    public ResponseEntity<Page<MemberResponse>> getMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MemberStatus status,
            @RequestParam(required = false) MemberRole role,
            @PageableDefault Pageable pageable) {
        MemberSearchCondition condition = MemberSearchCondition.of(name, status, role);
        return ResponseEntity.ok(adminMemberUseCase.getMembers(condition, pageable).map(MemberResponse::from));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(MemberResponse.from(adminMemberUseCase.getMember(memberId)));
    }

}
