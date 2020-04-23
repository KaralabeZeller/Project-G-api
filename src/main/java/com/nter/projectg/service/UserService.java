package com.nter.projectg.service;


import com.nter.projectg.model.web.User;
import org.springframework.stereotype.Service;
public interface UserService {
    public User findUserByName(String email) ;
    public User saveUser(User user);
}