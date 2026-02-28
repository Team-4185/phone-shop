package com.challengeteam.shop.entity.order;

import com.challengeteam.shop.entity.phone.Phone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders_phones")
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;

    @MapsId("orderId")
    @ManyToOne
    @JoinColumn(name = "fk_order_id")
    private Order order;

    @MapsId("phoneId")
    @ManyToOne
    @JoinColumn(name = "fk_phone_id")
    private Phone phone;

    @Column(name = "amount")
    private int amount;

    public OrderItem(Order order, Phone phone, int amount) {
        this.order = order;
        this.phone = phone;
        this.amount = amount;
        this.id = new OrderItemId(order.getId(), phone.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;

        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "id=" + id +
               ", order.id=" + (order != null ? order.getId().toString() : "null") +
               ", phone=" + phone +
               ", amount=" + amount +
               '}';
    }
}
