package com.amplitudehack.antiscamchecker.ui;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.BoundingBox;
import com.amplitudehack.antiscamchecker.R;
import com.amplitudehack.antiscamchecker.data.model.DetectionStatus;
import com.amplitudehack.antiscamchecker.utils.ExifUtil;
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
import java.util.List;

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

    private MainViewModel viewModel;
    private Paint paint;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Glide.with(this)
                .load(R.drawable.app_logo_2)
                .apply(RequestOptions.centerInsideTransform())
                .into(ivAppLogo);

        paint = new Paint();
        paint.setColor(ContextCompat.getColor(this, R.color.standardRed));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getLiveData().observe(this, mainUIState -> {
            switch (mainUIState.getState()) {
                case TEXTRACT_SUCCESS:
                    Timber.d("Textract success: " + mainUIState);

                    layoutGpt.setVisibility(View.VISIBLE);
                    layoutSagemaker.setVisibility(View.VISIBLE);
                    layoutFraudFreeze.setVisibility(View.VISIBLE);
                    layoutDisposableEmail.setVisibility(View.VISIBLE);
                    layoutOopScam.setVisibility(View.VISIBLE);


                    //Prevents image shrinking when reloading
                    ivSelectedImage.layout(0, 0, 0, 0);
                    Glide.with(this)
                            .asBitmap()
                            .load(drawBoxesAroundText(imagePath, mainUIState.getBlockList()))
                            .apply(RequestOptions.fitCenterTransform())
                            .into(ivSelectedImage);

                    String extractedText = mainUIState.getExtractedText();

                    viewModel.callChatGpt(extractedText);
                    viewModel.callSageMaker(extractedText);
                    viewModel.callFraudFreeze(extractedText);
                    viewModel.callDisposableEmail(extractedText);
                    viewModel.callOopSpam(extractedText);
                    break;

                case GPT_SAFE:
                    tvSafeGpt.setVisibility(View.VISIBLE);
                    tvDetectedGpt.setVisibility(View.GONE);
                    progressGpt.setVisibility(View.GONE);
                    break;

                case GPT_DETECTED:
                    tvDetectedGpt.setVisibility(View.VISIBLE);
                    tvSafeGpt.setVisibility(View.GONE);
                    progressGpt.setVisibility(View.GONE);
                    break;

                case SAGEMAKER_SAFE:
                    tvSafeSagemaker.setVisibility(View.VISIBLE);
                    tvDetectedSagemaker.setVisibility(View.GONE);
                    progressSagemaker.setVisibility(View.GONE);
                    break;

                case SAGEMAKER_DETECTED:
                    tvDetectedSagemaker.setVisibility(View.VISIBLE);
                    tvSafeSagemaker.setVisibility(View.GONE);
                    progressSagemaker.setVisibility(View.GONE);
                    break;

                case FRAUD_FREEZE_SAFE:
                    tvSafeFraudFreeze.setVisibility(View.VISIBLE);
                    tvDetectedFraudFreeze.setVisibility(View.GONE);
                    progressFraudFreeze.setVisibility(View.GONE);
                    break;

                case FRAUD_FREEZE_DETECTED:
                    tvDetectedFraudFreeze.setVisibility(View.VISIBLE);
                    tvSafeFraudFreeze.setVisibility(View.GONE);
                    progressFraudFreeze.setVisibility(View.GONE);
                    break;

                case DISPOSABLE_EMAILER_SAFE:
                    tvSafeDisposableEmail.setVisibility(View.VISIBLE);
                    tvDetectedDisposableEmail.setVisibility(View.GONE);
                    progressDisposableEmail.setVisibility(View.GONE);
                    break;

                case DISPOSABLE_EMAILER_DETECTED:
                    tvDetectedDisposableEmail.setVisibility(View.VISIBLE);
                    tvSafeDisposableEmail.setVisibility(View.GONE);
                    progressDisposableEmail.setVisibility(View.GONE);
                    break;

                case OOP_SCAM_SAFE:
                    tvSafeOopScam.setVisibility(View.VISIBLE);
                    tvDetectedOopScam.setVisibility(View.GONE);
                    progressOopScam.setVisibility(View.GONE);
                    break;

                case OOP_SCAM_DETECTED:
                    tvDetectedOopScam.setVisibility(View.VISIBLE);
                    tvSafeOopScam.setVisibility(View.GONE);
                    progressOopScam.setVisibility(View.GONE);
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
                        imagePath = result.get(0).getRealPath();

                        btnTakePhoto.setVisibility(View.GONE);
                        btnChooseFile.setVisibility(View.GONE);
                        tvUploadWarning.setVisibility(View.GONE);
                        ivSelectedImage.setVisibility(View.VISIBLE);

                        //Prevents image shrinking when reloading
                        ivSelectedImage.layout(0, 0, 0, 0);
                        Glide.with(MainActivity.this)
                                .load(imagePath)
                                .apply(RequestOptions.fitCenterTransform())
                                .into(ivSelectedImage);

                        viewModel.callAWSTextract(imagePath);
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

    private Bitmap drawBoxesAroundText(String imagePath, List<Block> blockList) {
        Bitmap immutableBitmap = BitmapFactory.decodeFile(imagePath);
        //ExifUtil rotates to correct orientation after .copy changes it.
        Bitmap bitmap = ExifUtil.rotateBitmap(imagePath, immutableBitmap.copy(Bitmap.Config.ARGB_8888, true));
        Canvas canvas = new Canvas(bitmap);
        for (Block block : blockList) {
            if (block.getBlockType().equals("LINE")) {
                BoundingBox box = block.getGeometry().getBoundingBox();
                int newLeft = Math.round(bitmap.getWidth() * box.getLeft());
                int newTop = Math.round(bitmap.getHeight() * box.getTop());
                int newWidth = Math.round(bitmap.getWidth() * box.getWidth());
                int newHeight = Math.round(bitmap.getHeight() * box.getHeight());
                canvas.drawRect(
                        newLeft,
                        newTop,
                        newWidth + newLeft,
                        newHeight + newTop,
                        paint
                );
            }
        }
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.dispose();
    }

    private void initReadMoreFragment(DetectionStatus status, String msg) {
        DialogFragment dialogFragment = new ReadMoreFragment(status, msg);
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    @OnClick({
            R.id.tv_safe_gpt, R.id.tv_safe_sagemaker, R.id.tv_safe_fraud_freeze, R.id.tv_safe_disposable_email, R.id.tv_safe_oop_scam,
            R.id.tv_detected_gpt, R.id.tv_detected_sagemaker, R.id.tv_detected_fraud_freeze, R.id.tv_detected_disposable_email, R.id.tv_detected_oop_scam
    })
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_safe_gpt:

            case R.id.tv_detected_gpt:
                Timber.d("Gpt status: " + viewModel.getGptStatus());
                initReadMoreFragment(viewModel.getGptStatus(), viewModel.getGptResponse());
                break;

            case R.id.tv_safe_sagemaker:

            case R.id.tv_detected_sagemaker:
                initReadMoreFragment(viewModel.getSagemakerStatus(), viewModel.getSagemakerResponse());
                break;

            case R.id.tv_safe_fraud_freeze:

            case R.id.tv_detected_fraud_freeze:
                initReadMoreFragment(viewModel.getFraudFreezeStatus(), viewModel.getFraudFreezeResponse());
                break;

            case R.id.tv_safe_disposable_email:

            case R.id.tv_detected_disposable_email:
                initReadMoreFragment(viewModel.getDisposableEmailStatus(), viewModel.getDisposableEmailResponse());
                break;

            case R.id.tv_safe_oop_scam:

            case R.id.tv_detected_oop_scam:
                initReadMoreFragment(viewModel.getOopScamStatus(), viewModel.getOopScamResponse());
                break;
        }
    }

    @BindView(R.id.layout_gpt)
    ConstraintLayout layoutGpt;

    @BindView(R.id.layout_sagemaker)
    ConstraintLayout layoutSagemaker;

    @BindView(R.id.layout_fraud_freeze)
    ConstraintLayout layoutFraudFreeze;

    @BindView(R.id.layout_disposable_email)
    ConstraintLayout layoutDisposableEmail;

    @BindView(R.id.layout_oop_scam)
    ConstraintLayout layoutOopScam;

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

    @BindView(R.id.tv_detected_gpt)
    TextView tvDetectedGpt;

    @BindView(R.id.tv_detected_sagemaker)
    TextView tvDetectedSagemaker;

    @BindView(R.id.tv_detected_fraud_freeze)
    TextView tvDetectedFraudFreeze;

    @BindView(R.id.tv_detected_disposable_email)
    TextView tvDetectedDisposableEmail;

    @BindView(R.id.tv_detected_oop_scam)
    TextView tvDetectedOopScam;

    @BindView(R.id.tv_safe_gpt)
    TextView tvSafeGpt;

    @BindView(R.id.tv_safe_sagemaker)
    TextView tvSafeSagemaker;

    @BindView(R.id.tv_safe_fraud_freeze)
    TextView tvSafeFraudFreeze;

    @BindView(R.id.tv_safe_disposable_email)
    TextView tvSafeDisposableEmail;

    @BindView(R.id.tv_safe_oop_scam)
    TextView tvSafeOopScam;

    @BindView(R.id.progress_gpt)
    ProgressBar progressGpt;

    @BindView(R.id.progress_sagemaker)
    ProgressBar progressSagemaker;

    @BindView(R.id.progress_fraud_freeze)
    ProgressBar progressFraudFreeze;

    @BindView(R.id.progress_disposable_email)
    ProgressBar progressDisposableEmail;

    @BindView(R.id.progress_oop_scam)
    ProgressBar progressOopScam;
}