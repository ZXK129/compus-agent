package com.starrycampus.card.controller;

import com.starrycampus.card.dto.CardDTO;
import com.starrycampus.card.dto.TopUpRequest;
import com.starrycampus.card.dto.TransactionDTO;
import com.starrycampus.card.service.CardService;
import com.starrycampus.common.base.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/card")
public class CardController {

    private final CardService cardService;
    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public CardController(CardService cardService) { this.cardService = cardService; }

    @GetMapping
    public ApiResponse<CardDTO> getCardInfo() {
        return ApiResponse.success(cardService.getCardInfo(defaultStudentId));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<TransactionDTO>> getTransactions(@RequestParam(required = false) Integer limit) {
        return ApiResponse.success(cardService.getTransactions(defaultStudentId, limit));
    }

    @PostMapping("/topup")
    public ApiResponse<CardDTO> topUp(@Valid @RequestBody TopUpRequest request) {
        return ApiResponse.success("充值成功", cardService.topUp(defaultStudentId, request.getAmount()));
    }
}
