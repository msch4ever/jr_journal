package cz.los.jr_journal.dal.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface GroupMentorMapper {

    @Insert("INSERT INTO group_mentor (group_id, mentor_id) VALUES(#{groupId}, #{userId})")
    void createAssociation(@Param("groupId") long groupId, @Param("userId") long userId);

}
