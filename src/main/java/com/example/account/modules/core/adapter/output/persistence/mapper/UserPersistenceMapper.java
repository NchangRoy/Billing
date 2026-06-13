package com.example.account.modules.core.adapter.output.persistence.mapper;

import com.example.account.modules.core.domain.model.User;
import com.example.account.modules.core.adapter.output.persistence.UserPersistenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    User toDomain(UserPersistenceEntity entity);
    UserPersistenceEntity toEntity(User domain);
}
