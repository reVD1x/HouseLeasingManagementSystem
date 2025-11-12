package com.houseleasing.houseleasingmanagementsystem.dto;

import com.houseleasing.houseleasingmanagementsystem.model.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequestDTO {
    private Long id;

    @NotNull(message = "房源ID不能为空")
    private Long houseId;


    @NotBlank(message = "问题描述不能为空")
    @Size(max = 1000, message = "问题描述不能超过1000字")
    private String description;

    @Positive(message = "维修费用必须大于0")
    private Double cost;

    @NotNull(message = "维修状态不能为空")
    private MaintenanceStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    // 新增提交者姓名与联系电话字段
    private String requesterName;
    private String contact;
}
