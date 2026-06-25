package com.starrycampus.chat.tool.function;

/**
 * Function Calling 工具的输入参数定义（Java Record，Spring AI 自动生成 JSON Schema）
 */
public class ToolInputs {

    public record EmptyInput() {}

    public record WeekdayInput(Integer weekday) {}

    public record CourseIdInput(Long courseId) {}

    public record AmountInput(Double amount) {}

    public record BookIdInput(Long bookId) {}

    public record BorrowInput(String isbn, String title, String author) {}

    public record FloorInput(String floor) {}

    public record EventIdInput(Long eventId) {}

    public record CreateMomentInput(String title, String tag, String location) {}

    public record ExtendInput(Long bookId, Integer days) {}
}
