package cz.los.jr_journal.dal.mapper;

import cz.los.jr_journal.model.Group;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

public interface GroupMapper {

    @Select("SELECT group_id, name, display_name, module FROM bot_group WHERE group_id = #{groupId}")
    Group getGroupById(Long groupId);

    @Select("SELECT group_id, name, display_name, module FROM bot_group WHERE name = #{name}")
    Optional<Group> findByName(@Param("name") String name);

    @Select("""
            <script>
                SELECT group_id, name, display_name, module FROM bot_group
                WHERE group_id IN
                <foreach item='groupId' index='index' collection='groupIds' open='(' separator=',' close=')'>
                     #{groupId}
                </foreach>
            </script>
            """
    )
    List<Group> getByIdInList(@Param("groupIds") List<Long> groupIds);

    @Select("SELECT COUNT(*) FROM bot_group WHERE name = #{name}")
    int countByName(@Param("name") String name);

    @Insert("""
            INSERT INTO bot_group (name, display_name, module)
            VALUES (#{name}, #{displayName}, #{module})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "groupId")
    void insertGroup(Group group);

    @Update("""
            UPDATE bot_group
            SET module = #{module}
            WHERE group_id = #{groupId}
            """)
    void updateGroup(Group group);

    @Delete("DELETE FROM bot_group WHERE name = #{name}")
    void deleteGroup(@Param("name") String name);

}
