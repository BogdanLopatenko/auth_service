package com.auth_service.producer;

import com.auth_service.dto.MailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.mailing}")
    private String topic;

    public void sendMail(MailDto dto) {

        kafkaTemplate.send(topic, dto);
    }
}
