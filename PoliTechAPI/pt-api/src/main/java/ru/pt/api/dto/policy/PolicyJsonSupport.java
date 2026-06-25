package ru.pt.api.dto.policy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.policyv3.PolicyDTO;

public final class PolicyJsonSupport {

    private static final ObjectMapper MAPPER = createMapper();

    private PolicyJsonSupport() {
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    public static PolicyDTO fromJson(String json) {
        try {
            return MAPPER.readValue(json, PolicyDTO.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(ErrorConstants.createErrorModel(
                    400,
                    ErrorConstants.invalidJsonFormat("policy"),
                    ErrorConstants.DOMAIN_POLICY,
                    ErrorConstants.REASON_INVALID_FORMAT,
                    "policy"
            ));
        }
    }

    public static String toJson(PolicyDTO policyDto) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(policyDto);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException(ErrorConstants.createErrorModel(
                    500,
                    "Unexpected error serializing policy: " + e.getMessage(),
                    ErrorConstants.DOMAIN_POLICY,
                    ErrorConstants.REASON_INTERNAL_ERROR,
                    "policy"
            ));
        }
    }
}
