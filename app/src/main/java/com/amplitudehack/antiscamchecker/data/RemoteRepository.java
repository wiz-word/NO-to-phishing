package com.amplitudehack.antiscamchecker.data;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.textract.AmazonTextractClient;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.util.IOUtils;
import com.amplitudehack.antiscamchecker.data.model.ChatRequest;
import com.amplitudehack.antiscamchecker.data.model.ChatResponse;
import com.amplitudehack.antiscamchecker.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class RemoteRepository {

    private final MainService mainService;

    @Inject
    public RemoteRepository(MainService mainService) {
        this.mainService = mainService;
    }

    public Single<ChatResponse> callGpt(ChatRequest chatRequest) {
        String apiKey = Constants.GPT_API_KEY;
        String authorizationHeader = "Bearer " + apiKey;
        return mainService.callChatGpt(authorizationHeader, chatRequest);
    }

    public DetectDocumentTextResult callAWSTextract(String imagePath) throws AmazonServiceException, IOException {
        String accessKey = Constants.AWS_TEXTRACT_ACCESS_KEY;
        String secretKey = Constants.AWS_TEXTRACT_SECRET_KEY;

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(300000);
        configuration.setSocketTimeout(300000);

        AmazonTextractClient client = new AmazonTextractClient(credentials, configuration);
        InputStream inputStream = new FileInputStream(new File(imagePath));
        ByteBuffer imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));

        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document().withBytes(imageBytes));

        return client.detectDocumentText(request);
    }

    public String callSagemaker(String extractedText){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Constants.API_GATEWAY_SAGEMAKER_ENDPOINT)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            return responseBody;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //https://rapidapi.com/xand3rr/api/fraudfreeze-phishing-check/
    //"google.com"
    public Response callFraudFreeze(String extractedText) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Constants.FRAUD_FREEZE_ENDPOINT)
                .get()
                .addHeader("url", extractedText)
                .addHeader("X-RapidAPI-Key", Constants.RAPID_API_KEY)
                .addHeader("X-RapidAPI-Host", "fraudfreeze-phishing-check.p.rapidapi.com")
                .build();

        return client.newCall(request).execute();
    }

    //https://rapidapi.com/tomwimmenhove/api/sentimental2/
    //badactor%40spam4.me
    public Response callDisposableEmail(String extractedText) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Constants.DISPOSABLE_EMAIL_ENDPOINT + extractedText)
                .get()
                .addHeader("X-RapidAPI-Key", Constants.RAPID_API_KEY)
                .addHeader("X-RapidAPI-Host", "disposable-email-validation.p.rapidapi.com")
                .build();
        return client.newCall(request).execute();
    }

    //https://rapidapi.com/oopspam/api/oopspam-spam-filter
    public Response callOopScam(String extractedText) throws IOException {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"checkForLength\": true,\n    \"content\": \"Dear Agent, We are a manufacturing company which specializes in supplying Aluminum Rod with Zinc Alloy Rod to customers worldwide, based in Japan, Asia. We have been unable to follow up payments effectively for transactions with debtor customers in your country due to our distant locations, thus our reason for requesting for your services representation.\",\n    \"senderIP\": \"185.234.219.246\",\n    \"email\": \"name@example.com\"\n}");
        Request request = new Request.Builder()
                .url(Constants.OOP_SPAM_ENDPOINT)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", Constants.RAPID_API_KEY)
                .addHeader("X-RapidAPI-Host", "oopspam.p.rapidapi.com")
                .build();

        return client.newCall(request).execute();
    }
}
