package com.subbotin.echo;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

public class PomodoroBot extends TelegramLongPollingBot {

    private final ConcurrentHashMap<UserTimer, Long> userTimerRepository = new ConcurrentHashMap();

    record UserTimer(Instant userTimer, TimerType timerType) {

    }

    @Override
    public String getBotUsername() {
        return "Pomodoro";
    }

    @Override
    public String getBotToken() {
        return "5499302700:AAFWo2TCISVr2Gtpu9o_655bMYtQLtqIzdg";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var args = update.getMessage().getText().split(" ");
        // 10:30 30 10
        // workTime 11:00
        // breakTime 11:10
        Instant workTime = Instant.now().plus(Long.parseLong(args[0]), ChronoUnit.MINUTES);
        Instant breakTime = workTime.plus(Long.parseLong(args[1]), ChronoUnit.MINUTES);
        userTimerRepository.put(new UserTimer(workTime, TimerType.WORK), update.getMessage().getChatId());
        System.out.printf("[%s] Размер коллекции %d", Instant.now().toString(), userTimerRepository.size());
        userTimerRepository.put(new UserTimer(breakTime, TimerType.BREAK), update.getMessage().getChatId());
        System.out.printf("[%s] Размер коллекции %d", Instant.now().toString(), userTimerRepository.size());
        sendMsg(update.getMessage().getChatId(), "Таймер запущен!!!");
    }

    public void checkTimer() throws InterruptedException {
        while (true) {
            System.out.println("Количество таймеров пользователей " + userTimerRepository.size());
            userTimerRepository.forEach((timer, userId) -> {
                if (Instant.now().isAfter(timer.userTimer)) {
                    switch (timer.timerType) {
                        case WORK -> sendMsg(userId, "Время отдыхать!!!");
                        case BREAK -> sendMsg(userId, "Таймер завершил работу!!!");
                    }
                    userTimerRepository.remove(timer);
                }
            });
            Thread.sleep(1000);
        }
    }

    private void sendMsg(Long chatId, String text) {
        /*
        Логика отправки сообщений:
            - создаем обьект SendMessage;
            - проставим ChatId и текст с которым нужно взаимодействовать (ChatId - id пользователя с которым происходит взаимодействие);
            - из update достаем message и получаем ChatId;
            - создаём текст;
            - получаем и печатаем имя пользователя;
            - отправляем обратно принятое сообщение.
         */
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
