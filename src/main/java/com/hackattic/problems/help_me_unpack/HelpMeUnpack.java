package com.hackattic.problems.help_me_unpack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackattic.config.HackatticAdapter;
import com.hackattic.problems.HackAtticProblem;
import com.hackattic.problems.ProblemResult;
import com.hackattic.utils.UrlMaker;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelpMeUnpack implements HackAtticProblem {

    private final UrlMaker urlMaker;
    private final HackatticAdapter hackatticAdapter;

    static byte[] rangeOf(byte[] original, int startInclusive, int endExclusive) {
        final int length = endExclusive - startInclusive;
        byte[] intBytes = new byte[length];
        System.arraycopy(original, startInclusive, intBytes, 0/*since we are making a copy it always starts at 0 and goes till length*/, length);
        System.out.println(Arrays.toString(intBytes));
        return intBytes;
    }

    @Override
    public String problemName() {
        return "help_me_unpack";
    }


    //a regular int (signed), to start off
    //an unsigned int
    //a short (signed) to make things interesting
    //a float because floating point is important
    //a double as well
    //another double but this time in big endian (network byte order)
    //In case you're wondering, we're using 4 byte ints, so everything is in the context of a 32-bit platform.
    //
    //Extract those numbers from the byte string and send them back to the solution endpoint for your reward. See the solution section for a description of the expected JSON format.
    @SneakyThrows
    @Override
    public void solveProblem() {
        Request request = hackatticAdapter.getProblemData(urlMaker.getProblemURL(this), Request.class);
        byte[] bytes = Base64.getDecoder().decode(request.getBase64bytes());
        System.out.println("Number of bytes = " + bytes.length);
        System.out.println(Arrays.toString(bytes));
        final ByteBuffer unpackedBytes = ByteBuffer.wrap(bytes);
        /*
        final ByteBuffer signedIntBuffer = ByteBuffer.wrap(rangeOf(bytes, 0, 4));
        final ByteBuffer unSignedIntBuffer = ByteBuffer.wrap(rangeOf(bytes, 4, 8));
        // skip 2 bytes ? - short take only 2 bytes so you can do 8-10
        // but for some language there might be no short
        // so take out int(4 bytes) and cast to short as done in response object
        final ByteBuffer signedShortBuffer = ByteBuffer.wrap(rangeOf(bytes, 8, 12));

        final ByteBuffer floatBuffer = ByteBuffer.wrap(rangeOf(bytes, 12, 16));
        final ByteBuffer doubleBuffer = ByteBuffer.wrap(rangeOf(bytes, 16, 24));
        final ByteBuffer doubleBufferBigEndian = ByteBuffer.wrap(rangeOf(bytes, 24, 32));
        */

        // uses the relative position reading feature of ByteBuffer to unpack the byte[]
        final Response response = new Response(
                unpackedBytes.order(ByteOrder.LITTLE_ENDIAN).getInt(),
                Integer.toUnsignedLong(unpackedBytes.order(ByteOrder.LITTLE_ENDIAN).getInt()),
                (short) unpackedBytes.order(ByteOrder.LITTLE_ENDIAN).getInt(),
                unpackedBytes.order(ByteOrder.LITTLE_ENDIAN).getFloat(),
                unpackedBytes.order(ByteOrder.LITTLE_ENDIAN).getDouble(),
                unpackedBytes.order(ByteOrder.BIG_ENDIAN).getDouble()
        );

        log.info("response = {}", new ObjectMapper().writeValueAsString(response));
        final ProblemResult problemResult = hackatticAdapter.submitSolution(urlMaker.getSolutionURL(this, true), response);
        log.info("Result : {}", problemResult.result());
    }

}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Request {
    @JsonProperty("bytes")
    private String base64bytes;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Response {
    @JsonProperty("int")
    int signedIntBuffer;
    @JsonProperty("uint")
    long unSignedIntBuffer;
    @JsonProperty("short")
    short signedShortBuffer;
    @JsonProperty("float")
    float floatBuffer;
    @JsonProperty("double")
    double doubleBuffer;
    @JsonProperty("big_endian_double")
    double doubleBufferBigEndian;
}
