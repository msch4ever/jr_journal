package cz.los.jr_journal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(exclude = "groupIds")
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
    private List<Long> groupIds;

    public BotUser(Long telegramUserId, String username, String firstName, String lastName) {
        this.telegramUserId = telegramUserId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}