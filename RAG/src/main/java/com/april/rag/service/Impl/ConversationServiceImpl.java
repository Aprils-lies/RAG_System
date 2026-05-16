package com.april.rag.service.Impl;

import com.april.rag.entity.Conversation;
import com.april.rag.entity.User;
import com.april.rag.exception.CustomException;
import com.april.rag.mapper.ConversationMapper;
import com.april.rag.mapper.UserMapper;
import com.april.rag.service.ConversationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 22:01
 * Description:对话管理
 */
@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 记录用户的对话历史。
     *
     * @param username 用户名
     * @param question 用户提问内容
     * @param answer   系统回答内容
     */
    @Override
    public void recordConversation(String username, String question, String answer) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        Conversation conversation = new Conversation();
        conversation.setUserId(user.getId());
        conversation.setQuestion(question);
        conversation.setAnswer(answer);

        conversationMapper.insert(conversation);
    }

    /**
     * 查询用户的对话历史。
     *
     * @param username  用户名
     * @param startDate 起始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 符合条件的对话记录列表
     */
    @Override
    public List<Conversation> getConversations(String username, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new CustomException("User not found", HttpStatus.NOT_FOUND);
        }

        // 检查用户角色，如果是管理员且username参数为"all"，则返回所有对话历史
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        if (user.getRole() == User.Role.ADMIN && "all".equals(username)) {
            // 时间范围（可选）
            wrapper.between(
                    startDate != null && endDate != null,
                    Conversation::getTimestamp,
                    startDate,
                    endDate
            );
        } else {
            // 时间范围（可选）
            wrapper.eq(Conversation::getUserId, user.getId()).between(
                    startDate != null && endDate != null,
                    Conversation::getTimestamp,
                    startDate,
                    endDate
            );
        }
        wrapper.orderByDesc(Conversation::getTimestamp);

        return conversationMapper.selectList(wrapper);
    }

    /**
     * 管理员查询所有用户的对话历史。
     *
     * @param adminUsername  管理员用户名
     * @param targetUsername 目标用户名（可选，如果提供则只查询该用户的对话历史）
     * @param startDate      起始日期（可选）
     * @param endDate        结束日期（可选）
     * @return 符合条件的对话记录列表
     */
    @Override
    public List<Conversation> getAllConversations(String adminUsername, String targetUsername, LocalDateTime startDate, LocalDateTime endDate) {
        User admin = userMapper.findByUsername(adminUsername);
        if (admin == null) {
            throw new CustomException("Admin not found", HttpStatus.NOT_FOUND);
        }

        // 验证用户是否为管理员
        if (admin.getRole() != User.Role.ADMIN) {
            throw new CustomException("Unauthorized access", HttpStatus.FORBIDDEN);
        }

        // 如果指定了目标用户，则只查询该用户的对话历史
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        if (targetUsername != null && !targetUsername.isEmpty()) {
            User targetUser = userMapper.findByUsername(targetUsername);
            if (targetUser == null) {
                throw new CustomException("Target not found", HttpStatus.NOT_FOUND);
            }
            wrapper.eq(Conversation::getUserId, targetUser.getId())
                    .between(
                            startDate != null && endDate != null,
                            Conversation::getTimestamp,
                            startDate,
                            endDate
                    );
        } else {
            wrapper.between(
                    startDate != null && endDate != null,
                    Conversation::getTimestamp,
                    startDate,
                    endDate
            );
        }
        wrapper.orderByDesc(Conversation::getTimestamp);

        return conversationMapper.selectList(wrapper);
    }
}
