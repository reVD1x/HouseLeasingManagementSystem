package com.houseleasing.houseleasingmanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDTO {
    // Getters and Setters
    private Long id;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "联系方式不能为空")
    private String phone;

}
