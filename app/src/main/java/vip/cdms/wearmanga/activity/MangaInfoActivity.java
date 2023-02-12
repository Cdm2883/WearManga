package vip.cdms.wearmanga.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.ComicDetail;
import vip.cdms.wearmanga.databinding.ActivityMangaInfoBinding;
import vip.cdms.wearmanga.utils.BiliCookieJar;
import vip.cdms.wearmanga.utils.DensityUtil;
import vip.cdms.wearmanga.utils.StringUtils;

import java.util.regex.Pattern;

public class MangaInfoActivity extends AppCompatActivity {
    private final MangaInfoActivity CONTEXT = this;
    private ActivityMangaInfoBinding binding;

    private int comicId;
    private JSONArray ep_list;
    private int read_epid;

    private boolean manga_cover_loaded = false;
    private boolean manga_header_loaded = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMangaInfoBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportPostponeEnterTransition();

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        comicId = getIntent().getIntExtra("comic_id", -1);

        // appBar高度全屏
        binding.appBar.post(() -> {
            ViewGroup.LayoutParams appbarLayoutParams = binding.appBar.getLayoutParams();
            appbarLayoutParams.height = getResources().getDisplayMetrics().heightPixels;
            binding.appBar.setLayoutParams(appbarLayoutParams);
        });
        // headerCard大小设置
        binding.headerCard.post(() -> {
            ViewGroup.LayoutParams cardLayoutParams = binding.headerCard.getLayoutParams();
            cardLayoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.5);
            cardLayoutParams.height = cardLayoutParams.width / 3 * 4;
            binding.headerCard.setLayoutParams(cardLayoutParams);
        });
        // 退出按钮
        View.OnClickListener exitAction = view -> {
//            binding.scrollView.smoothScrollTo(0, 0);
            binding.favFab.animate()
                    .alpha(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            ActivityCompat.finishAfterTransition(this);
        };
        binding.exitBtn.setOnClickListener(exitAction);
        // 章节列表
        View.OnClickListener chaptersListAction = view -> ChaptersListActivity.startActivity(this, comicId, ep_list, read_epid);
        binding.chaptersListBtn.setOnClickListener(chaptersListAction);

        binding.toolbar.setOnTouchListener((view, event) -> {
            // 扩大toolbar按钮触发大小
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            float touchX = event.getX();
            int toolbarWidth = binding.toolbar.getWidth();
            int dp30 = DensityUtil.dp2px(MangaInfoActivity.this, 30);

            if (touchX <= dp30) exitAction.onClick(view);
            else if (touchX >= (toolbarWidth - dp30)) chaptersListAction.onClick(view);

            binding.toolbar.performClick();
            return false;
        });

        ComicDetail.get(
                new BiliCookieJar(this),
                comicId,
                new API.JsonDataCallback() {
                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> new MaterialAlertDialogBuilder(CONTEXT)
                                .setMessage(e.toString())
                                .show());
                    }
                    @Override
                    public void onResponse(JSONObject json_root_data) {
                        favStatus = json_root_data.getInteger("fav") != 0;
                        read_epid = json_root_data.getInteger("read_epid");
                        ep_list = json_root_data.getJSONArray("ep_list");
                        runOnUiThread(() -> {
                            if (favStatus) binding.favFab.setOnClickListener(view -> favBtnNo());
                            else binding.favFab.setOnClickListener(view -> favBtnYes());

                            String mangaName = json_root_data.getString("title");
                            binding.appBarTitle.setText(mangaName);
                            binding.headerCardTitle.setText(mangaName);

                            binding.headerCardSubtitle.setText(StringUtils.join(", ", json_root_data.getJSONArray("author_name")));

                            String vertical_cover = json_root_data.getString("vertical_cover");
                            if (Pattern.compile(
                                    "^*.hdslb.com/bfs/.+/.+\\..*$"
                            ).matcher(vertical_cover).matches()) vertical_cover = vertical_cover + "@300w_400h";
                            Glide.with(binding.toolbarLayout)
                                    .setDefaultRequestOptions(new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .override(
                                                    binding.headerCard.getWidth(),
                                                    binding.headerCard.getHeight())
                                            .format(DecodeFormat.PREFER_RGB_565))
                                    .load(vertical_cover)
                                    .placeholder(R.drawable.baseline_book_24)
                                    .listener(new RequestListener<Drawable>() {
                                        private void loaded() {
                                            manga_cover_loaded = true;
                                            if (manga_header_loaded) runOnUiThread(MangaInfoActivity.this::supportStartPostponedEnterTransition);
                                        }

                                        @Override
                                        public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            loaded();
                                            return false;
                                        }
                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            loaded();
                                            return false;
                                        }
                                    })
                                    .into(binding.mangaCover);

                            Glide.with(binding.toolbarLayout)
//                                    .setDefaultRequestOptions(new RequestOptions()
//                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                            .override((int) (Target.SIZE_ORIGINAL * 0.8), (int) (Target.SIZE_ORIGINAL * 0.8))
//                                            .format(DecodeFormat.PREFER_RGB_565))
                                    .load(json_root_data.getString("horizontal_cover"))
                                    .placeholder(R.drawable.baseline_book_24)
                                    .listener(new RequestListener<Drawable>() {
                                        private void loaded() {
                                            manga_header_loaded = true;
                                            if (manga_cover_loaded) runOnUiThread(MangaInfoActivity.this::supportStartPostponedEnterTransition);
                                        }

                                        @Override
                                        public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            loaded();
                                            return false;
                                        }
                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            loaded();
                                            return false;
                                        }
                                    })
                                    .into(binding.mangaHeader);

                            StringBuilder continueReadingBtnText = new StringBuilder();
                            String json_root_data_read_short_title = json_root_data.getString("read_short_title");
                            if (json_root_data_read_short_title.isEmpty()) {
                                JSONArray json_root_data_ep_list = ep_list;
                                Object json_root_data_ep_list_last = json_root_data_ep_list.get(json_root_data_ep_list.size() - 1);
                                if (json_root_data_ep_list_last instanceof JSONArray) {
                                    JSONArray json_root_data_ep_list_last_array = (JSONArray) json_root_data_ep_list_last;
                                    JSONObject json_root_data_ep_list_last_last = json_root_data_ep_list_last_array.getJSONObject(json_root_data_ep_list_last_array.size() - 1);
                                    String json_root_data_ep_list_last_last_short_title = json_root_data_ep_list_last_last.getString("short_title");

                                    continueReadingBtnText.append("开始阅读 ");
                                    continueReadingBtnText.append(StringUtils.shortTitle(json_root_data_ep_list_last_last_short_title));
                                } else if (json_root_data_ep_list_last instanceof JSONObject) {
                                    JSONObject json_root_data_ep_list_last_last = (JSONObject) json_root_data_ep_list_last;
                                    String json_root_data_ep_list_last_last_short_title = json_root_data_ep_list_last_last.getString("short_title");

                                    continueReadingBtnText.append("开始阅读 ");
                                    continueReadingBtnText.append(StringUtils.shortTitle(json_root_data_ep_list_last_last_short_title));
                                }
                            } else {
                                continueReadingBtnText.append("续看 ");
                                continueReadingBtnText.append(StringUtils.shortTitle(json_root_data_read_short_title));
                            }
                            binding.continueReadingBtn.setText(continueReadingBtnText);
                            // todo continueReadingBtn -> [Action] <-

                            String introduction = json_root_data.getString("introduction");
                            if (introduction.trim().isEmpty()) binding.mangaInfoIntroduction.setVisibility(View.GONE);
                            else binding.mangaInfoIntroduction.setText(json_root_data.getString("introduction"));

                            binding.mangaInfoTag.setText(StringUtils.join(", ", json_root_data.getJSONArray("styles")));

                            binding.mangaInfoLastNum.setText("更新至 " + StringUtils.shortTitle(json_root_data.getString("last_short_title")));

                            String renewal_time = json_root_data.getString("renewal_time");
                            if (renewal_time.isEmpty()) ((View) binding.mangaInfoRenewalTime.getParent()).setVisibility(View.GONE);
                            else binding.mangaInfoRenewalTime.setText(json_root_data.getString("renewal_time"));
                        });
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean favStatus = false;
    private void favBtnYes() {
        favStatus = true;
        runOnUiThread(() -> {
            binding.favFab.hide();
            binding.favFab.setImageResource(R.drawable.baseline_star_24);
            binding.favFab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
            binding.favFab.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fav_fab_yes));
            binding.favFab.show();
            binding.favFab.setOnClickListener(view -> favBtnNo());
        });
    }
    private void favBtnNo() {
        favStatus = false;
        runOnUiThread(() -> {
            binding.favFab.hide();
            binding.favFab.setImageResource(R.drawable.baseline_star_border_24);
            binding.favFab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
            binding.favFab.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fav_fab_no));
            binding.favFab.show();
            binding.favFab.setOnClickListener(view -> favBtnYes());
        });
    }

    public static void startActivity(Activity activity, View cardView, int comic_id) {ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, cardView, activity.getString(R.string.app_name));
        ActivityCompat.startActivity(
                activity,
                new Intent(activity, MangaInfoActivity.class)
                        .putExtra("comic_id", comic_id),
                compat.toBundle()
        );
    }
}