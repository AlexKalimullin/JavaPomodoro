package TelegramPackage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.sql.DataSource;

@Configuration
public class AppConfiguration {

    @Bean
    public PomodoroBot pomodoroBot(TimerDao timerDao){
        return new PomodoroBot(timerDao);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(PomodoroBot pomodoroBot) throws TelegramApiException{
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(pomodoroBot);
        new Thread(() -> {
            try {
                pomodoroBot.checkTimer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).run();
        return telegramBotsApi;
    }

    @Bean
    public DataSource dataSource(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc.postgresql://localhost:5432/postgres");
        config.setUsername("postgres");
        config.setPassword("test");
        return new HikariDataSource(config);
    }

    @Bean
    public TimerDao timerDao(DataSource dataSource){
        return new TimerDao(dataSource);
    }
}
