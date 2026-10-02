package com.agroconnect.dto;
import lombok.Data;
@Data
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private Boolean blocked;
}
