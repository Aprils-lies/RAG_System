package com.april.rag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 四月是你的谎言
 * @CreateTime: 2026/2/15 13:13
 * Description:
 */

@Data
@AllArgsConstructor
public class FileTypeValidationResult {
    private final boolean valid;
    private final String message;
    private final String fileType;
    private final String extension;

    @Override
    public String toString() {
        return String.format("FileTypeValidationResult{valid=%s, message='%s', fileType='%s', extension='%s'}",
                valid, message, fileType, extension);
    }
}
