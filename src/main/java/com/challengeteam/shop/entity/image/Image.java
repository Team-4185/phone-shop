package com.challengeteam.shop.entity.image;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
@Table(name = "images")
public class Image extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String minioKey;

    @Column(nullable = false)
    private Long size;

    @ManyToOne
    @JoinColumn(name = "fk_mime_type_id", nullable = false)
    private MIMEType mimeType;


    public Image() {
        super();
    }

    @Override
    public String toString() {
        return "Image{" +
               "name='" + name + '\'' +
               ", minioKey='" + minioKey + '\'' +
               ", size=" + size +
               ", mimeType=" + mimeType +
               "} " + super.toString();
    }

}
