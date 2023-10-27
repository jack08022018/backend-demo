package com.jpa.service;

import com.jpa.entity.ProductEntity;
import org.springframework.data.domain.Page;

public interface ApiService {
    Page<ProductEntity> getProductList(ProductEntity dto);

}
