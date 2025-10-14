package com.challengeteam.shop.entity.user;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
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

    public User(Long id,
                String email,
                String password,
                String firstName,
                String lastName,
                String city,
                String phoneNumber,
                Role role) {
        super(id);
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.role = role;
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
