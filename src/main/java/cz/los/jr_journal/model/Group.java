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
    private String name;
    private String displayName;
    @Setter
    private Level module;
    private List<Long> mentorIds;
    private List<DayOfWeek> schedule;

    public Group(String name) {
        name = name.trim();
        this.name = name.toLowerCase();
        this.displayName = name;
    }

    public Group(String name, Level module) {
        this(name);
        this.module = module;
    }

    public Group(Long groupId, String name, String displayName, Level module) {
        this.groupId = groupId;
        this.name = name;
        this.displayName = displayName;
        this.module = module;
    }
}
