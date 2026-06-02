package com.example.sclms_user.Reposotory;

import com.example.sclms_user.Model.SchoolModel;
import com.example.sclms_user.Model.enums.Role;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface userReposotory {
    Optional<SchoolModel> findByEmail(String email);
    boolean existsByEmail(String email);
    List<SchoolModel> findAllByRole(Role role);
}
