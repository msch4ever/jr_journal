package cz.los.jr_journal.model;

import lombok.*;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString(exclude = {"mentorIds", "schedule"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Group {

    @EqualsAndHashCode.Include
    private Long groupId;
    @EqualsAndHashCode.Include
    private String name; //ToDo: create display name and lowercase name as an unique identifier
    @Setter
    private Level module;
    private List<Long> mentorIds;
    private List<DayOfWeek> schedule;

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, Level module) {
        this.name = name;
        this.module = module;
    }

    public Group(Long groupId, String name, Level module) {
        this.groupId = groupId;
        this.name = name;
        this.module = module;
    }

    //ToDo: add operational flag to indicate if Group is ready for work. That is: has schedule, has mentors, has module

}
