package com.nter.projectg.service;

import com.nter.projectg.model.web.RoleModel;
import com.nter.projectg.model.web.UserModel;
import com.nter.projectg.repository.RoleRepository;
import com.nter.projectg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserModel findByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public UserModel save(UserModel user) {
        // Encode password
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        // TODO Assign role
        RoleModel userRole = roleRepository.findByRole("ADMIN");
        user.setRoles(new HashSet<RoleModel>(Collections.singletonList(userRole)));

        return userRepository.save(user);
    }

}
