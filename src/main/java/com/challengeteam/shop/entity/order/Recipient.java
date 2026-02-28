package com.challengeteam.shop.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Recipient {
    @Column(name = "recipient_firstname", nullable = false, length = 255)
    private String firstname;

    @Column(name = "recipient_lastname", nullable = false, length = 255)
    private String lastname;

    @Column(name = "recipient_email", nullable = false, length = 100)
    private String email;

    @Column(name = "recipient_phone", nullable = false, length = 13)
    private String phone;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recipient recipient = (Recipient) o;

        return Objects.equals(firstname, recipient.firstname)
               && Objects.equals(lastname, recipient.lastname)
               && Objects.equals(email, recipient.email)
               && Objects.equals(phone, recipient.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, email, phone);
    }

    @Override
    public String toString() {
        return "Recipient{" +
               "firstname='" + firstname + '\'' +
               ", lastname='" + lastname + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               '}';
    }
}
