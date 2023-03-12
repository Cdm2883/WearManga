package vip.cdms.wearmanga.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.api.*;
import vip.cdms.wearmanga.databinding.ActivityComicVerticalReaderBinding;
import vip.cdms.wearmanga.ui.CommentsView;
import vip.cdms.wearmanga.utils.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * 漫画阅读器 - 上下滚动
 */
public class ComicVerticalReaderActivity extends AppCompatActivity {
    private ActivityComicVerticalReaderBinding binding;

    private int comicId;
    private int epId;

    private boolean needBuy = false;

    private CommentsView commentsView;
    private LinearLayout imagesLayout;
    private final ArrayList<ImageView> images = new ArrayList<>();

    private SharedPreferences settings;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityComicVerticalReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        comicId = intent.getIntExtra("comic_id", -1);
        epId = intent.getIntExtra("ep_id", -1);
        epList = intent.getIntArrayExtra("ep_list");

        settings = SettingsUtils.getSettings(this);

        commentsView = binding.commentsView;
        imagesLayout = binding.images;

        // 退出按钮
        View.OnClickListener exitAction = view -> {
            //
            finish();
        };
        binding.exitBtn.setOnClickListener(exitAction);
        binding.toolbar.setOnTouchListener((view, event) -> {
            // 扩大toolbar按钮触发大小
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            float touchX = event.getX();
            int dp30 = MathUtils.dp2px(ComicVerticalReaderActivity.this, 30);

            if (touchX <= dp30) exitAction.onClick(view);

            binding.toolbar.performClick();
            return false;
        });

        binding.lastBtn.setOnClickListener(v -> last());
        binding.nextBtn.setOnClickListener(v -> next());

        ComicAPI.GetEpisode(
                new BiliCookieJar(this),
                epId,
                API.getJsonDataCallbackAutoE(this, json_root_data -> runOnUiThread(() -> binding.appBarTitle.setText(StringUtils.shortTitle(json_root_data.getString("short_title")) + " " +  json_root_data.getString("title"))))
        );

        API.JsonDataCallbackAutoE<JSONObject> jsonDataCallbackAutoE  = json_root_data -> runOnUiThread(() -> {
            int widthPixels = getResources().getDisplayMetrics().widthPixels;

            assert json_root_data != null;
            JSONArray json_root_data_images = json_root_data.getJSONArray("images");
            int index = 0;
            for (Object json_root_data_images_item_ : json_root_data_images) {
                JSONObject json_root_data_images_item = (JSONObject) json_root_data_images_item_;

                int x = json_root_data_images_item.getInteger("x");
                int y = json_root_data_images_item.getInteger("y");
                int imageHeight = (int) (MathUtils.divide(widthPixels, x, 2) * y);

                ImageView imageView;
                if (settings.getBoolean("comic_vertical_reader_image_zoom", true)) {
                    PhotoView photoView = new PhotoView(imagesLayout.getContext());
                    photoView.setScale(1);
                    photoView.setMaximumScale(settings.getInt("comic_vertical_reader_image_zoom_max", 3));
                    imageView = photoView;
                } else {
                    imageView = new ImageView(imagesLayout.getContext());
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPixels, imageHeight);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                imagesLayout.addView(imageView);
                images.add(index, imageView);

                double imageSize = settings.getInt("image_size", 100) * 0.01;
                int width;
                int height;
                if (settings.getBoolean("comic_vertical_reader_image_zoom", true)) {
                    width = (int) Math.min(widthPixels * settings.getInt("comic_vertical_reader_image_zoom_max", 3) * imageSize, x);
                    height = (int) Math.min(imageHeight * settings.getInt("comic_vertical_reader_image_zoom_max", 3) * imageSize, y);
                } else {
                    width = (int) (widthPixels * imageSize);
                    height = (int) (imageHeight * imageSize);
                }
//                        String imageUrl = json_root_data_images_item.getString("path") + "@" + width + "w_" + height + "h_" + SettingsUtils.getInt("image_quality", 100) + "q.webp";
                int imageQuality = SettingsUtils.getInt("image_quality", 100);
                String imageUrl =
                        json_root_data_images_item.getString("path") + "@"
                                + width + "w_"
                                + height + "h"
                                + (imageQuality == 100 ? "" : "_" + imageQuality + "q.webp");
                int finalIndex = index;
                ComicAPI.ImageToken(
                        new BiliCookieJar(this),
                        imageUrl,
                        API.getJsonDataCallbackAutoE(this, json_root_data1 -> runOnUiThread(() -> {
                            if (isDestroyed()) return;

                            String imageUrlPermission = ComicAPI.ImageTokenParser(json_root_data1);
                            Glide.with(imagesLayout.getContext())
                                    .setDefaultRequestOptions(new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .override(width, height)
                                            .format(DecodeFormat.PREFER_RGB_565))
                                    .load(imageUrlPermission)
                                    .placeholder(R.drawable.baseline_book_24)
                                    .into(images.get(finalIndex));
                        }))
                );
//                        ComicAPI.ImageToken(
//                                new BiliCookieJar(this),
//                                json_root_data_images_item.getString("path"),
//                                API.getJsonDataCallbackAutoE(this, json_root_data1 -> runOnUiThread(() -> {
//                                    if (isDestroyed()) return;
//
//                                    double imageSize = settings.getInt("image_size", 100) * 0.01;
//                                    int width;
//                                    int height;
//                                    if (settings.getBoolean("comic_vertical_reader_image_zoom", true)) {
//                                        width = (int) Math.min(widthPixels * settings.getInt("comic_vertical_reader_image_zoom_max", 3) * imageSize, x);
//                                        height = (int) Math.min(imageHeight * settings.getInt("comic_vertical_reader_image_zoom_max", 3) * imageSize, y);
//                                    } else {
//                                        width = (int) (widthPixels * imageSize);
//                                        height = (int) (imageHeight * imageSize);
//                                    }
//
//                                    String imageUrl = ComicAPI.ImageTokenParser(json_root_data1);
//                                    imageUrl = StringUtils.biliImageUrl(imageUrl, width, height);
//                                    Glide.with(imagesLayout.getContext())
//                                            .setDefaultRequestOptions(new RequestOptions()
//                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
////                                                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                                                    .override(width, height)
//                                                    .format(DecodeFormat.PREFER_RGB_565))
//                                            .load(imageUrl)
//                                            .placeholder(R.drawable.baseline_book_24)
//                                            .into(images.get(finalIndex));
//                                }))
//                        );

                if (needBuy && index == 0) {
                    ImageView imageViewX = new ImageView(imagesLayout.getContext());
                    imageViewX.setImageResource(R.drawable.baseline_lock_24);
                    imageViewX.setPadding(0, 8, 0, 8);
                    imagesLayout.addView(imageViewX);
                }

                index++;
            }
        });
        ComicAPI.GetImageIndex(
                new BiliCookieJar(this),
                epId,
                new API.JsonDataCallback<JSONObject>() {
                    @Override
                    public void onFailure(Exception e, JSONObject json_root) {
                        if (e instanceof BiliAPIError) {
                            needBuy = true;

                            BiliAPIError biliAPIError = (BiliAPIError) e;
                            Snackbar.make(binding.getRoot(), biliAPIError.getMessage() == null ? biliAPIError.getCodeString() : biliAPIError.getMessage(), Snackbar.LENGTH_LONG).show();
                            jsonDataCallbackAutoE.onResponse(json_root.getJSONObject("data"));
                        } else ActivityUtils.alert(ComicVerticalReaderActivity.this, e);
                    }
                    @Override
                    public void onResponse(JSONObject json_root_data) {
                        jsonDataCallbackAutoE.onResponse(json_root_data);
                    }
                }
        );

        replySort();

        BookshelfAPI.AddHistory(
                new BiliCookieJar(this),
                comicId,
                epId,
                API.getJsonDataCallbackAutoE(this, json_root_data -> {})
        );
    }

    private int replyNowSort = BiliAPI.REPLY_SORT_TIME;
    private void replySort() {
        if (!settings.getBoolean("comments_show", true)) return;
        SpannableString spannableString = new SpannableString("按热度排序 按时间排序");
        if (replyNowSort == BiliAPI.REPLY_SORT_TIME) {
            replyNowSort = BiliAPI.REPLY_SORT_REPLY;
            // 按热度排序
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 0, 5, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        } else if (replyNowSort == BiliAPI.REPLY_SORT_REPLY) {
            replyNowSort = BiliAPI.REPLY_SORT_TIME;
            // 按时间排序
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 6, 11, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 6, 11, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        runOnUiThread(() -> {
            commentsView.adapter.clearDataItems();
            commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemHeader()
                    .header("单话评论", spannableString)
                    .setOnClickListener(v -> replySort()));
        });
        replyPage(1);
    }

    private boolean firstLoadReply = true;
    private int replyNowPage;
    /**
     * 加载评论
     * @param page 页码
     */
    private void replyPage(int page) {
        replyNowPage = page;
        BiliAPI.reply(
                new BiliCookieJar(this),
                BiliAPI.REPLY_TYPE_MANGA_EPID,
                epId,
                replyNowSort,
                20,
                page,
                API.getJsonDataCallbackAutoE(this, json_root_data -> runOnUiThread(() -> {
                    if (commentsView.adapter.getItemCount() > 1) commentsView.adapter.removeDataItem(commentsView.adapter.getItemCount() - 1);

                    View.OnLongClickListener smoothScrollToTop = v -> {
                        runOnUiThread(() -> {
                            binding.scrollView.smoothScrollTo(0, (int) commentsView.getY());
                            Snackbar.make(binding.getRoot(), "已滚动到顶部", Snackbar.LENGTH_SHORT).show();
                        });
                        return true;
                    };

                    JSONArray json_root_data_replies = json_root_data.getJSONArray("replies");
                    for (Object json_root_data_replies_item_ : json_root_data_replies) {
                        JSONObject json_root_data_replies_item = (JSONObject) json_root_data_replies_item_;
                        JSONObject json_root_data_replies_item_member = json_root_data_replies_item.getJSONObject("member");

                        commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemComment(
                                json_root_data_replies_item_member.getString("avatar"),
                                json_root_data_replies_item_member.getString("uname"),
                                null,
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(json_root_data_replies_item.getLong("ctime") * 1000),
                                json_root_data_replies_item.getInteger("action") == 1,
                                json_root_data_replies_item.getInteger("like").toString()
                        ).setContentHtml(CommentsView.commentContent2Html(json_root_data_replies_item.getJSONObject("content")))
                                .setOnClickListener(v -> CommentDetailsActivity.startActivity(ComicVerticalReaderActivity.this, v, comicId, json_root_data_replies_item.getLong("rpid")))
                                .setLikeClickListener((likeLayout, beforeLiked, beforeCount, like, unlike) -> {
                                    boolean action = !beforeLiked;
                                    BiliAPI.replyAction(
                                            new BiliCookieJar(ComicVerticalReaderActivity.this),
                                            BiliAPI.REPLY_TYPE_MANGA_EPID,
                                            epId,
                                            json_root_data_replies_item.getLong("rpid"),
                                            action,
                                            API.getJsonDataCallbackAutoE(ComicVerticalReaderActivity.this, json_root_data1 -> {})
                                    );
                                    if (action) like.run();
                                    else unlike.run();
                                    return String.valueOf(Integer.parseInt(beforeCount) + (action ? 1 : -1));
                                })
                                .setOnLongClickListener(smoothScrollToTop));

                        // 加载部分子评论
                        JSONArray json_root_data_replies_item_replies = json_root_data_replies_item.getJSONArray("replies");
                        if (json_root_data_replies_item_replies != null) {
                            for (Object json_root_data_replies_item_replies_item_ : json_root_data_replies_item_replies) {
                                JSONObject json_root_data_replies_item_replies_item = (JSONObject) json_root_data_replies_item_replies_item_;
                                JSONObject json_root_data_replies_item_replies_item_member = json_root_data_replies_item_replies_item.getJSONObject("member");
                                commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemCommentChild(
                                        json_root_data_replies_item_replies_item_member.getString("avatar"),
                                        json_root_data_replies_item_replies_item_member.getString("uname"),
                                        null,
                                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(json_root_data_replies_item_replies_item.getLong("ctime") * 1000),
                                        json_root_data_replies_item_replies_item.getInteger("action") == 1,
                                        json_root_data_replies_item_replies_item.getInteger("like").toString()
                                ).setContentHtml(CommentsView.commentContent2Html(json_root_data_replies_item_replies_item.getJSONObject("content")))
                                        .setLikeClickListener((likeLayout, beforeLiked, beforeCount, like, unlike) -> {
                                            boolean action = !beforeLiked;
                                            BiliAPI.replyAction(
                                                    new BiliCookieJar(ComicVerticalReaderActivity.this),
                                                    BiliAPI.REPLY_TYPE_MANGA_EPID,
                                                    epId,
                                                    json_root_data_replies_item_replies_item.getLong("rpid"),
                                                    action,
                                                    API.getJsonDataCallbackAutoE(ComicVerticalReaderActivity.this, json_root_data1 -> {})
                                            );
                                            if (action) like.run();
                                            else unlike.run();
                                            return String.valueOf(Integer.parseInt(beforeCount) + (action ? 1 : -1));
                                        })
                                        .setOnLongClickListener(smoothScrollToTop));
                            }
                        }
                    }

                    commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemHeader()
                            .more("点击加载更多评论")
                            .setOnClickListener(v -> replyPage(replyNowPage + 1)));

                    if (page == 1 && !firstLoadReply)
                        TimeUtils.setTimeout(() -> runOnUiThread(() -> binding.scrollView.smoothScrollTo(0, (int) binding.commentsView.getY())), 0);
                    firstLoadReply = false;
                }))
        );
    }

    private int lastEpid = -1;
    private int nextEpid = -1;
    private int[] epList = null;
    private void epidLoad(Runnable runnable) {
        LogUtil.d("cvra", "");
        LogUtil.d("cvra", "lastEpid: " + lastEpid);
        LogUtil.d("cvra", "nextEpid: " + nextEpid);
        LogUtil.d("cvra", "epid: " + epId);
        LogUtil.d("cvra", "epList: " + Arrays.toString(epList));
        if (lastEpid != -1 || nextEpid != -1) {
            runnable.run();
            return;
        }
        if (epList != null) {
            for (int i = 0; i < epList.length; i++) {
                int nowEpid = epList[i];
                if (nowEpid != epId) continue;
                if (i > 0) lastEpid = epList[i - 1];
                if (i != (epList.length - 1)) nextEpid = epList[i + 1];
                break;
            }
            runnable.run();
            return;
        }
        ComicAPI.ComicDetail(
                new BiliCookieJar(this),
                comicId,
                API.getJsonDataCallbackAutoE(this, json_root_data -> {
                    JSONArray ep_list_json = json_root_data.getJSONArray("ep_list");

                    ArrayList<Integer> epListArrayList = new ArrayList<>();
                    if (ep_list_json.get(0) instanceof JSONArray) {
                        for (int i = ep_list_json.size() - 1; i >= 0; i--) {
                            JSONArray arrayItem = (JSONArray) ep_list_json.get(i);
                            for (int j = arrayItem.size() - 1; j >= 0; j--) epListArrayList.add(((JSONObject) arrayItem.get(i)).getInteger("id"));
                        }
                    } else if (ep_list_json.get(0) instanceof JSONObject) {
                        for (int i = ep_list_json.size() - 1; i >= 0; i--) epListArrayList.add(((JSONObject) ep_list_json.get(i)).getInteger("id"));
                    }

                    epList = new int[epListArrayList.size()];
                    for (int i = 0; i < epListArrayList.size(); i++) {
                        int nowEpid = epListArrayList.get(i);
                        epList[i] = nowEpid;
                        if (nowEpid != epId) continue;
                        if (i > 0) lastEpid = epListArrayList.get(i - 1);
                        if (i != (epListArrayList.size() - 1)) nextEpid = epListArrayList.get(i + 1);
//                        break;
                    }

                    runnable.run();
                })
        );
    }
    private void last() {
        epidLoad(() -> runOnUiThread(() -> {
            if (lastEpid == -1) SnackbarMaker.makeTop(binding.getRoot(), "没有上一话了", Snackbar.LENGTH_LONG).show();
            else {
                startActivity(this, comicId, lastEpid, epList);
                finish();
            }
        }));
    }
    private void next() {
        epidLoad(() -> runOnUiThread(() -> {
            if (nextEpid == -1) SnackbarMaker.makeTop(binding.getRoot(), "没有下一话了", Snackbar.LENGTH_LONG).show();
            else {
                startActivity(this, comicId, nextEpid, epList);
                finish();
            }
        }));
    }

    public static void startActivity(Activity activity, int comic_id, int ep_id, int[] ep_list) {
        activity.startActivity(
                new Intent(activity, ComicVerticalReaderActivity.class)
                        .putExtra("comic_id", comic_id)
                        .putExtra("ep_id", ep_id)
                        .putExtra("ep_list", ep_list)
        );
    }
    public static void startActivity(Activity activity, int comic_id, int ep_id) {
        startActivity(activity, comic_id, ep_id, null);
    }
}
