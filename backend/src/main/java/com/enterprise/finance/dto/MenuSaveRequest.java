package com.enterprise.finance.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class MenuSaveRequest {

    private Long parentId;

    @NotBlank(message = "Menu name is required")
    @Size(max = 50, message = "Menu name length must be less than 50")
    private String menuName;

    @Size(max = 200, message = "Path length must be less than 200")
    private String path;

    @Size(max = 100, message = "Permission length must be less than 100")
    private String permission;

    @Size(max = 50, message = "Icon length must be less than 50")
    private String icon;

    private Integer sort;

    @NotNull(message = "Menu type is required")
    @Min(value = 0, message = "Menu type must be 0, 1 or 2")
    @Max(value = 2, message = "Menu type must be 0, 1 or 2")
    private Integer type;
}
