package discord;

import java.lang.reflect.Field;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "discord")
@Validated
public class BotProperties implements Validator {
    @NotBlank
    private String token;

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String defaultPrefix;

    @NotBlank
    private String dbPath;

    @NotNull
    private String youtubeRefreshToken;

    public String token() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String clientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String defaultPrefix() {
        return defaultPrefix;
    }

    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    public String dbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String youtubeRefreshToken() {
        return youtubeRefreshToken;
    }

    public void setYoutubeRefreshToken(String youtubeRefreshToken) {
        this.youtubeRefreshToken = youtubeRefreshToken;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return BotProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BotProperties properties = (BotProperties) target;

        for (Field field : BotProperties.class.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                try {
                    Object value = field.get(properties);
                    checkPlaceholder(field.getName(), value.toString(), errors);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkPlaceholder(String field, String value, Errors errors) {
        if (value != null && value.trim().startsWith("${")) {
            errors.rejectValue(field, "unresolved.placeholder",
                    "The environment variable for '" + field + "' was not resolved or found!");
        }
    }
}
