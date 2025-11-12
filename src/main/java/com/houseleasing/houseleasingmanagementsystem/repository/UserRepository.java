package com.houseleasing.houseleasingmanagementsystem.repository;

import com.houseleasing.houseleasingmanagementsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRealName(String realName);

    List<User> findByRealNameContainingIgnoreCase(String realName);

    // 新增：分页版本
    Page<User> findByRealNameContainingIgnoreCase(String realName, Pageable pageable);

    Optional<User> findByIdCard(String idCard);
}
