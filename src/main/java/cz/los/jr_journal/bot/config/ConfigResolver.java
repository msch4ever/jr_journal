package cz.los.jr_journal.bot.config;

import cz.los.jr_journal.bot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigResolver {

    private static final String BASE_CONFIG = "src/main/resources/base.properties";
    private static final String DEV_CONFIG = "dev.properties";
    private static final String BASE = "base";
    private static final String DEV = "dev";

    public BotConfig resolveConfig(String[] cmd) {
        log.info("Resolving configuration for bot...");
        validateCmd(cmd);

        try (InputStream input = new FileInputStream(resolveConfigFile(cmd))) {
            Properties prop = new Properties();
            prop.load(input);

            String botName = prop.getProperty("bot.name");
            String username = prop.getProperty("bot.username");
            String token = retrieveToken(prop);
            log.info("Configuration for bot resolved!");
            return new BotConfig(botName, username, token);
        } catch (Exception e) {
            String message = "Could not create configuration for Bot." + System.lineSeparator() + e.getMessage();
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    private static String retrieveToken(Properties prop) {
        String tokenProperty = prop.getProperty("bot.token");
        return System.getenv(
                tokenProperty
                        .replace("${", "")
                        .replace("}", "")
                        .trim());
    }

    private static String resolveConfigFile(String[] cmd) {
        log.info("Resolving config file to be used...");
        String configFile;
        if (cmd.length == 1) {
            configFile = DEV_CONFIG;
        } else {
            configFile = BASE_CONFIG;
        }
        log.info("{} config file will be used.", configFile);
        return configFile;
    }

    private void validateCmd(String[] cmd) {
        log.info("Validating cmd params...");
        String message;
        if (cmd.length > 1) {
            message = String.format("Too many arguments passed! Expected 0 or 1, but was %s!", cmd.length);
            log.error(message);
            throw new RuntimeException(message);
        }
        if (cmd.length == 1) {
            String param = cmd[0];
            if (!(BASE.equalsIgnoreCase(param) || DEV.equalsIgnoreCase(param))) {
                message = String.format("Unexpected parameter passed! Expected %s or %s, but was %s!", BASE, DEV, param);
                log.error(message);
                throw new RuntimeException(message);
            }
        }
        log.info("Cmd arguments are valid!");
    }

}
