package org.edu.user_demo.adapter.out.persistence;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWithdrawnMemberJpaEntity is a Querydsl query type for WithdrawnMemberJpaEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWithdrawnMemberJpaEntity extends EntityPathBase<WithdrawnMemberJpaEntity> {

    private static final long serialVersionUID = -2065726841L;

    public static final QWithdrawnMemberJpaEntity withdrawnMemberJpaEntity = new QWithdrawnMemberJpaEntity("withdrawnMemberJpaEntity");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> originalMemberId = createNumber("originalMemberId", Long.class);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final DateTimePath<java.time.LocalDateTime> scheduledDeletionAt = createDateTime("scheduledDeletionAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> withdrawnAt = createDateTime("withdrawnAt", java.time.LocalDateTime.class);

    public QWithdrawnMemberJpaEntity(String variable) {
        super(WithdrawnMemberJpaEntity.class, forVariable(variable));
    }

    public QWithdrawnMemberJpaEntity(Path<? extends WithdrawnMemberJpaEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWithdrawnMemberJpaEntity(PathMetadata metadata) {
        super(WithdrawnMemberJpaEntity.class, metadata);
    }

}

