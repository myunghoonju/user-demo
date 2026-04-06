package org.edu.user_demo.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.edu.user_demo.application.port.in.MemberSearchCondition;
import org.edu.user_demo.application.port.out.LoadMemberPort;
import org.edu.user_demo.application.port.out.SaveMemberPort;
import org.edu.user_demo.domain.Member;
import org.edu.user_demo.domain.MemberStatus;
import org.edu.user_demo.domain.WithdrawnMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements LoadMemberPort, SaveMemberPort {

    private final MemberJpaRepository memberJpaRepository;
    private final WithdrawnMemberJpaRepository withdrawnMemberJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = memberJpaRepository.save(MemberJpaEntity.from(member));
        return entity.toDomain();
    }

    @Override
    public WithdrawnMember saveWithdrawn(WithdrawnMember withdrawnMember) {
        WithdrawnMemberJpaEntity entity = withdrawnMemberJpaRepository.save(WithdrawnMemberJpaEntity.from(withdrawnMember));
        return entity.toDomain();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email).map(MemberJpaEntity::toDomain);
    }

    @Override
    public Optional<Member> findByPhoneNumber(String phoneNumber) {
        return memberJpaRepository.findByPhoneNumber(phoneNumber).map(MemberJpaEntity::toDomain);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberJpaEntity::toDomain);
    }

    @Override
    public Page<Member> search(MemberSearchCondition condition, Pageable pageable) {
        QMemberJpaEntity qMember = QMemberJpaEntity.memberJpaEntity;
        BooleanBuilder builder = new BooleanBuilder();

        // status and role can be filtered at DB level (not encrypted)
        if (condition.getStatus() != null) {
            builder.and(qMember.status.eq(condition.getStatus()));
        }
        if (condition.getRole() != null) {
            builder.and(qMember.role.eq(condition.getRole()));
        }

        // name is AES-encrypted in DB — filter after decryption in application layer
        List<Member> candidates = queryFactory
                .selectFrom(qMember)
                .where(builder)
                .fetch()
                .stream()
                .map(MemberJpaEntity::toDomain)
                .toList();

        if (condition.getName() != null) {
            candidates = candidates.stream()
                    .filter(m -> m.getName().contains(condition.getName()))
                    .toList();
        }

        int total = candidates.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<Member> content = (start > total) ? List.of() : candidates.subList(start, end);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Member> findAllWithdrawn() {
        return memberJpaRepository.findAllByStatus(MemberStatus.WITHDRAWN)
                .stream()
                .map(MemberJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(Long memberId) {
        memberJpaRepository.deleteById(memberId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmailAndStatus(email, MemberStatus.ACTIVE);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return memberJpaRepository.existsByPhoneNumberAndStatus(phoneNumber, MemberStatus.ACTIVE);
    }
}
