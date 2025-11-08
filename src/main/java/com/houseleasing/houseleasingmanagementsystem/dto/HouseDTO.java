package com.houseleasing.houseleasingmanagementsystem.dto;

import com.houseleasing.houseleasingmanagementsystem.model.enums.HouseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HouseDTO {
    // Getters and Setters
    private Long id;

    @NotBlank(message = "房屋地址不能为空")
    private String address;

    private String houseType;

    @Positive(message = "面积必须大于0")
    private Double area;

    @NotNull(message = "租金不能为空")
    @Positive(message = "租金必须大于0")
    private Double rent;

    private String decoration;
    private String facilities;

    @NotNull(message = "房源状态不能为空")
    private HouseStatus status;

    private String description;
    private Boolean recommended;
    private Long landlordId;

}


