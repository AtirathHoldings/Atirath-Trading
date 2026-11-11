package com.atirath.trading.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Contact Information section matching Image 1")
public class ContactInfoDto {

    @Schema(description = "Primary Email Address", example = "rohit.sharma@example.com")
    private String emailAddress;

    @Schema(description = "Email verification status", example = "Verified")
    private String emailStatus;

    @Schema(description = "Primary Mobile Number", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Mobile verification status", example = "Verified")
    private String mobileStatus;

    @Schema(description = "Alternate Mobile Number", example = "+91 91234 56789")
    private String alternateMobile;

    @Schema(description = "Alternate Email Address", example = "rohit.alt@example.com")
    private String alternateEmail;
}
