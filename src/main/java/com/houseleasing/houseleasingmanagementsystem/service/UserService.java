package com.houseleasing.houseleasingmanagementsystem.service;

import com.houseleasing.houseleasingmanagementsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<User> getAllUsers(Pageable pageable);

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    // 精确根据真实姓名查询
    List<User> getByRealName(String realName);

    // 分页模糊查询（按姓名关键字）
    Page<User> searchByRealName(String realName, Pageable pageable);

    User getByIdCard(String idCard);
}
