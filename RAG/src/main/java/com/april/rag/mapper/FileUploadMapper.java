package com.april.rag.mapper;

import com.april.rag.entity.FileUpload;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/6 18:38
 * Description:
 */

@Mapper
public interface FileUploadMapper extends BaseMapper<FileUpload> {

    FileUpload selectByFileMd5(@Param("fileMd5") String fileMd5);

    FileUpload selectByFileMd5AndUserId(@Param("fileMd5") String fileMd5, @Param("userId") String userId);

    FileUpload selectPublicByFileName(@Param("fileName") String fileName);

    Long countByFileMd5(@Param("fileMd5") String fileMd5);

    int deleteByFileMd5(@Param("fileMd5") String fileMd5);

    int deleteByFileMd5AndUserId(@Param("fileMd5") String fileMd5, @Param("userId") String userId);

    /**
     * 查询用户自己上传的所有文件
     *
     * @param userId 用户ID
     * @return 用户上传的文件列表
     */
    List<FileUpload> selectByUserId(@Param("userId") String userId);

    List<FileUpload> selectByFileMd5In(@Param("md5List") List<String> md5List);

    /**
     * 查询用户自己的文件和公开文件
     */
    List<FileUpload> findByUserIdOrIsPublicTrue(@Param("userId") String userId);

    /**
     * 查询用户可访问的所有文件（考虑层级标签权限）
     */
    List<FileUpload> findAccessibleFilesWithTags(@Param("userId") String userId, @Param("orgTagList") List<String> orgTagList);

    /**
     * 查询用户可访问的所有文件（原始方法，向后兼容）
     */
    List<FileUpload> findAccessibleFiles(@Param("userId") String userId, @Param("orgTagList") List<String> orgTagList);
}
