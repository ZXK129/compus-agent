package com.starrycampus.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书借阅请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {
    @NotBlank(message = "ISBN 不能为空")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    private String title;

    @NotBlank(message = "作者不能为空")
    private String author;
}
