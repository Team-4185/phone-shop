package com.challengeteam.shop.service;

public interface EmailService {

    void sendResetLink(String to, String link);

}
