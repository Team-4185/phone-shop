package com.challengeteam.shop.entity.phone;

import com.challengeteam.shop.entity.BaseEntity;
import com.challengeteam.shop.entity.image.Image;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
        name = "phones",
        uniqueConstraints = @UniqueConstraint(name = "uk_phones_sku", columnNames = "sku")
)
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

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Embedded
    private PhoneCharacteristics phoneCharacteristics;

    @OneToMany(mappedBy = "phone")
    private List<Image> images;

    @OneToMany(mappedBy = "phone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @Override
    public String toString() {
        return "Phone{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", brandName='" + brand + '\'' +
                ", releaseYear='" + releaseYear + '\'' +
                ", sku='" + sku + '\'' +
                ", stock='" + stock +
                ", status='" + status +
                ", phoneCharacteristics='" + phoneCharacteristics + '\'' +
                "} " + super.toString();
    }
}
