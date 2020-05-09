package com.edson.gae_final.controller;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.ProductInterest;
import com.edson.gae_final.model.User;
import com.edson.gae_final.repository.ProductInterestRepository;
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

@RestController
@RequestMapping("/api/productinterests")
public class ProductInterestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductInterestRepository productInterestRepository;

    @PreAuthorize("hasRole('USER')	or	hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductInterest> saveProductInterest(@RequestBody ProductInterest productInterest) {
        try {
            return new ResponseEntity<ProductInterest>(productInterestRepository.saveOrUpdateProductInterest(productInterest),
                    HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('USER')	or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductInterest>> getUsers(
            @RequestParam String cpf,
            Authentication authentication
    ) {
        boolean hasRoleAdmin = CheckRole.hasRoleAdmin(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        try {
            Optional<User> user = this.userRepository.getByEmail(userDetails.getUsername());
            if(user.isPresent()) {
                User currentUser = user.get();
                if (hasRoleAdmin || currentUser.getCpf().equals(cpf)) {
                    return new ResponseEntity<List<ProductInterest>>(productInterestRepository.getUserProductsInterests(cpf), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
