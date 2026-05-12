package com.nci.fiuza.repository;

import com.nci.fiuza.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//extends JpaRepository to get methods to easily access data such as CRUD operations
public interface ProductRepository extends JpaRepository<Product, Long> {

    //this is like a select * from products where active = true
    List<Product> findByActiveTrue();

}
