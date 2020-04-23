package com.nter.projectg.repository;

import com.nter.projectg.model.web.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByName(String name);
}
