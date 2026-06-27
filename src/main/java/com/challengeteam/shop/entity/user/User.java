package com.challengeteam.shop.entity.user;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.cart.Cart;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false)
    private Long tokenVersion = 0L;

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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    public void incrementTokenVersion() {
        tokenVersion = tokenVersion == null ? 1L : tokenVersion + 1L;
    }

    @Override
    public String toString() {
        return "User{" +
               "email='" + email + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", newCity='" + city + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", role=" + role +
               "} " + super.toString();
    }

}
