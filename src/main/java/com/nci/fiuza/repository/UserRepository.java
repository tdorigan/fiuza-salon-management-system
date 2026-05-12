package com.nci.fiuza.repository;

import com.nci.fiuza.domain.Role;
import com.nci.fiuza.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//extends JpaRepository to get methods to easily access data such as CRUD operations
public interface UserRepository extends JpaRepository<User, Long> {
    //this is like doing a select * from users where email = XXXX
    //it returns optional because user may or may not be found with given email
    Optional<User> findByEmail(String email);

    //this is like doing a select * from users where role = XXXX
    List<User> findByRole(Role role);

//    i can use method names that matches select statements by my columns, for example findByRoleOrderByFullNameAsc
//    or i can use @Query and provide the select query
//    i can find doc about possible names on Derived Query Methods Spring Data JPA docs
}
