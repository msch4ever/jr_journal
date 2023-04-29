package cz.los.jr_journal.model;

import java.util.List;

public class User {

    private Long userId;
    private Long telegramUserId;
    private String username;
    private String firstName;
    private String lastName;
    private UserType type;
    private List<Long> groupIds;

}
