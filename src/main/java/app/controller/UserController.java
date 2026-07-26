package app.controller;

import app.dto.LoginRequest;
import lombok.extern.slf4j.Slf4j;
import app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import app.service.UserService;
import app.util.ApiResponse;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/warehouse")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody User user, Errors errors){
        return userService.registerUser(user);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDetails>> login(@RequestBody LoginRequest loginRequest) {
        return
                userService.getUserByLogin(loginRequest.getUsername(), loginRequest.getPassword());
    }
}
