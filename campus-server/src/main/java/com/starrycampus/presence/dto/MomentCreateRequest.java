package com.starrycampus.presence.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomentCreateRequest {
    @NotBlank(message = "内容不能为空")
    private String title;
    @NotBlank(message = "标签不能为空")
    private String tag;
    @NotBlank(message = "位置不能为空")
    private String location;
}
