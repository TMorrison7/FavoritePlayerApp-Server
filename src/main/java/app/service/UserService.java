package app.service;

import app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.repositories.UserRepository;
import app.util.ApiResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
//    @Autowired
//    private PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<ApiResponse<User>> registerUser(User user) {


        if (userExistByPassword(user.getPassword())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "User with given Password already exist", user)
            );
        }
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.status(201).body(new ApiResponse<>(true, "User registered", user));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {

        return ResponseEntity.status(200).body(new ApiResponse<>(true, "List of Users", userRepository.findAll()));
    }


    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<UserDetails>> getUserByLogin(String username, String password) throws UsernameNotFoundException {
        Optional<User> user = this.userRepository.findByUsernameAndPassword(username, password);
        if (!user.isPresent()) {
            return ResponseEntity.status(401).body(new ApiResponse<>(
                    true,
                    "User not found with given email and/or password",
                    null)
            );
        }

        UserDetails details =
                new org.springframework.security.core.userdetails.User(
                        user.get().getUsername(),
                        user.get().getPassword(),
                        Collections.emptyList()
                );
        return ResponseEntity.status(200).body(new ApiResponse<>(true, "Login Successful for User", details));
    }


    @Transactional
    public boolean userExistByPassword(String password) {
        Optional<User> user = userRepository.findByPassword(password);
        return user.isPresent();
    }
}
