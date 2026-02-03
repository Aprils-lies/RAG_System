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

    @Select("select * from organization_tags where tag_id = #{tagId}")
    OrganizationTag findByTagId(String tagId);

    @Select("select * from organization_tags where parent_tag = #{parentTag}")
    List<OrganizationTag> findByParentTag(String parentTag);

    @Select("select count(*) > 0 from organization_tags where tag_id = #{tagId}")
    boolean existsByTagId(String tagId);
}
