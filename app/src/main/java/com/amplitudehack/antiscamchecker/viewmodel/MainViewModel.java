package com.amplitudehack.antiscamchecker.viewmodel;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.amazonaws.services.textract.model.Block;
import com.amplitudehack.antiscamchecker.R;
import com.amplitudehack.antiscamchecker.data.RemoteRepository;
import com.amplitudehack.antiscamchecker.data.model.ChatRequest;
import com.amplitudehack.antiscamchecker.data.model.DetectionStatus;
import com.amplitudehack.antiscamchecker.data.model.GptModels;
import com.amplitudehack.antiscamchecker.data.model.MainUIState;
import com.amplitudehack.antiscamchecker.data.model.Message;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

@HiltViewModel
public class MainViewModel extends AndroidViewModel {

    private final RemoteRepository remoteRepository;
    private final Application application;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final MutableLiveData<MainUIState> liveData = new MutableLiveData<>();
    private String gptResponse, sagemakerResponse, fraudFreezeResponse, disposableEmailResponse, oopScamResponse;
    private DetectionStatus gptStatus, sagemakerStatus, fraudFreezeStatus, disposableEmailStatus, oopScamStatus;

    @Inject
    public MainViewModel(@NonNull Application application, RemoteRepository remoteRepository) {
        super(application);
        this.application = application;
        this.remoteRepository = remoteRepository;


        //callChatGpt(application.getString(R.string.test_gpt_scam));
        //callFraudFreeze(application.getString(R.string.test_gpt_scam));
        //callDisposableEmail(application.getString(R.string.test_gpt_scam));
        // callOopSpam(application.getString(R.string.test_gpt_scam));
        callSagemaker(application.getString(R.string.gpt_prompt));
    }

    public void callChatGpt(String extractedTextFromEmail) {
        List<Message> msg = new ArrayList<>();
        String formattedPrompt = application.getString(R.string.gpt_prompt) + extractedTextFromEmail;
        msg.add(0, new Message("user", formattedPrompt));
        Disposable disposable = remoteRepository.callGpt(new ChatRequest(GptModels.GPT_THREE_POINT_FIVE_TURBO.getName(), msg))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    this.gptResponse = response.getChoices().get(0).getMessage().getContent();

                    if (gptResponse.contains("Yes")) {
                        gptStatus = DetectionStatus.DETECTED;
                        liveData.setValue(MainUIState.gptDetected());
                    } else {
                        gptStatus = DetectionStatus.SAFE;
                        liveData.setValue(MainUIState.gptSafe());
                    }
                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    //{"isscam":false,"domain":"1:38!(929) 615-7686!Why is this spam?Similar
    public void callFraudFreeze(String extractedText) {
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callFraudFreeze(extractedText))
                .map(response -> {
                    Timber.d("Fraud freeze response: " + response.body().string());

                    //Gson gson = new Gson();
                    //JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                    //jsonObject.get("isscam").getAsBoolean();
                    return false;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isScam -> {
                    fraudFreezeStatus = DetectionStatus.DETECTED;
                    fraudFreezeResponse = "Scam detected. No disposable number found";
                    liveData.setValue(MainUIState.fraudFreezeDetected());
                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    //{"Score":6,"Details":{"isEmailBlocked":true,"numberOfSpamWords":0}}
    public void callOopSpam(String extractedText) {
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callOopScam(extractedText))
                .map(response -> {
                    Timber.d("Oop scam: " + response.body().string());

                    //Gson gson = new Gson();
                    //JsonObject jsonObject = gson.fromJson(response.body().charStream(), JsonObject.class);
                    //jsonObject.get("isEmailBlocked").getAsBoolean();
                    return false;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {
                    oopScamStatus = DetectionStatus.SAFE;
                    oopScamResponse = "Detection score 6/10. Not a spam email.";
                    liveData.setValue(MainUIState.oopScamSafe());
                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    //{"is_disposable_domain":false}
    public void callDisposableEmail(String extractedText) {
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callDisposableEmail(extractedText))
                .map(response -> {
                    Timber.d("Disposable email: " + response.body().string());

                    //Gson gson = new Gson();
                    //DisposableEmailResponse jsonObject = gson.fromJson(response.body().string(), DisposableEmailResponse.class);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isDisposableDomain -> {
                    if (isDisposableDomain) {
                        disposableEmailStatus = DetectionStatus.DETECTED;
                        disposableEmailResponse = "This email is a disposable email.";
                        liveData.setValue(MainUIState.disposableEmailerDetected());
                    } else {
                        disposableEmailStatus = DetectionStatus.SAFE;
                        disposableEmailResponse = "Not a disposable email.";
                        liveData.setValue(MainUIState.disposableEmailerSafe());
                    }
                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    public void callAWSTextract(String imagePath) {
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callAWSTextract(imagePath))
                .map(response -> {
                    List<String> allDetectedTextList = new ArrayList<>();
                    List<Block> blocks = response.getBlocks();

                    for (Block b : blocks) {
                        String s = b.getText();
                        if (s == null) {
                            continue;
                        }
                        allDetectedTextList.add(s);
                    }

                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < allDetectedTextList.size(); i++) {
                        sb.append(allDetectedTextList.get(i));
                    }

                    return new Pair<>(blocks, sb.toString());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    liveData.setValue(MainUIState.successCallTextract(pair.first, pair.second));
                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    public void callSagemaker(String extractedText) {
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callSagemaker(extractedText))
                .map(response -> {
                    Timber.d("Sagemaker response: " + response);

                    return false;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isScam -> {

                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    public void dispose() {
        compositeDisposable.dispose();
    }

    public MutableLiveData<MainUIState> getLiveData() {
        return liveData;
    }

    public String getGptResponse() {
        return gptResponse;
    }

    public String getSagemakerResponse() {
        return sagemakerResponse;
    }

    public String getFraudFreezeResponse() {
        return fraudFreezeResponse;
    }

    public String getDisposableEmailResponse() {
        return disposableEmailResponse;
    }

    public String getOopScamResponse() {
        return oopScamResponse;
    }

    public DetectionStatus getGptStatus() {
        return gptStatus;
    }

    public DetectionStatus getSagemakerStatus() {
        return sagemakerStatus;
    }

    public DetectionStatus getFraudFreezeStatus() {
        return fraudFreezeStatus;
    }

    public DetectionStatus getDisposableEmailStatus() {
        return disposableEmailStatus;
    }

    public DetectionStatus getOopScamStatus() {
        return oopScamStatus;
    }
}
