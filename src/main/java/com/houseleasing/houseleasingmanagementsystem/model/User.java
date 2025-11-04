package com.houseleasing.houseleasingmanagementsystem.model;

import com.houseleasing.houseleasingmanagementsystem.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;         // 用户名

    @Column(nullable = false)
    private String password;         // 密码

    private String realName;         // 真实姓名

    private String idCard;           // 身份证号

    private String phone;            // 联系方式

    @Enumerated(EnumType.STRING)
    private UserRole role;           // 角色(管理员、用户)

    @CreatedDate
    private LocalDateTime createdAt;
}
