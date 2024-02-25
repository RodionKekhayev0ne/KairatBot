package KairatTiketBot.TiketBot.config;

import KairatTiketBot.TiketBot.Service.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("TiketBot.Service")
public class AppConfig {

    private final TelegramBot telegramBot;

    @Autowired
    public AppConfig(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

}
