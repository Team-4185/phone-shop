package com.challengeteam.shop.entity.cart;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @OneToOne
    @JoinColumn(nullable = false, name = "fk_user_id")
    private User user;

    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CartItem> cartItems;

    @Override
    public String toString() {
        return "Cart{" +
                "userId='" + user.getId() + '\'' +
                ", totalPrice='" + totalPrice + '\'' +
                "} " + super.toString();
    }

}
