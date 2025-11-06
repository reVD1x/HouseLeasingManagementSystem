package com.houseleasing.houseleasingmanagementsystem.service;

import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HouseService {
    Page<House> getAllHouses(Pageable pageable);
    House getHouseById(Long id);
    House createHouse(House house);
    House updateHouse(Long id, House house);
    void deleteHouse(Long id);
    Page<House> searchHouses(String address, String houseType, Double minRent, Double maxRent, HouseStatus status, Pageable pageable);
    List<House> getRecommendedHouses();
    House toggleRecommendation(Long id);
}

