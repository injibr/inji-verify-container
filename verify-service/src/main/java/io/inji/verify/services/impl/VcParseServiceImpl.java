package io.inji.verify.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.inji.verify.exception.VPSubmissionNotFoundException;
import io.inji.verify.models.VPSubmission;
import io.inji.verify.repository.VPSubmissionRepository;
import io.inji.verify.services.VcParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VcParseServiceImpl implements VcParseService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    final VPSubmissionRepository vpSubmissionRepository;

    public VcParseServiceImpl(VPSubmissionRepository vpSubmissionRepository) {
        this.vpSubmissionRepository = vpSubmissionRepository;
    }

    @Override
    public Boolean getIsOver18(List<String> requestIds, String transactionId) throws VPSubmissionNotFoundException {
        List<VPSubmission> vpSubmissions = vpSubmissionRepository.findAllById(requestIds);

        if (vpSubmissions.isEmpty()) {
            throw new VPSubmissionNotFoundException();
        }
        String vcJson = vpSubmissions.getFirst().getVpToken();
        try {
            log.info("Parsing VC to extract dateOfBirth");
            JsonNode rootNode = objectMapper.readTree(vcJson);
            JsonNode isOver18Node = rootNode.path("verifiableCredential").get(0).path("credentialSubject").path("isOver18");

            if (isOver18Node.isMissingNode() || isOver18Node.isNull()) {
                throw new IllegalArgumentException("dateOfBirth not found in VC");
            }

            return isOver18Node.asText().contains("true")?true:false;

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract dateOfBirth from VC", e);
        }
    }
}
