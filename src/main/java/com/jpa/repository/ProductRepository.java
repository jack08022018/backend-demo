package com.jpa.repository;

import com.jpa.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @Query(nativeQuery = true, value = ""
            +   "SELECT A.* "
            +   "FROM PRODUCT A "
            +   "WHERE (:#{#params.name} IS NULL OR A.NAME LIKE %:#{#params.name}%) ",
            countQuery = ""
                    +   "SELECT COUNT(*) "
                    +   "FROM PRODUCT A "
                    +   "WHERE (:#{#params.name} IS NULL OR A.NAME LIKE %:#{#params.name}%) ")
    Page<ProductEntity> getProductList(@Param("params") ProductEntity params, Pageable pageable);

}
