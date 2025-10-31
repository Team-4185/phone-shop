package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.image.MIMEType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MIMETypeRepository extends JpaRepository<MIMEType, Long> {

    Optional<MIMEType> findByExtension(String extension);

}
