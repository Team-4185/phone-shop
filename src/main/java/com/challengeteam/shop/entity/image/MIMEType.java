package com.challengeteam.shop.entity.image;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "mime_types")
public class MIMEType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "mimeType")
    private List<Image> images;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MIMEType mimeType = (MIMEType) o;

        return Objects.equals(id, mimeType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MIMEType{" +
               "id=" + id +
               ", extension='" + extension + '\'' +
               ", type='" + type + '\'' +
               '}';
    }

}
