package com.nter.projectg.service;

import com.nter.projectg.model.web.UserModel;

public interface UserService {

    // TODO Optional<UserModel>
    UserModel findByName(String name);

    UserModel save(UserModel user);

}
