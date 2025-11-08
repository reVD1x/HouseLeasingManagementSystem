package com.houseleasing.houseleasingmanagementsystem.dto;

import com.houseleasing.houseleasingmanagementsystem.model.enums.ReviewType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;

    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    @NotNull(message = "评价人ID不能为空")
    private Long reviewerId;

    @NotNull(message = "被评价人ID不能为空")
    private Long revieweeId;

    @NotNull(message = "评价类型不能为空")
    private ReviewType reviewType;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;

    @Size(max = 2000, message = "评价内容不能超过2000字")
    private String comment;
}

