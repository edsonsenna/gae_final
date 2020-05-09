package com.edson.gae_final.controller;

import com.edson.gae_final.exception.UserNotFoundException;
import com.edson.gae_final.model.UpdateInfo;
import com.edson.gae_final.model.User;
import com.edson.gae_final.repository.ProductInterestRepository;
import com.edson.gae_final.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/api/message")
public class MessageController {
    private static final Logger log = Logger.getLogger("MessageController");
    @Autowired
    private ProductInterestRepository productInterestRepository;
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
            log.info(options.toString());
        } catch (IOException e) {
            log.info("Falha	ao	configurar	FirebaseApp");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/sendpriceupdate")
    public ResponseEntity<List<String>> sendPriceUpdate(
            @RequestParam String productSalesId,
            @RequestParam Double price )  {

        List<String> users = this.productInterestRepository.findInterestedUsers(productSalesId, price);
        return new ResponseEntity<List<String>>(users, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/sendupdate")
    public ResponseEntity<String> sendOrderUpdate(
            @RequestBody UpdateInfo updateInfo) throws UserNotFoundException {

        if (updateInfo.getCpf() == null || updateInfo.getCpf().isEmpty()) {
            return new ResponseEntity<String>("CPF é obrigatório!", HttpStatus.BAD_REQUEST);
        } else if(updateInfo.getSalesId() == null || updateInfo.getSalesId().isEmpty()) {
            return new ResponseEntity<String>("Id do provedor de vendas é obrigatório!", HttpStatus.BAD_REQUEST);
        } else if(updateInfo.getCrmId() == null || updateInfo.getCrmId().isEmpty()) {
            return new ResponseEntity<String>("Id do provedor de crm é obrigatório!", HttpStatus.BAD_REQUEST);
        } else if(updateInfo.getMessageSource() == null || updateInfo.getMessageSource().isEmpty()) {
            return new ResponseEntity<String>("Fonte da mensagem é obrigatório!", HttpStatus.BAD_REQUEST);
        } else if(updateInfo.getOrderStatus() == null || updateInfo.getOrderStatus().isEmpty()) {
            return new ResponseEntity<String>("Status do pedido é obrigatório!", HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<User> optUser = userRepository.getByCpf(updateInfo.getCpf());
            User user = optUser.get();
            String registrationToken = user.getFcmRegId();
            log.info("FCM do usuario com CPF " + updateInfo.getCpf() + " e " + registrationToken);
            if(registrationToken == null || registrationToken.isEmpty()) {
                return new ResponseEntity<String>("Usuário não possui um FCM válido", HttpStatus.BAD_REQUEST);
            } else {
                try {
                    Message message = Message.builder()
                            .putData("salesMessage", objectMapper.writeValueAsString(updateInfo))
                            .setToken(registrationToken)
                            .build();
                    String response = FirebaseMessaging.getInstance().send(message);
                    log.info("Mensagem	enviada	ao	pedido: " + updateInfo.getSalesId());
                    log.info("Reposta	do	FCM:	" + response);
                    return new ResponseEntity<String>("Mensagem enviada com sucesso: "
                            + objectMapper.writeValueAsString(updateInfo), HttpStatus.OK);
                } catch (FirebaseMessagingException | JsonProcessingException e) {
                    log.severe("Falha	ao	enviar	mensagem	pelo	FCM:	" + e.getMessage());
                    return new ResponseEntity<String>(e.getMessage(), HttpStatus.PRECONDITION_FAILED);
                }
            }
        } catch (UserNotFoundException e) {
            log.severe("Usuário	não	encontrado");
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }
}