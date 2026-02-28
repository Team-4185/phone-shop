package com.challengeteam.shop.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OrderItemId {
    @Column(name = "fk_order_id")
    private Long orderId;
    @Column(name = "fk_phone_id")
    private Long phoneId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemId that = (OrderItemId) o;

        return Objects.equals(orderId, that.orderId)
               && Objects.equals(phoneId, that.phoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, phoneId);
    }

    @Override
    public String toString() {
        return "OrderItemId{" +
               "orderId=" + orderId +
               ", phoneId=" + phoneId +
               '}';
    }
}
