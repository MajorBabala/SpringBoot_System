package com.enterprise.finance.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "Nickname is required")
    @Size(max = 50, message = "Nickname length must be less than 50")
    private String nickname;

    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email length must be less than 100")
    private String email;

    @Pattern(regexp = "^$|^[0-9]{11}$", message = "Mobile must be 11 digits")
    private String mobile;

    private Long deptId;

    private Integer status;

    private List<Long> roleIds;
}
