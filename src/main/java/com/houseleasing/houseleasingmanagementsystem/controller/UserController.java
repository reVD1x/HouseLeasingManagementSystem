package com.houseleasing.houseleasingmanagementsystem.controller;

import com.houseleasing.houseleasingmanagementsystem.dto.UserDTO;
import com.houseleasing.houseleasingmanagementsystem.model.User;
import com.houseleasing.houseleasingmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setRealName(dto.getRealName());
        user.setIdCard(dto.getIdCard());
        user.setPhone(dto.getPhone());
        return user;
    }

    // 分页获取所有用户
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    // 创建用户
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User created = userService.createUser(user);
        return ResponseEntity.ok(created);
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User updated = userService.updateUser(id, user);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // 根据真实姓名精确查询（路径参数）
    @GetMapping("/by-realname/{realName}")
    public ResponseEntity<List<User>> getByRealName(@PathVariable String realName) {
        List<User> users = userService.getByRealName(realName);
        return ResponseEntity.ok(users);
    }

    // 根据身份证号查询
    @GetMapping("/by-idcard/{idCard}")
    public ResponseEntity<User> getByIdCard(@PathVariable String idCard) {
        User user = userService.getByIdCard(idCard);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    // 分页模糊查询（按姓名关键字）
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchByRealName(
            @RequestParam(required = false) String realName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.searchByRealName(realName, pageable);
        return ResponseEntity.ok(users);
    }

    // 直接返回 List<User> 的模糊查询（兼容旧前端调用）
    @GetMapping("/search-raw")
    public ResponseEntity<List<User>> searchByRealNameRaw(@RequestParam String realName) {
        List<User> users = userService.getByRealName(realName);
        return ResponseEntity.ok(users);
    }
}
