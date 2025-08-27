//package org.badmintonchain.config;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//    public class RabbitTest {
//
//        @Autowired
//        private RabbitTemplate rabbitTemplate;
//
//        @PostConstruct
//        public void sendTestMessage() {
//            rabbitTemplate.convertAndSend(
//                    RabbitMQConfig.BOOKING_EVENT_QUEUE,
//                    "Hello RabbitMQ!"
//            );
//            System.out.println(">>> Test message sent");
//        }
//
//        @RabbitListener(queues = RabbitMQConfig.BOOKING_EVENT_QUEUE)
//        public void receiveTestMessage(String message) {
//            System.out.println(">>> Received: " + message);
//        }
//    }