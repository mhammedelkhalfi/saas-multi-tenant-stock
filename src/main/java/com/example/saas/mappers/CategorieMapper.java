package com.example.saas.mappers;

import com.example.saas.entities.Categorie;
import com.example.saas.request.CategorieRequest;
import com.example.saas.response.CategorieResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.stereotype.Service;

@Service
public class CategorieMapper {

    public Categorie toEntity(final CategorieRequest request){
        return Categorie.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deleted(false)
                .build();

    }

    public CategorieResponse toRequest(final Categorie entity){
        return CategorieResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
