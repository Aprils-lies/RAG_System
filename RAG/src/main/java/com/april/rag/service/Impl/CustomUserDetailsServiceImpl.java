package com.april.rag.service.Impl;

import com.april.rag.entity.User;
import com.april.rag.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/7 21:24
 * Description:实现 Spring Security 的 UserDetailsService 接口，用于加载用户的详细信息（包括用户名、密码和权限）。
 * 通过用户名从数据库中查找用户，并将其转换为 Spring Security 所需的 UserDetails 格式
 */
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户名加载用户详细信息。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中获取用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // 返回 Spring Security 所需的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRole()) // 获取用户的角色权限
        );
    }

    /**
     * 将用户的角色转换为 Spring Security 的权限格式。
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User.Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
