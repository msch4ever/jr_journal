package cz.los.jr_journal.dal.mapper;

import cz.los.jr_journal.model.BotUser;
import org.apache.ibatis.annotations.*;

public interface UserMapper {
    @Select("SELECT * FROM bot_user WHERE user_id = #{userId}")
    BotUser getUserById(Long userId);

    @Select("SELECT user_id, telegram_user_id, username, first_name, last_name, type FROM bot_user WHERE telegram_user_id = #{telegramId}")
    BotUser findByTelegramId(long telegramId);

    @Select("SELECT user_id, telegram_user_id, username, first_name, last_name, type FROM bot_user WHERE username = #{username}")
    BotUser findByUsername(String username);

    @Select("SELECT COUNT(*) FROM bot_user WHERE username = #{username} AND telegram_user_id = #{telegramUserId}")
    int countByUsernameAndTelegramId(@Param("username") String username, @Param("telegramUserId") long telegramUserId);

    @Insert("""
            INSERT INTO bot_user (telegram_user_id, username, first_name, last_name, type)
            VALUES (#{telegramUserId}, #{username}, #{firstName}, #{lastName}, #{type})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insertUser(BotUser user);

    @Update("""
               UPDATE bot_user
               SET telegram_user_id = #{telegramUserId},
                   username = #{username},
                   first_name = #{firstName},
                   last_name = #{lastName},
                   type = #{type}
               WHERE user_id = #{userId}
               """)
    void updateUser(BotUser user);

    @Delete("DELETE FROM bot_user WHERE user_id = #{userId}")
    void deleteUser(Long userId);
}
