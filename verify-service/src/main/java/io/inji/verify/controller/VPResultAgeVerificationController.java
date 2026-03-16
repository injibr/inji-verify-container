package io.inji.verify.controller;
import java.util.List;
import java.util.Optional;

import io.inji.verify.dto.ageverification.AgeVerificationResultDto;
import io.inji.verify.dto.core.ErrorDto;
import io.inji.verify.dto.submission.VPTokenResultDto;
import io.inji.verify.enums.ErrorCode;
import io.inji.verify.exception.VPSubmissionNotFoundException;
import io.inji.verify.exception.VPSubmissionWalletError;
import io.inji.verify.services.VCSubmissionService;
import io.inji.verify.services.VcParseService;
import io.inji.verify.services.VerifiablePresentationRequestService;
import io.inji.verify.services.VerifiablePresentationSubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class VPResultAgeVerificationController {
    final VerifiablePresentationRequestService verifiablePresentationRequestService;
    final VCSubmissionService vcSubmissionService;
    private final VcParseService vcParseService;

    final VerifiablePresentationSubmissionService verifiablePresentationSubmissionService;

    public VPResultAgeVerificationController(VerifiablePresentationRequestService verifiablePresentationRequestService, VCSubmissionService vcSubmissionService, VcParseService vcParseService, VerifiablePresentationSubmissionService verifiablePresentationSubmissionService) {
        this.verifiablePresentationRequestService = verifiablePresentationRequestService;
        this.vcSubmissionService = vcSubmissionService;
        this.vcParseService = vcParseService;
        this.verifiablePresentationSubmissionService = verifiablePresentationSubmissionService;
    }

    @GetMapping(path = "/vp-result-verify-age/{transactionId}")
    public ResponseEntity<Object> getVPResult(@PathVariable String transactionId) {
        List<String> requestIds = verifiablePresentationRequestService.getLatestRequestIdFor(transactionId);

        if (!requestIds.isEmpty()) {
            try {
                log.info("Fetching VP result for transactionId: {}", transactionId);
                VPTokenResultDto result = verifiablePresentationSubmissionService.getVPResult(requestIds, transactionId);
                AgeVerificationResultDto ageVerificationResultDto = new AgeVerificationResultDto();
                Boolean isOver18 = vcParseService.getIsOver18(requestIds, transactionId);
                if(isOver18 != null) {
                    ageVerificationResultDto.setIsAdult(isOver18);
                }else {
                    new RuntimeException("Date of Birth not found in the VC");
                }
                ageVerificationResultDto.setVerificationStatus(String.valueOf(result.getVcResults().get(0).getVerificationStatus()));
                ageVerificationResultDto.setVpVerificationStatus(String.valueOf(result.getVpResultStatus()));
                return ResponseEntity.status(HttpStatus.OK).body(ageVerificationResultDto);
            } catch (VPSubmissionNotFoundException e) {
                log.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(ErrorCode.NO_VP_SUBMISSION));
            } catch (VPSubmissionWalletError e) {
                log.error("Received wallet error for transactionId: {} - {} - {}", e.getErrorCode(), e.getErrorDescription(), transactionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(e.getErrorCode(), e.getErrorDescription()));
            }
        } else {
            return Optional.ofNullable(vcSubmissionService.getVcWithVerification(transactionId))
                    .map(vc -> ResponseEntity.status(HttpStatus.OK).body((Object) vc))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(ErrorCode.INVALID_TRANSACTION_ID)));
        }
    }
}
