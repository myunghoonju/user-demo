package org.edu.user_demo.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.edu.user_demo.adapter.out.persistence.config.JpaConfig;
import org.edu.user_demo.application.port.in.MemberSearchCondition;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.MemberRole;
import org.edu.user_demo.domain.MemberStatus;
import org.edu.user_demo.domain.vo.Email;
import org.edu.user_demo.domain.vo.Password;
import org.edu.user_demo.domain.vo.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "encryption.secret-key=0123456789abcdef0123456789abcdef"
})
@Import({JpaConfig.class, MemberPersistenceAdapter.class})
class MemberSearchAdapterTest {

    @Autowired
    private MemberPersistenceAdapter memberPersistenceAdapter;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        saveMember("hong@example.com", "홍길동", "01011111111", MemberRole.USER, MemberStatus.ACTIVE);
        saveMember("kim@example.com", "김철수", "01022222222", MemberRole.USER, MemberStatus.ACTIVE);
        saveMember("lee@example.com", "이영희", "01033333333", MemberRole.ADMIN, MemberStatus.ACTIVE);
        saveMember("park@example.com", "박민준", "01044444444", MemberRole.USER, MemberStatus.WITHDRAWN);
        entityManager.flush();
        entityManager.clear();
    }

    private void saveMember(String email, String name, String phone, MemberRole role, MemberStatus status) {
        Member member = Member.create(
                Email.of(email),
                Password.of("Password1!"),
                name,
                PhoneNumber.of(phone)
        );
        member.assignRole(role);
        member.assignStatus(status);
        memberPersistenceAdapter.save(member);
    }

    @Test
    void 조건_없이_전체_조회() {
        Page<Member> result = memberPersistenceAdapter.search(MemberSearchCondition.empty(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    void 이름_부분일치_검색() {
        Page<Member> result = memberPersistenceAdapter.search(
                MemberSearchCondition.of("홍", null, null), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("홍길동");
    }

    @Test
    void 상태_필터_검색() {
        Page<Member> result = memberPersistenceAdapter.search(
                MemberSearchCondition.of(null, MemberStatus.WITHDRAWN, null), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("박민준");
    }

    @Test
    void 역할_필터_검색() {
        Page<Member> result = memberPersistenceAdapter.search(
                MemberSearchCondition.of(null, null, MemberRole.ADMIN), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("이영희");
    }

    @Test
    void 이름_상태_복합_검색() {
        Page<Member> result = memberPersistenceAdapter.search(
                MemberSearchCondition.of("김", MemberStatus.ACTIVE, null), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("김철수");
    }
}
