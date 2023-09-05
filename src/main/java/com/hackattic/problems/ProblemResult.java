package com.hackattic.problems;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemResult {
    @JsonProperty("result")
    private String _result;
    private String rejected;
    private String message;
    private String hint;


    public String result() {
        return "\n\n"+ this;
    }
}
