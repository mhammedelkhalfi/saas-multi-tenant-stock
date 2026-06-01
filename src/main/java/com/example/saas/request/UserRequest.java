package com.example.saas.request;

import com.example.saas.enums.UserRole;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
}
