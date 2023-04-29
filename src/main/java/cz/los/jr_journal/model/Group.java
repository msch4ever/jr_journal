package cz.los.jr_journal.model;

import java.time.DayOfWeek;
import java.util.List;

public class Group {

    private Long groupId;
    private String name;
    private Level module;
    private List<Long> mentorIds;
    private List<DayOfWeek> schedule;
}
