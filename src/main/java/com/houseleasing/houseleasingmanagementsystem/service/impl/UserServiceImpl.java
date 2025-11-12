package com.houseleasing.houseleasingmanagementsystem.service.impl;

import com.houseleasing.houseleasingmanagementsystem.model.User;
import com.houseleasing.houseleasingmanagementsystem.repository.UserRepository;
import com.houseleasing.houseleasingmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        if (user != null) {
            user.setRealName(userDetails.getRealName());
            user.setIdCard(userDetails.getIdCard());
            user.setPhone(userDetails.getPhone());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getByRealName(String realName) {
        // 使用部分匹配并忽略大小写，便于前端按关键字搜索
        return userRepository.findByRealNameContainingIgnoreCase(realName);
    }

    @Override
    public Page<User> searchByRealName(String realName, Pageable pageable) {
        if (realName == null || realName.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByRealNameContainingIgnoreCase(realName, pageable);
    }

    @Override
    public User getByIdCard(String idCard) {
        return userRepository.findByIdCard(idCard).orElse(null);
    }
}
