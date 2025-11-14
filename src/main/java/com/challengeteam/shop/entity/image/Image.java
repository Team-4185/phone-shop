package com.challengeteam.shop.entity.image;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "images")
public class Image extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private Long size;

    @ManyToOne
    @JoinColumn(name = "fk_mime_type_id", nullable = false)
    private MIMEType mimeType;

    @Override
    public String toString() {
        return "Image{" +
               "name='" + name + '\'' +
               ", storageKey='" + storageKey + '\'' +
               ", size=" + size +
               ", mimeType=" + mimeType +
               "} " + super.toString();
    }

}
