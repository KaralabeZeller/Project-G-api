package com.nter.projectg.controller;

import com.nter.projectg.config.RedirectConfig;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
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
        UserModel user = userService.findByName(auth.getName());
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
        UserModel user = userService.findByName(auth.getName());
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

        // TODO only lookup UserModel when BindingResult is valid
        UserModel userModel = userService.findByName(user.getName());
        if (userModel != null) {
            bindingResult.rejectValue("name", "error.user", "There is already a user registered with the name provided");
        }

        if (!bindingResult.hasErrors()) {
            userService.save(user);
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", new UserModel());
            modelAndView.setViewName("registration");
        } else {
            modelAndView.setViewName("registration");
        }

        return modelAndView;
    }

    //TODO implement create new lobby with lobbyHandler
    @RequestMapping(value = "/createLobby", method = RequestMethod.POST)
    public String createLobby(@Valid String game, final RedirectAttributes redirectAttributes, HttpServletResponse response, HttpServletRequest request) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Creating lobby for game: {}", game);

        if (gameFactory.gameExists(game)) {
            lobbyHandler.createLobby(game);
        }

        String environment = request.getHeader("host").contains("localhost") ? "LOCAL" : "REMOTE";
        logger.info("sendRedirect environment: {}", environment);

        String redirectUrl = "lobbies";
        if (environment.equals("LOCAL")) {
            redirectUrl = response.encodeRedirectURL(redirectUrl);
            RedirectConfig.setLocation("LOCAL");
        } else {
            redirectUrl = response.encodeRedirectURL("https://" + request.getHeader("host") + "/" + redirectUrl);
            RedirectConfig.setLocation("REMOTE");
        }
        logger.info("sendRedirect encoded URL: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
        return "redirect:user/lobbies";

    }

    @RequestMapping(value = "/admin/adminHome", method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel user = userService.findByName(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getName());
        modelAndView.addObject("adminMessage", "This Page is available to Users with Admin Role");
        modelAndView.setViewName("admin/adminHome");
        return modelAndView;
    }

    @RequestMapping(value = "/user/userHome", method = RequestMethod.GET)
    public ModelAndView user() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel user = userService.findByName(auth.getName());

        modelAndView.addObject("userName", "Welcome " + user.getName());
        modelAndView.addObject("userMessage", "This Page is available to Users with User Role");
        modelAndView.setViewName("user/userHome");
        return modelAndView;
    }
}
