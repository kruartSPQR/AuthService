package com.innowise.authenticationService.mapper;

import com.innowise.authenticationService.dto.UserCredentialsDto;
import com.innowise.authenticationService.entity.UserCredentials;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface
UserCredentialsDtoMapper {
    UserCredentialsDto toDto(UserCredentials user);

}
