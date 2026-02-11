package com.april.rag.mapper;

import com.april.rag.entity.OrganizationTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2025/12/8 15:34
 * Description: OrganizationTagMapper
 */

@Mapper
public interface OrganizationTagMapper extends BaseMapper<OrganizationTag> {

    OrganizationTag findByTagId(String tagId);

    List<OrganizationTag> findByParentTag(String parentTag);

    boolean existsByTagId(String tagId);
}
