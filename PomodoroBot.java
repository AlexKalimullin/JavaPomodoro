import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

class PomodoroBot extends TelegramLongPollingBot {

    private final ConcurrentHashMap<UserTimer, Long> userTimeRepository = new ConcurrentHashMap();

    enum TimerType{
        WORK,
        BREAK
    }

    record UserTimer(Instant userTimer, TimerType timerType){}

    @Override
    public String getBotUsername() {
        return "Govorun";
    }

    @Override
    public String getBotToken() {
        return "5440312385:AAHsUTLIfKCb6OVwuba9DvaTiLfOLu6HqiY";
    }

    @Override
    public void onUpdateReceived(Update update) {
        /*if(update.hasMessage() && update.getMessage().hasText()){
            if(update.getMessage().getText().equals("/start")){
                sendMsg(update.getMessage().getChatId(),
                        "Hello i'm a bot. My name is Govorun",
                        update.getMessage().getChat().getUserName());
                return;
            }
            sendMsg(update.getMessage().getChatId(),
                    update.getMessage().getText(),
                    update.getMessage().getChat().getUserName());
        }*/
        if(!update.hasMessage() && !update.getMessage().hasText()){
            return;
        }
        var args = update.getMessage().getText().split(" ");
        Instant workTime = Instant.now().plus(Long.parseLong(args[0]), ChronoUnit.MINUTES);
        Instant breakTime = workTime.plus(Long.parseLong(args[1]), ChronoUnit.MINUTES);

        userTimeRepository.put(new UserTimer(workTime,TimerType.WORK), update.getMessage().getChatId());
        userTimeRepository.put(new UserTimer(breakTime,TimerType.BREAK), update.getMessage().getChatId());

    }

    private void sendMsg(Long chatId, String text/*, String userName*/){
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        /*System.out.println("Пользователь использовал бота = " + userName);*/
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void checkTimer() throws InterruptedException {
        while (true){
            System.out.println("Number of users timers" + userTimeRepository.size());
            userTimeRepository.forEach((timer, userId) -> {
                if (Instant.now().isAfter(timer.userTimer)){
                    switch (timer.timerType){
                        case WORK -> sendMsg(userId, "relax");
                        case BREAK -> sendMsg(userId, "Timer is over");
                    }
                    userTimeRepository.remove(timer);
                }

            });
            Thread.sleep(1000);
        }
    }
}
