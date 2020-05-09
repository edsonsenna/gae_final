package com.edson.gae_final.controller;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.ProductInterest;
import com.edson.gae_final.repository.ProductInterestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productinterests")
public class ProductInterestController {

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
}
