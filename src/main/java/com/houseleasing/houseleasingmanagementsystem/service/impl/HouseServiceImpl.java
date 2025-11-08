package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.House;
import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import com.houseleasing.houseleasingmanagementsystem.repository.HouseRepository;
import com.houseleasing.houseleasingmanagementsystem.service.HouseService;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private HouseRepository houseRepository;

    @Override
    public Page<House> getAllHouses(Pageable pageable) {
        return houseRepository.findAll(pageable);
    }

    @Override
    public House getHouseById(Long id) {
        Optional<House> house = houseRepository.findById(id);
        return house.orElse(null);
    }

    @Override
    public House createHouse(House house) {
        return houseRepository.save(house);
    }

    @Override
    public House updateHouse(Long id, House houseDetails) {
        House house = getHouseById(id);
        if (house != null) {
            house.setAddress(houseDetails.getAddress());
            house.setHouseType(houseDetails.getHouseType());
            house.setArea(houseDetails.getArea());
            house.setRent(houseDetails.getRent());
            house.setDecoration(houseDetails.getDecoration());
            house.setFacilities(houseDetails.getFacilities());
            house.setDescription(houseDetails.getDescription());
            house.setStatus(houseDetails.getStatus());
            house.setRecommended(houseDetails.getRecommended());
            // Only update landlord if provided to avoid accidental null FK
            if (houseDetails.getLandlord() != null) {
                house.setLandlord(houseDetails.getLandlord());
            }
            return houseRepository.save(house);
        }
        return null;
    }

    @Override
    public void deleteHouse(Long id) {
        houseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<House> searchHouses(String address, String houseType, Double minArea, Double maxArea,
                                    Double minRent, Double maxRent, String decoration, String facilities,
                                    HouseStatus status, Boolean recommended, Long landlordId, String landlordName,
                                    Pageable pageable) {

        Specification<House> spec = (root, query, cb) -> {
            // Ensure fetch join only applied to root queries (not count query)
            if (House.class.equals(query.getResultType())) {
                root.fetch("landlord", JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (address != null && !address.isBlank()) {
                predicates.add(cb.like(root.get("address"), "%" + address + "%"));
            }
            if (houseType != null && !houseType.isBlank()) {
                predicates.add(cb.equal(root.get("houseType"), houseType));
            }
            if (minArea != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("area"), minArea));
            }
            if (maxArea != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("area"), maxArea));
            }
            if (minRent != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rent"), minRent));
            }
            if (maxRent != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rent"), maxRent));
            }
            if (decoration != null && !decoration.isBlank()) {
                predicates.add(cb.equal(root.get("decoration"), decoration));
            }
            if (facilities != null && !facilities.isBlank()) {
                predicates.add(cb.like(root.get("facilities"), "%" + facilities + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (recommended != null) {
                predicates.add(cb.equal(root.get("recommended"), recommended));
            }
            // landlord filters
            if (landlordId != null) {
                predicates.add(cb.equal(root.get("landlord").get("id"), landlordId));
            }
            if (landlordName != null && !landlordName.isBlank()) {
                predicates.add(cb.like(root.get("landlord").get("realName"), "%" + landlordName + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return houseRepository.findAll(spec, pageable);
    }

    @Override
    public List<House> findByRentBetween(Double minRent, Double maxRent) {
        return houseRepository.findByRentBetween(minRent, maxRent);
    }

    @Override
    public List<House> getRecommendedHouses() {
        return houseRepository.findByRecommendedTrue();
    }

    @Override
    public House toggleRecommendation(Long id) {
        House house = getHouseById(id);
        if (house != null) {
            house.setRecommended(!house.getRecommended());
            return houseRepository.save(house);
        }
        return null;
    }
}
