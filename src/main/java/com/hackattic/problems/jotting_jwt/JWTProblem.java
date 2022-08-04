package com.hackattic.problems.jotting_jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackattic.config.HackatticAdapter;
import com.hackattic.problems.HackAtticProblem;
import com.hackattic.utils.UrlMaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@RestController
@RequiredArgsConstructor
public class JWTProblem implements HackAtticProblem {

	private final HackatticAdapter hackatticAdapter;
	private final UrlMaker urlMaker;
	private final ObjectMapper objectMapper;
	private final List<String> data = new CopyOnWriteArrayList<>();
	private final Base64.Decoder decoder = Base64.getUrlDecoder();
	private String jwtSecret;

	// pre-req have a ngrok tunnel for port app is running
	// example command for port 8080 -> ngrok http --region in 8080
	@Value("hackattic.jotting_jwts.app.url")
	private String appUrl;

	@Override
	public String problemName() {
		return "jotting_jwts";
	}

	@PostMapping
	ResponseEntity postAppends(@RequestBody String jwtToken) throws Exception {

		String payload = getPayloadString(jwtToken);
		final JsonNode jsonNode = objectMapper.readValue(payload, JsonNode.class);

		final JsonNode append = jsonNode.get("append");
		if (append == null) {
			// if no append key, we send the response back to the server
			final String join = String.join("", data);
			System.out.println("join = " + join);

			return ResponseEntity.ok().body(new OutResponse(join));
		}
		// check if request is valid suing the jwtsecret
		if (!isValidToken(jwtToken)) {
			// if in-valid we do not want to append the jwt token payload
			return ResponseEntity.ok().build();
		}

		// get append-key
		final String appendText = append.asText();
		System.out.println("appendText = " + appendText);

		data.add(appendText);

		return ResponseEntity.ok().build();
	}

	private boolean isValidToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(jwtSecret); // use more secure key
			JWTVerifier verifier = JWT.require(algorithm).build(); // Reusable verifier instance
			DecodedJWT jwt = verifier.verify(token);
			return true;
		} catch (JWTVerificationException exception) {
			// Invalid signature/claims
			return false;
		}
	}

	private String getPayloadString(String jwtToken) {
		System.out.println("jwtToken = " + jwtToken);
		DecodedJWT jwt = JWT.decode(jwtToken);

		final String payloadENC = jwt.getPayload();
		String payload = new String(decoder.decode(payloadENC));
		System.out.println("payload = " + payload);
		return payload;
	}

	@Override
	public void solveProblem() {
		Request request = hackatticAdapter.getProblemData(urlMaker.getProblemURL(this), Request.class);

		this.jwtSecret = request.jwt_secret;

		hackatticAdapter.submitSolution(urlMaker.getSolutionURL(this), new Response(appUrl));
	}
}

@Data
class Request {
	String jwt_secret;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Response {
	String app_url;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class OutResponse {
	String solution;
}

