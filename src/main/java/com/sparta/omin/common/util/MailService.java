package com.sparta.omin.common.util;

import com.sparta.omin.app.model.order.entity.status.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public void snedOrderStatusMail(String to, OrderStatus status) {
        System.out.println("to = " + to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[주문 알림] " + status.getDescription());
        message.setText(status.getMailMessage());
        javaMailSender.send(message);
    }
}
