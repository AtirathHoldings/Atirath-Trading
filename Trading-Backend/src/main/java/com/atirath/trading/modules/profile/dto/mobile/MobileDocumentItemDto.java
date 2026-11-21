package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileDocumentItemDto {
    private String documentId;
    private String title;
    private String subtitle;
    private String fileFormat;
    private String fileSize;
    private String downloadUrl;
    private boolean verified;
}
