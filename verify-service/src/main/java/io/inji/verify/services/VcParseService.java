package io.inji.verify.services;

import io.inji.verify.exception.VPSubmissionNotFoundException;

import java.util.List;

public interface VcParseService {
    public Boolean getIsOver18(List<String> requestId, String transactionId) throws VPSubmissionNotFoundException;
}
