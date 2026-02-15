package com.april.rag.mapper;

import com.april.rag.entity.DocumentVector;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/6 19:59
 * Description:
 */

@Mapper
public interface DocumentVectorMapper extends BaseMapper<DocumentVector> {
    /**
     * 根据文件 MD5 查询该文件的所有向量分块
     */
    List<DocumentVector> findByFileMd5(String fileMd5);

    /**
     * 根据文件 MD5 删除该文件的所有向量分块
     */
    int deleteByFileMd5(String fileMd5);
}
