package com.hackattic.utils;

import com.hackattic.problems.HackAtticProblem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlMaker {

    @Value("${hackattic.api.token}")
    String accessToken;

    //   https://hackattic.com/challenges/reading_qr/problem?access_token=959e221efca14d67
    public String getProblemURL(HackAtticProblem problem) {
        return "https://hackattic.com/challenges/" + problem.problemName() + "/problem?access_token=" + accessToken;
    }

    //    https://hackattic.com/challenges/reading_qr/solve?access_token=959e221efca14d67&playground=1
    // create a method that returns the URL to the solve endpoint
    public String getSolutionURL(HackAtticProblem problem) {
        return "https://hackattic.com/challenges/" + problem.problemName() + "/solve?access_token=" + accessToken;
    }

    public String getSolutionURL(HackAtticProblem problem, boolean playground) {
        return "https://hackattic.com/challenges/" + problem.problemName() + "/solve?access_token=" + accessToken + "&playground=1";
    }


}
