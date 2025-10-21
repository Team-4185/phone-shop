package com.challengeteam.shop.entity.cart;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @OneToOne
    @JoinColumn(nullable = false, name = "fk_user_id")
    private User user;

    private BigDecimal totalPrice;

    public Cart() {
        super();
    }

    @Override
    public String toString() {
        return "Cart{" +
                "userId='" + user.getId() + '\'' +
                ", totalPrice='" + totalPrice + '\'' +
                "} " + super.toString();
    }

}
