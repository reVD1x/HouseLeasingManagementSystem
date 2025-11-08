package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.House;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseRepository extends JpaRepository<House, Long>, JpaSpecificationExecutor<House> {
    List<House> findByRecommendedTrue();

    List<House> findByRentBetween(Double minRent, Double maxRent);
}
