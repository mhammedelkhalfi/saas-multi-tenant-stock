package com.example.saas.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorieRequest {
    private String name;
    private String description;
}
