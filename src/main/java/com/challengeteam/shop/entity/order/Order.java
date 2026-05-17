package com.challengeteam.shop.entity.order;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.shipping.ShippingAddress;
import com.challengeteam.shop.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Root order aggregate persisted for admin Order Management workflows.
 */
@NamedEntityGraph(
        name = "Order.withItems",
        attributeNodes = {
                @NamedAttributeNode(value = "items", subgraph = "items.phone")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "items.phone",
                        attributeNodes = {
                                @NamedAttributeNode("phone")
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity(name = "CustomerOrder")
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "fk_user_id")
    private User user;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = true)
    private String customerFirstName;

    @Column(nullable = true)
    private String customerLastName;

    @Column(nullable = true)
    private String customerPhoneNumber;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod deliveryMethod;

    @Embedded
    private ShippingAddress shippingAddress;

    @Embedded
    @Column(nullable = false)
    private PaymentDetails paymentDetails;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}