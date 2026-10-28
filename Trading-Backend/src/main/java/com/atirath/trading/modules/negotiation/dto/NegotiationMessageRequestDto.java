package com.atirath.trading.modules.negotiation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationMessageRequestDto {

    @NotBlank(message = "Message text cannot be empty")
    private String messageText;

    private String attachmentUrl;
}
