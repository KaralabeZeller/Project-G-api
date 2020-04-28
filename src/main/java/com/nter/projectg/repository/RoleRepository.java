package com.nter.projectg.repository;

import com.nter.projectg.model.web.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    RoleModel findByRole(String role);
}
