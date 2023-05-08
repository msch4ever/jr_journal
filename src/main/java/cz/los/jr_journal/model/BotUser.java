package cz.los.jr_journal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(exclude = "groups")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BotUser {

    @EqualsAndHashCode.Include
    private Long userId;
    @EqualsAndHashCode.Include
    private Long telegramUserId;
    private String username;
    private String firstName;
    private String lastName;
    private UserType type;
    @Setter
    private List<Group> groups;

    public BotUser(Long telegramUserId, String username, String firstName, String lastName) {
        this.telegramUserId = telegramUserId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public BotUser(Long userId, Long telegramUserId, String username, String firstName, String lastName, UserType type) {
        this.userId = userId;
        this.telegramUserId = telegramUserId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
    }
}
