package com.challengeteam.shop.entity.cart;

import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @ManyToOne
    @JoinColumn(nullable = false, name = "fk_variant_id")
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer amount;

}
