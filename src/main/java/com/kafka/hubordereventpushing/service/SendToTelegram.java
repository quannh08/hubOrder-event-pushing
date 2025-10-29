package com.kafka.hubordereventpushing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SEND-TO-TELEGRAM")
public class SendToTelegram extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        String botUsername = "";
        return botUsername;
    }

    @Value("${bot.token}")
    private String token ;//= ConfigLoader.getEventTelegramKey();

    @Override
    public String getBotToken() {
        return token;
    }

    @Value("${bot.chatId}")
    private String chatId ;//= ConfigLoader.getEventTelegramGroupId();

    @Override
    public void onUpdateReceived(Update update) {
    }




    public void sendError(Throwable ex, Long eventId) {
        String currentTime = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).toString();
        String messageText = String.format(
                "System: HubOrder-Event-Pushing\n" +
                        "Time: %s\n" +
                        "EventId: %s\n" +
                        "Content: %s",
                currentTime,
                eventId,
                ex.getMessage()
        );
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)  // (chat_id)
                .text(messageText)
                .build();

        try {
            execute(sendMessage);  // Gửi qua API
            log.info("Notification sent successfully to chat_id: " + chatId);
        } catch (TelegramApiException e) {
            System.err.println("Error sending notification: " + e.getMessage());
            // Có thể throw exception hoặc log
        }
    }
}
