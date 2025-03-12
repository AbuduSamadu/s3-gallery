package com.mascot.springboots3gallery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    @GetMapping("/")
    public String  getImage() {
       return "Hello World";
    }
}
