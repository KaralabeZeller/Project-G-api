package com.nter.projectg.service;

import com.nter.projectg.model.web.RoleModel;
import com.nter.projectg.model.web.UserModel;
import com.nter.projectg.repository.RoleRepository;
import com.nter.projectg.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserModel findUserByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public UserModel saveUser(UserModel user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        RoleModel userRole = roleRepository.findByRole("ADMIN");
        user.setRoles(new HashSet<RoleModel>(Arrays.asList(userRole)));
        return userRepository.save(user);
    }
}