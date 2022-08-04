package com.hackattic.problems.readingqr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.hackattic.config.HackatticAdapter;
import com.hackattic.problems.HackAtticProblem;
import com.hackattic.problems.ProblemResult;
import com.hackattic.utils.UrlMaker;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.net.URL;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReadingQRProblem implements HackAtticProblem {

    private final UrlMaker urlMaker;
    private final HackatticAdapter hackatticAdapter;

    @Override
    public String problemName() {
        return "reading_qr";
    }

    @SneakyThrows
    private Optional<String> readQR(String imageUrl) {
        LuminanceSource imageSource = new BufferedImageLuminanceSource(ImageIO.read(new URL(imageUrl)));
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(imageSource));

        try {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return Optional.ofNullable(result.getText());
        } catch (NotFoundException e) {
            log.error("Error: ", e);
            return Optional.empty();
        }

    }

    @Override
    public void solveProblem() {
        Request request = hackatticAdapter.getProblemData(urlMaker.getProblemURL(this), Request.class);

        Optional<String> numberInQr = readQR(request.getImageUrl());

        if (numberInQr.isPresent()) {
            Response solution = new Response(numberInQr.get());

            ProblemResult problemResult = hackatticAdapter.submitSolution(urlMaker.getSolutionURL(this), solution);

            log.info("Result : {}", problemResult.getResult());
        } else {
            log.error("Error: QR code not found in image");
        }

    }
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Request {
    @JsonProperty("image_url")
    private String imageUrl;
}

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Response {
    private String code;
}

