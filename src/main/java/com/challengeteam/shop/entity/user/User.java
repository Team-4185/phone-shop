package com.challengeteam.shop.entity.user;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    private String city;

    @Column(nullable = true)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(nullable = false, name = "fk_role_id")
    private Role role;


    public User() {
        super();
    }

    @Override
    public String toString() {
        return "User{" +
               "email='" + email + '\'' +
               ", password='" + password + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", newCity='" + city + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", role=" + role +
               "} " + super.toString();
    }

}
