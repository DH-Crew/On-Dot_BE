package com.dh.ondot.schedule.infra.config;

import com.dh.ondot.schedule.infra.dto.OdsayErrorResponse;
import com.dh.ondot.schedule.infra.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class OdsayRestClientConfig {

    @Bean
    @Qualifier("odsayRestClient")
    public RestClient odsayRestClient(OdsayApiConfig props) {
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    ObjectMapper mapper = new ObjectMapper();
                    OdsayErrorResponse ers = mapper.readValue(response.getBody(), OdsayErrorResponse.class);

                    OdsayErrorResponse.Error err = ers.error().get(0);
                    String code = err.code();
                    String msg  = err.message();

                    switch (code) {
                        case "500" -> throw new OdsayServerErrorException(msg);
                        case "-8" -> throw new OdsayBadInputException(msg);
                        case "-9" -> throw new OdsayMissingParamException(msg);
                        case "3", "4", "5" -> throw new OdsayNoStopException(msg);
                        case "6" -> throw new OdsayServiceAreaException(msg);
                        case "-98" -> throw new OdsayTooCloseException(msg);
                        case "-99" -> throw new OdsayNoResultException(msg);
                        default -> throw new OdsayUnhandledException(msg);
                    }
                })
                .build();
    }
}
