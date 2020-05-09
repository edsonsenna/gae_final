package com.edson.gae_final.service;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.User;
import com.edson.gae_final.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("userDetailsService")
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        try{
            Optional<User> optUser =
                    userRepository.getByEmail(email);
            if (optUser.isPresent()) {
                return optUser.get();
            }
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("Usuário	não	encontrado");
        }
        return null;
    }
}