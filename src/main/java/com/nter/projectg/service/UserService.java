package com.nter.projectg.service;


import com.nter.projectg.model.web.UserModel;

public interface UserService {
    UserModel findUserByName(String email);

    UserModel saveUser(UserModel user);
}