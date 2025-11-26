package com.challengeteam.shop.entity.phone;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.image.Image;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @OneToMany(mappedBy = "phone")
    private List<Image> images;

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
