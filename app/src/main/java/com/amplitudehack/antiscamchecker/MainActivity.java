package com.amplitudehack.antiscamchecker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amplitudehack.antiscamchecker.utils.GlideEngine;
import com.amplitudehack.antiscamchecker.viewmodel.MainViewModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.hilt.android.AndroidEntryPoint;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_app_logo)
    ImageView ivAppLogo;

    @BindView(R.id.iv_selected_image)
    ImageView ivSelectedImage;

    @BindView(R.id.btn_take_photo)
    MaterialButton btnTakePhoto;

    @BindView(R.id.btn_choose_file)
    MaterialButton btnChooseFile;

    @BindView(R.id.tv_static_upload_warning)
    TextView tvUploadWarning;

    private MainViewModel viewModel;
    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Glide.with(this)
                .load(R.drawable.app_logo)
                .apply(RequestOptions.centerInsideTransform())
                .into(ivAppLogo);

        paint = new Paint();
        paint.setColor(ContextCompat.getColor(this, R.color.standardRed));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        Glide.with(this)
                .load(R.drawable.app_logo)
                .apply(RequestOptions.centerInsideTransform())
                .into(ivAppLogo);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getLiveData().observe(this, mainUIState -> {
            switch (mainUIState.getState()) {
                case TEXTRACT_SUCCESS:
                    //viewModel.callChatGpt(mainUIState.getConcatenatedText());
                    break;

                case CALL_GPT_SUCCESS:
                    Timber.d("Gpt response: " + mainUIState.getChatGptResponse());
                    break;

                case ERROR:
                    Toast.makeText(this, mainUIState.getErrMsg(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void selectPhoto() {
        PictureSelector.create(this)
                .openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        String path = result.get(0).getRealPath();

                        btnTakePhoto.setVisibility(View.GONE);
                        btnChooseFile.setVisibility(View.GONE);
                        tvUploadWarning.setVisibility(View.GONE);
                        ivSelectedImage.setVisibility(View.VISIBLE);

                        //Prevents image shrinking when reloading
                        ivSelectedImage.layout(0, 0, 0, 0);
                        Glide.with(MainActivity.this)
                                .load(path)
                                .apply(RequestOptions.fitCenterTransform())
                                .into(ivSelectedImage);

                        viewModel.callAWSTextract(path);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onPermissionDenied() {
        Timber.e("Permission denied");
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onPermissionNeverAskAgain() {
        Toast.makeText(this, "Must enable read storage permissions in settings", Toast.LENGTH_LONG).show();
    }

    @OnClick({R.id.btn_take_photo, R.id.btn_choose_file, R.id.iv_selected_image})
    void onSelectPhoto() {
        MainActivityPermissionsDispatcher.selectPhotoWithPermissionCheck(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.dispose();
    }
}