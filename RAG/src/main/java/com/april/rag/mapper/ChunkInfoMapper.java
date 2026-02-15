package com.april.rag.mapper;

import com.april.rag.entity.ChunkInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 13:24
 * Description:
 */
@Mapper
public interface ChunkInfoMapper extends BaseMapper<ChunkInfo> {
    List<ChunkInfo> selectByFileMd5OrderByChunkIndexAsc(@Param("fileMd5") String fileMd5);
}
