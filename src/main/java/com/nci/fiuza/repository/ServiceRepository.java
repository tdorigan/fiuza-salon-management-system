package com.nci.fiuza.repository;

import com.nci.fiuza.domain.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//extends JpaRepository to get methods to easily access data such as CRUD operations
public interface ServiceRepository extends JpaRepository<Service, Long> {

    //this is like a select * from services where active = true
    List<Service> findByActiveTrue();

}
