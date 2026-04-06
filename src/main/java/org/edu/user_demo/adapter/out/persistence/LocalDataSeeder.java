package org.edu.user_demo.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.application.service.JwtTokenProvider;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.MemberRole;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 개발 환경 초기 데이터 시더.
 * spring.profiles.active=local 일 때만 실행됩니다.
 *
 * 삽입 계정:
 *   ADMIN  admin@example.com    / AdminPassword1!
 *   USER   user1@example.com    / UserPassword1!
 *   USER   user2@example.com    / UserPassword1!
 *   USER   user3@example.com    / UserPassword1!
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder implements ApplicationRunner {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedUsers();
    }

    private void seedAdmin() {
        String email = "admin@example.com";
        if (loadMemberPort.existsByEmail(email)) {
            log.info("[Seeder] 관리자 계정 이미 존재 — 건너뜀: {}", email);
            return;
        }

        Member admin = Member.create(
                Email.of(email),
                Password.ofEncoded(jwtTokenProvider.encodePassword("AdminPassword1!")),
                "관리자",
                PhoneNumber.of("01000000000")
        );
        admin.assignRole(MemberRole.ADMIN);
        saveMemberPort.save(admin);
        log.info("[Seeder] 관리자 계정 생성 완료: {}", email);
    }

    private void seedUsers() {
        record UserSeed(String email, String name, String phone) {}

        UserSeed[] users = {
            new UserSeed("user1@example.com", "홍길동", "01011111111"),
            new UserSeed("user2@example.com", "김철수", "01022222222"),
            new UserSeed("user3@example.com", "이영희", "01033333333"),
        };

        for (UserSeed seed : users) {
            if (loadMemberPort.existsByEmail(seed.email())) {
                log.info("[Seeder] 사용자 계정 이미 존재 — 건너뜀: {}", seed.email());
                continue;
            }
            Member user = Member.create(
                    Email.of(seed.email()),
                    Password.ofEncoded(jwtTokenProvider.encodePassword("UserPassword1!")),
                    seed.name(),
                    PhoneNumber.of(seed.phone())
            );
            saveMemberPort.save(user);
            log.info("[Seeder] 사용자 계정 생성 완료: {}", seed.email());
        }
    }
}
