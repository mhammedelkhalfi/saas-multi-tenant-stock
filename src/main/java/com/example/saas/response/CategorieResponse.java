package com.example.saas.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorieResponse {
    private String id;
    private String name;
    private String description;
}
