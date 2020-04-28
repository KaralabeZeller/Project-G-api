package com.nter.projectg.service;


import com.nter.projectg.model.web.UserModel;

public interface UserService {
    public UserModel findUserByName(String email);

    public UserModel saveUser(UserModel user);
}