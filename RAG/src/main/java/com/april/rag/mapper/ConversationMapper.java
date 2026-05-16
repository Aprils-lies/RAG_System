package com.april.rag.mapper;

import com.april.rag.entity.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 22:02
 * Description:
 */

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    List<Conversation> selectByUserIdAndTime(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Conversation> selectByUserId(@Param("userId") Long userId);

    List<Conversation> selectByTime(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

