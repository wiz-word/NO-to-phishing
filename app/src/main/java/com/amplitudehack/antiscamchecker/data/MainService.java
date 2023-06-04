package com.amplitudehack.antiscamchecker.data;

import com.amplitudehack.antiscamchecker.data.model.ChatRequest;
import com.amplitudehack.antiscamchecker.data.model.ChatResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface MainService {
    @POST("v1/chat/completions")
    Single<ChatResponse> callChatGpt(@Header("Authorization") String authorization, @Body ChatRequest request);
}
