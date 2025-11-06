package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.List;

@Repository
public interface HouseRepository extends JpaRepository<House, Long> {
    @Query("SELECT h FROM House h WHERE " +
            "(:address IS NULL OR h.address LIKE %:address%) AND " +
            "(:houseType IS NULL OR h.houseType = :houseType) AND " +
            "(:minRent IS NULL OR h.rent >= :minRent) AND " +
            "(:maxRent IS NULL OR h.rent <= :maxRent) AND " +
            "h.status = :status")
    Page<House> findByConditions(@Param("address") String address,
                             @Param("houseType") String houseType,
                             @Param("minRent") Double minRent,
                             @Param("maxRent") Double maxRent,
                             @Param("status") HouseStatus status,
                             Pageable pageable);

    List<House> findByRecommendedTrue();


}

