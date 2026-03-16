package io.inji.verify.dto.ageverification;

import lombok.Data;

@Data
public class AgeVerificationResultDto {
    String verificationStatus;
    String vpVerificationStatus;
    Boolean isAdult;
}
