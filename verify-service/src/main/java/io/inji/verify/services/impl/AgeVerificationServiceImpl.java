package io.inji.verify.services.impl;

import io.inji.verify.services.AgeVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AgeVerificationServiceImpl implements AgeVerificationService {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static int calculateAge(String dob) {
        log.info("Calculating age for DOB: {}", dob);
        LocalDate birthDate = LocalDate.parse(dob, FORMATTER);
        LocalDate today = LocalDate.now();

        if (birthDate.isAfter(today)) {
            throw new IllegalArgumentException("DOB cannot be in the future");
        }

        return Period.between(birthDate, today).getYears();
    }

    @Override
    public boolean isAdult(String dob) {
        return calculateAge(dob) > 18;
    }
}
