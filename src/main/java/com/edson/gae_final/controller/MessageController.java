package com.edson.gae_final.controller;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.UpdateInfo;
import com.edson.gae_final.model.User;
import com.edson.gae_final.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/message")
public class MessageController {
    private static final Logger log = Logger.getLogger("MessageController");
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setDatabaseUrl("https://esjdm111.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp	configurado");
        } catch (IOException e) {
            log.info("Falha	ao	configurar	FirebaseApp");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/sendupdate")
    public ResponseEntity<String> sendOrderUpdate(
            @RequestBody UpdateInfo updateInfo) throws UserNotFoundException {

        if (updateInfo.getCpf() == null) {
            return new ResponseEntity<String>("CPF é obrigatório!", HttpStatus.BAD_REQUEST);
        }

        Optional<User> optUser = userRepository.getByCpf(updateInfo.getCpf());
        if (optUser.isPresent()) {
            User user = optUser.get();
            String registrationToken = user.getFcmRegId();
            try {
                Message message = Message.builder()
                        .putData("updateInfo", objectMapper.writeValueAsString(updateInfo))
                        .setToken(registrationToken)
                        .build();
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Mensagem	enviada	ao	pedido	" + updateInfo.getSalesId());
                log.info("Reposta	do	FCM:	" + response);
                return new ResponseEntity<String>("Mensagem	enviada	com	o pedido: "
                        + updateInfo.getSalesId(), HttpStatus.OK);
            } catch (FirebaseMessagingException | JsonProcessingException e) {
                log.severe("Falha	ao	enviar	mensagem	pelo	FCM:	" + e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        } else {
            log.severe("Usuário	não	encontrado");
            return new ResponseEntity<String>("Usuário	não	encontrado", HttpStatus.NOT_FOUND);
        }

    }
}