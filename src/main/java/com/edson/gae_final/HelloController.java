package com.edson.gae_final;

import	org.springframework.web.bind.annotation.GetMapping;
import	org.springframework.web.bind.annotation.PathVariable;
import	org.springframework.web.bind.annotation.RequestMapping;
import	org.springframework.web.bind.annotation.RestController;
import	java.util.logging.Logger;

@RestController
@RequestMapping(path="/api/test")
public class HelloController	{
    private static final	Logger	log	=	Logger.getLogger(HelloController.class.getName());
    @GetMapping("/{name}")
    public	String	hello(@PathVariable	String	name) {
        log.info("Name:	"	+	name);
        return "Hello	World	-	"	+	name;
    }
}