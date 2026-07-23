package com.alivos.api.dto;

import com.alivos.api.entity.Role;
import com.alivos.api.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {
    private String id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String avatarUrl;
    private UserStatus status;
}
