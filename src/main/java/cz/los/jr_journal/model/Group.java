package cz.los.jr_journal.model;

import lombok.*;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@Builder
@ToString(exclude = {"mentorIds", "schedule"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Group {

    @EqualsAndHashCode.Include
    private Long groupId;
    @EqualsAndHashCode.Include
    private String name;
    private Level module;
    private List<Long> mentorIds;
    private List<DayOfWeek> schedule;

    //ToDo: add operational flag to indicate if Group is ready for work. That is: has schedule, has mentors, has module

}
