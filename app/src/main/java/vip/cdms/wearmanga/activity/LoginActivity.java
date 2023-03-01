package vip.cdms.wearmanga.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import vip.cdms.wearmanga.api.BiliAPIError;
import vip.cdms.wearmanga.api.LoginByQRCode;
import vip.cdms.wearmanga.databinding.ActivityLoginBinding;
import vip.cdms.wearmanga.utils.ActivityUtils;
import vip.cdms.wearmanga.utils.SnackbarMaker;
import vip.cdms.wearmanga.utils.TimeUtils;

public class LoginActivity extends AppCompatActivity {
    private final LoginActivity CONTEXT = this;
    private ActivityLoginBinding binding;

    private int imageHeight;

    private String qrcodeKey = null;
    private Thread listenerThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 图片宽高1:1
        binding.image.post(() -> {
            ViewGroup.LayoutParams imageLayoutParams = binding.image.getLayoutParams();
            imageLayoutParams.height = binding.image.getWidth();
            binding.image.setLayoutParams(imageLayoutParams);

            imageHeight = imageLayoutParams.height;

            binding.card.animate()
                    .translationY(-imageHeight)
                    .setDuration(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });

        // 刷新二维码
        binding.swipeRefresh.setOnRefreshListener(() -> {
            refreshQRCode();
            binding.swipeRefresh.setRefreshing(false);
        });
        binding.card.setOnClickListener(view -> refreshQRCode());

//        SnackbarMaker.makeTop(
//                binding.getRoot(),
//                "请扫码登录",
//                Snackbar.LENGTH_SHORT
//        ).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        TimeUtils.setTimeout(this::refreshQRCode, 800);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrcodeKey != null) listenerThread.interrupt();
    }

    private void hideImage() {
        runOnUiThread(() -> binding.card.animate()
                .translationY(-imageHeight)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start());
    }
    private void showImage() {
        runOnUiThread(() -> binding.card.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start());
    }

    private void refreshQRCode() {
        if (qrcodeKey != null) {
            listenerThread.interrupt();
            qrcodeKey = null;
            hideImage();
            TimeUtils.setTimeout(this::refreshQRCode, 100);
            return;
        }
        runOnUiThread(() -> {
            binding.title.setText("扫码登录");
            binding.subtitle.setText("请使用手机客户端扫码登录");
        });
        LoginByQRCode.getQRCode(new LoginByQRCode.getQRCodeCallback() {
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    new MaterialAlertDialogBuilder(CONTEXT)
                            .setMessage(e.toString())
                            .show();
                    hideImage();
                });
            }
            @Override
            public void onResponse(String url, String qrcode_key) {
                runOnUiThread(() -> {
                    binding.image.setImageBitmap(LoginByQRCode.createQRCode(url, binding.image.getWidth()));
                    showImage();
                });
                qrcodeKey = qrcode_key;
                listenerThread = new Thread(() -> {
                    for (;;) {
                        if (Thread.currentThread().isInterrupted()) {
                            qrcodeKey = null;
                            break;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            qrcodeKey = null;
                            break;
                        }

                        LoginByQRCode.login(qrcodeKey, new LoginByQRCode.loginCallback() {
                            @Override
                            public void onFailure(Exception e) {
                                if (Thread.currentThread().isInterrupted()) return;

                                if (!(e instanceof BiliAPIError)) {
                                    runOnUiThread(() -> {
                                        new MaterialAlertDialogBuilder(CONTEXT)
                                                .setMessage(e.toString())
                                                .show();
                                        hideImage();
                                    });
                                    listenerThread.interrupt();
                                    return;
                                }

                                switch (((BiliAPIError) e).getCode()) {
                                    case 86101:  // 密钥正确时但未扫描
                                        break;
                                    case 86090:  // 扫描成功但手机端未确认
                                        SnackbarMaker.makeTop(
                                                binding.getRoot(),
                                                "请确认",
                                                Snackbar.LENGTH_SHORT
                                        ).show();
                                        break;
                                    case 86038:  // 二维码失效
                                        runOnUiThread(() -> {
                                            new MaterialAlertDialogBuilder(CONTEXT)
                                                    .setTitle("二维码失效")
                                                    .setMessage("请下拉刷新二维码重试")
                                                    .show();
                                            binding.title.setText("二维码失效");
                                            binding.subtitle.setText("请下拉刷新二维码重试");
                                            hideImage();
                                        });
                                        listenerThread.interrupt();
                                        break;

                                    default:
                                        runOnUiThread(() -> {
                                            new MaterialAlertDialogBuilder(CONTEXT)
                                                    .setMessage(e.toString())
                                                    .show();
                                            hideImage();
                                        });
                                        listenerThread.interrupt();
                                        break;
                                }
                            }
                            @Override
                            public void onResponse(String DedeUserID, String DedeUserID__ckMd5, String SESSDATA, String bili_jct) {
                                SharedPreferences sharedPreferences = getSharedPreferences("bili", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("DedeUserID", DedeUserID);
                                editor.putString("DedeUserID__ckMd5", DedeUserID__ckMd5);
                                editor.putString("SESSDATA", SESSDATA);
                                editor.putString("bili_jct", bili_jct);

                                if (editor.commit()) {
                                    listenerThread.interrupt();
                                    finish();
                                    ActivityUtils.restartApp(LoginActivity.this);
                                } else ActivityUtils.alert(LoginActivity.this, null, "commit failed");
                            }
                        });
                    }
                });
                listenerThread.start();
            }
        });
    }
}