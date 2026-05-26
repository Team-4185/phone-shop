package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String username);

    boolean existsByEmail(String username);

    long countByRole_Name(String roleName);

    @Query(
            "SELECT COUNT(u) FROM User u "
                    + "WHERE u.role.name = :roleName AND u.createdAt >= :start AND u.createdAt < :end")
    long countByRoleNameAndCreatedAtBetween(
            @Param("roleName") String roleName, @Param("start") Instant start, @Param("end") Instant end);

}
