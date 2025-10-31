package com.challengeteam.shop.entity.cart;

import com.challengeteam.shop.entity.phone.Phone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "carts_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "fk_cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(nullable = false, name = "fk_phone_id")
    private Phone phone;

    @Column(nullable = false)
    private Integer amount;

    public CartItem() {

    }
}
