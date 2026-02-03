package com.april.rag.mapper;

import com.april.rag.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/5 21:23
 * Description:
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {

//    @Select("select * from users where username = #{username}")
    User findByUsername(String username);

    @Select("select * from users where id = #{userId}")
    User findById(Long userId);
}
