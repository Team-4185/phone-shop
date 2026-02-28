package com.challengeteam.shop.entity.order;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "status", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "address", nullable = false, length = 500)
    private String destination;

    @Embedded
    private Recipient recipient;

    @Embedded
    private PaymentDetail paymentDetail;

    @Column(name = "processed_by_webhook", nullable = false)
    private boolean processedByWebhook;

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn(name = "fk_user_id", nullable = true)
    private User user;

    @Override
    public String toString() {
        return "Order{" +
               "totalPrice=" + totalPrice +
               ", status=" + status +
               ", address='" + destination + '\'' +
               ", recipient=" + recipient +
               ", paymentDetail=" + paymentDetail +
               ", orderItems=" + orderItems +
               ", user.id=" + (user != null ? user.getId() : "null") +
               ", processedByWebhook=" + processedByWebhook +
               "} " + super.toString();
    }

}
