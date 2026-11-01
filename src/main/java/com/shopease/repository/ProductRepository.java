package com.shopease.repository;

import com.shopease.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findCategories();

    long countByExternalIdIsNotNull();

    List<Product> findByExternalIdIn(Collection<Long> externalIds);

    void deleteByExternalIdIsNull();

    List<Product> findTop8ByOrderByIdDesc();

    List<Product> findTop8ByRatingNotNullOrderByRatingDesc();
}
