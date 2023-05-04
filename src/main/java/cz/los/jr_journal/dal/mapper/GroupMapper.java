package cz.los.jr_journal.dal.mapper;

import cz.los.jr_journal.model.Group;
import org.apache.ibatis.annotations.*;

public interface GroupMapper {

    @Select("SELECT * FROM bot_group WHERE group_id = #{groupId}")
    Group getGroupById(Long groupId);

    @Select("SELECT COUNT(*) FROM bot_group WHERE name = #{name}")
    int countByName(@Param("name") String name);

    @Insert("""
            INSERT INTO bot_group (name, module)
            VALUES (#{name}, #{module})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "groupId")
    void insertGroup(Group group);

    @Update("""
               UPDATE bot_group
               SET name = #{name},
                   module = #{module}
               WHERE group_id = #{groupId}
               """)
    void updateGroup(Group group);

    @Delete("DELETE FROM bot_group WHERE group_id = #{groupId}")
    void deleteGroup(Long groupId);
    
}
