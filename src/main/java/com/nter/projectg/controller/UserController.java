package com.nter.projectg.controller;

import com.nter.projectg.games.common.GameHandler;
import com.nter.projectg.games.common.util.Constants;
import com.nter.projectg.lobby.LobbyHandler;
import com.nter.projectg.model.web.UserModel;
import com.nter.projectg.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Map;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Constants constants;

    @Autowired
    private LobbyHandler lobbyHandler;

    @Autowired
    GameHandler gameFactory;

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @SuppressWarnings("SameReturnValue")
    @RequestMapping(value = {"/health"}, method = RequestMethod.GET)
    @ResponseBody
    public String health() {
        return "OK";
    }

    @RequestMapping(value = {"/join/{lobbyId}"}, method = RequestMethod.GET)
    public ModelAndView join(@PathVariable("lobbyId") String lobbyId, Map<String, Object> model) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        logger.info("Joining lobby: {}", lobbyId);
        UserModel user = userService.findUserByName(auth.getName());
        model.put("userName", user.getName());

        model.put("lobbyName", lobbyId);
        modelAndView.setViewName("user/lobby");
        return modelAndView;
    }

    @RequestMapping(value = {"/watch/{lobbyId}"}, method = RequestMethod.GET)
    public ModelAndView watch(@PathVariable("lobbyId") String lobbyId, Map<String, Object> model) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Watching lobby: {}", lobbyId);
        UserModel user = userService.findUserByName(auth.getName());
        model.put("userName", user.getName());

        model.put("lobbyName", lobbyId);
        modelAndView.setViewName("user/screen");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registration() {
        ModelAndView modelAndView = new ModelAndView();
        UserModel user = new UserModel();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/lobbies", method = RequestMethod.GET)
    public ModelAndView lobbies() {
        ModelAndView modelAndView = new ModelAndView();
        UserModel user = new UserModel();
        modelAndView.addObject("user", user);
        modelAndView.addObject("gameNames", constants.getGames());
        modelAndView.addObject("lobbies", lobbyHandler.getLobbies());
        modelAndView.setViewName("user/lobbies");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid UserModel user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        UserModel userExists = userService.findUserByName(user.getName());
        if (userExists != null) {
            bindingResult.rejectValue("name", "error.user", "There is already a user registered with the name provided");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", new UserModel());
            modelAndView.setViewName("registration");
        }
        return modelAndView;
    }

    //TODO implement create new lobby with lobbyHandler
    @RequestMapping(value = "/createLobby", method = RequestMethod.GET)
    public ModelAndView createLobby(@Valid String game, Map<String, Object> model) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Creating lobby for game: {}", game);

        if(gameFactory.gameExists(game))
            lobbyHandler.createLobby(game);

        UserModel user = new UserModel();
        modelAndView.addObject("user", user);
        modelAndView.addObject("gameNames", constants.getGames());
        modelAndView.addObject("lobbies", lobbyHandler.getLobbies());
        modelAndView.setViewName("user/lobbies");
        return modelAndView;

    }

    @RequestMapping(value = "/admin/adminHome", method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel user = userService.findUserByName(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getName());
        modelAndView.addObject("adminMessage", "This Page is available to Users with Admin Role");
        modelAndView.setViewName("admin/adminHome");
        return modelAndView;
    }

    @RequestMapping(value = "/user/userHome", method = RequestMethod.GET)
    public ModelAndView user() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel user = userService.findUserByName(auth.getName());

        modelAndView.addObject("userName", "Welcome " + user.getName());
        modelAndView.addObject("userMessage", "This Page is available to Users with User Role");
        modelAndView.setViewName("user/userHome");
        return modelAndView;
    }
}
