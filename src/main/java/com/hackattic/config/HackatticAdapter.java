package com.hackattic.config;

import com.hackattic.problems.ProblemResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class HackatticAdapter {

    private final RestTemplate restTemplate = new RestTemplate();

    public <T> T getProblemData(String url, Class<T> responseClass) {
        ResponseEntity<T> requestEntity = restTemplate.getForEntity(url, responseClass);

        return requestEntity.getBody();
    }

    public <T> ProblemResult submitSolution(String url, T responseData) {
        ResponseEntity<ProblemResult> requestEntity = restTemplate.postForEntity(url, responseData, ProblemResult.class);

        return requestEntity.getBody();
    }


}
