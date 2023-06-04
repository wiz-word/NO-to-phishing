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

    @Inject
    public MainViewModel(@NonNull Application application, RemoteRepository remoteRepository) {
        super(application);
        this.application = application;
        this.remoteRepository = remoteRepository;


        //callChatGpt(application.getString(R.string.test_gpt_scam));
        //callFraudFreeze();
        //callDisposableEmail();
        callOopSpam();
    }

    public void callChatGpt(String extractedTextFromEmail) {
        List<Message> msg = new ArrayList<>();
        String formattedPrompt = application.getString(R.string.gpt_prompt) + extractedTextFromEmail;
        msg.add(0, new Message("user", formattedPrompt));
        Disposable disposable = remoteRepository.callGpt(new ChatRequest(GptModels.GPT_THREE_POINT_FIVE_TURBO.getName(), msg))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    String s = response.getChoices().get(0).getMessage().getContent();

                    if (s.contains("Yes")) {
                        //Likely a scam
                    }

                    liveData.setValue(MainUIState.successCallChatGpt(s));
                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    public void callFraudFreeze(){
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callFraudFreeze())
                .map(response -> {
                    Timber.d("Fraud freeze response: " + response.body().string());
                    return "";
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {

                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    public void callOopSpam(){
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callOopScam())
                .map(response -> {
                    Timber.d("Disposable email: " + response.body().string());
                    return "";
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {

                }, err -> {
                    Timber.e(err);
                });

        compositeDisposable.add(disposable);
    }

    public void callDisposableEmail(){
        Disposable disposable = Single.fromCallable(() -> remoteRepository.callDisposableEmail())
                .map(response -> {
                    Timber.d("Disposable email: " + response.body().string());
                    return "";
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> {

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

    public void dispose() {
        compositeDisposable.dispose();
    }

    public MutableLiveData<MainUIState> getLiveData() {
        return liveData;
    }
}
