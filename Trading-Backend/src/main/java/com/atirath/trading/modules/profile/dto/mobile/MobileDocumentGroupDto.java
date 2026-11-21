package com.atirath.trading.modules.profile.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileDocumentGroupDto {
    private String categoryKey;
    private String groupTitle;
    private List<MobileDocumentItemDto> documents;
}
