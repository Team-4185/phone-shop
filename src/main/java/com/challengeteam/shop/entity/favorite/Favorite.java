package com.challengeteam.shop.entity.favorite;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "favorites",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorites_user_phone",
                columnNames = {"fk_user_id", "fk_phone_id"}
        )
)
public class Favorite extends BaseEntity {

    @ManyToOne
    @JoinColumn(nullable = false, name = "fk_user_id")
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false, name = "fk_phone_id")
    private Phone phone;
}
