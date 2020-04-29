package com.nter.projectg.service;

import com.nter.projectg.model.web.UserModel;

public interface UserService {

    // TODO Optional<UserModel>
    UserModel findUserByName(String email);

    UserModel saveUser(UserModel user);

}
