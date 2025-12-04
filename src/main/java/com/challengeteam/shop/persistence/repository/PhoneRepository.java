package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {

    @Query(value = """
            SELECT EXISTS(
                  SELECT 1
                  FROM images AS i
                  WHERE i.id = :imageId AND i.fk_phone_id = :phoneId
            );
            """, nativeQuery = true)
    boolean existsPhoneByIdWithImage(@Param("phoneId") long phoneId, @Param("imageId") long imageId);

}
