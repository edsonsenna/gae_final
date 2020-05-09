package com.edson.gae_final.controller;

import com.edson.gae_final.exception.UserAlreadyExistsException;
import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.User;
import com.edson.gae_final.repository.UserRepository;
import com.edson.gae_final.util.CheckRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger log = Logger.getLogger("UserController");

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getUsers() {
        return userRepository.getUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        try {
            return new ResponseEntity<User>(userRepository.saveUser(user),
                    HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')	or	hasRole('ADMIN')")
    @PutMapping(path = "/byemail")
    public ResponseEntity<User> updateUser(@RequestBody User user,
                                           @RequestParam("email") String email,
                                           Authentication authentication) {
        if ((user.getId() != null) && user.getId() != 0) {
            try {
                boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                if (hasRoleAdmin || userDetails.getUsername().equals(email)
                ) {
                    if (!hasRoleAdmin) {
                        user.setRole("USER");
                    }
                    return new ResponseEntity<User>(userRepository.updateUser
                            (user, email), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (UserNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (UserAlreadyExistsException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')	or	hasRole('ADMIN')")
    @GetMapping("/byemail")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email,
                                               Authentication authentication) {
        boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (hasRoleAdmin || userDetails.getUsername().equals(email)) {
            try {
                Optional<User> optUser = userRepository.getByEmail(email);
                if (optUser.isPresent()) {
                    return new ResponseEntity<User>(optUser.get(), HttpStatus.OK);
                }
            } catch(UserNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return null;
    }

    @PreAuthorize("hasRole('USER')	or	hasRole('ADMIN')")
    @GetMapping("/bycpf")
    public ResponseEntity<User> getUserByCpf(@RequestParam String cpf,
                                               Authentication authentication) {
        boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (hasRoleAdmin || userDetails.getUsername().equals(cpf)) {
            try {
                Optional<User> optUser = userRepository.getByCpf(cpf);
                if (optUser.isPresent()) {
                    return new ResponseEntity<User>(optUser.get(), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } catch (UserNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/bycpf")
    public ResponseEntity<User> deleteUserByCpf(
            @RequestParam("cpf") String cpf, Authentication authentication) {
        try {
            boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userToRemove = userRepository.getByCpf(cpf);
            if (hasRoleAdmin && !userDetails.getUsername().equals(userToRemove.get().getEmail())) {

                return new ResponseEntity<User>(userRepository.deleteUser(cpf),
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}