package com.challengeteam.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HelloController {

    @GetMapping
    public ResponseEntity<String> getHello() {
        String pingLink = "/ping";
        String template = """
                {
                    "greeting" : "Hello world!",
                    "ping-link" : "%s"
                }
                """;
        String response = template.formatted(pingLink);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        String response = """
                {
                    "status" : "OK"
                }
                """;

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-name")
    public ResponseEntity<String> getMyName() {
        String response = """
                {
                    "name" : "Bohdan",
                    "linkein" : "www.linkedin.com/in/bohdan-yarovyi"
                }
                """;

        return ResponseEntity.ok(response);
    }


}
