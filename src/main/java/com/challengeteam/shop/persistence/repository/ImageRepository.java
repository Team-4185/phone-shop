package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> getImagesByPhone_Id(Long phoneId);

    Optional<Image> findFirstByPhone_IdOrderByIdAsc(Long phoneId);

}
