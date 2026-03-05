package com.challengeteam.shop.entity.user;

import com.challengeteam.shop.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    private String name;

    @OneToMany(mappedBy = "role")
    private Set<User> users;

    @Override
    public String toString() {
        return "Role{" +
               "name='" + name + '\'' +
               "} " + super.toString();
    }

}
