//package com.example.moneymanager.service;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.properties.mail.smtp.from}")
//    private String fromEmail;
//
//    public void sendEmail(String to, String subject, String body){
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(body);
//
//            System.out.println(">>> Sending email to: " + to + " from: " + fromEmail);
//            mailSender.send(message);
//            System.out.println(">>> Email sent successfully!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to send email", e);
//        }
//    }
//
//}


package com.example.moneymanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${BREVO_FROM_EMAIL}")
    private String fromEmail;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.brevo.com")
            .build();

    public void sendEmail(String to, String subject, String body) {
        try {
            Map<String, Object> emailPayload = Map.of(
                    "sender", Map.of("name", "MoneyManager", "email", fromEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", body
            );

            webClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", apiKey)
                    .bodyValue(emailPayload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(">>> Email sent via Brevo API to " + to);

        } catch (WebClientResponseException e) {
            System.err.println("Error sending email: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send email via Brevo API", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email via Brevo API", e);
        }
    }
}
