package com.innowise.authenticationService.repository;

import com.innowise.authenticationService.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials,Long> {

    UserCredentials findByEmail(String email);
}
