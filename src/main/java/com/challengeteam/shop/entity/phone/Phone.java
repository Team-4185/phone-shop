package com.challengeteam.shop.entity.phone;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "phones")
public class Phone extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Integer releaseYear;

    public Phone() { super(); }

    @Override
    public String toString() {
        return "Phone{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", brand='" + brand + '\'' +
                ", releaseYear='" + releaseYear + '\'' +
                "} " + super.toString();
    }
}
