package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    boolean existsByCartIdAndPhoneId(Long cartId, Long phoneId);

    Optional<CartItem> findByCartIdAndPhoneId(Long cartId, Long phoneId);

    Optional<CartItem> findByCartId(Long cartId);

    void deleteAllByCartId(Long cartId);

}
