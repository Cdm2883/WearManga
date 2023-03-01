package vip.cdms.wearmanga.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.BiliAPI;
import vip.cdms.wearmanga.databinding.ActivityCommentDetailsBinding;
import vip.cdms.wearmanga.ui.CommentsView;
import vip.cdms.wearmanga.utils.BiliCookieJar;
import vip.cdms.wearmanga.utils.DensityUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 评论详情Activity
 */
public class CommentDetailsActivity extends AppCompatActivity {
    private ActivityCommentDetailsBinding binding;

    private int comicId;
    private long rpid;

    private CommentsView commentsView;

    private boolean loadedRootComment = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCommentDetailsBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportPostponeEnterTransition();
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        comicId = getIntent().getIntExtra("comic_id", -1);
        rpid = getIntent().getLongExtra("rpid", -1);

        // 退出按钮
        View.OnClickListener exitAction = view -> {
            //
            ActivityCompat.finishAfterTransition(this);
        };
        binding.exitBtn.setOnClickListener(exitAction);
        binding.toolbar.setOnTouchListener((view, event) -> {
            // 扩大toolbar按钮触发大小
            if (event.getAction() != MotionEvent.ACTION_UP) return false;

            float touchX = event.getX();
            int dp30 = DensityUtil.dp2px(CommentDetailsActivity.this, 30);

            if (touchX <= dp30) exitAction.onClick(view);

            binding.toolbar.performClick();
            return false;
        });

        commentsView = binding.commentsView;

        binding.fabScrollToTop.setOnClickListener(view -> commentsView.smoothScrollToPosition(0));
        binding.fabScrollToTop.post(() -> binding.fabScrollToTop.hide());
        commentsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (commentsView._getScrollY() > 10) runOnUiThread(() -> binding.fabScrollToTop.show());
                else runOnUiThread(() -> binding.fabScrollToTop.hide());
            }
        });

        replyPage(1);
    }

    private int replyNowPage;
    /**
     * 加载评论
     * @param page 页码
     */
    private void replyPage(int page) {
        replyNowPage = page;
        BiliAPI.replyReply(
                new BiliCookieJar(this),
                BiliAPI.REPLY_TYPE_MANGA_MCID,
                comicId,
                rpid,
                20,
                page,
                API.getJsonDataCallbackAutoE(this, json_root_data -> runOnUiThread(() -> {
                    if (commentsView.adapter.getItemCount() > 1) commentsView.adapter.removeDataItem(commentsView.adapter.getItemCount() - 1);

                    if (!loadedRootComment) {
                        JSONObject json_root_data_root = json_root_data.getJSONObject("root");
                        JSONObject json_root_data_root_member = json_root_data_root.getJSONObject("member");

                        commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemComment(
                                json_root_data_root_member.getString("avatar"),
                                json_root_data_root_member.getString("uname"),
                                null,
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(json_root_data_root.getLong("ctime") * 1000),
                                json_root_data_root.getInteger("action") == 1,
                                /*json_root_data_root.getInteger("like").toString()*/null
                        ).setContentHtml(CommentsView.commentContent2Html(json_root_data_root.getJSONObject("content"))));
                        commentsView.post(() -> {
                            commentsView.getLayoutManager().getChildAt(0).setTransitionName(getString(R.string.app_name));
                            supportStartPostponedEnterTransition();
                        });
                        loadedRootComment = true;
                    }

                    JSONArray json_root_data_replies = json_root_data.getJSONArray("replies");
                    if (json_root_data_replies == null) return;
                    for (Object json_root_data_replies_item_ : json_root_data_replies) {
                        JSONObject json_root_data_replies_item = (JSONObject) json_root_data_replies_item_;
                        JSONObject json_root_data_replies_item_member = json_root_data_replies_item.getJSONObject("member");
                        commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemCommentChild(
                                json_root_data_replies_item_member.getString("avatar"),
                                json_root_data_replies_item_member.getString("uname"),
                                null,
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(json_root_data_replies_item.getLong("ctime") * 1000),
                                json_root_data_replies_item.getInteger("action") == 1,
                                json_root_data_replies_item.getInteger("like").toString()
                        ).setContentHtml(CommentsView.commentContent2Html(json_root_data_replies_item.getJSONObject("content"))));
                    }

                    commentsView.adapter.addDataItem(new CommentsView.CommentsViewAdapter.DataItemHeader()
                            .more("点击加载更多评论")
                            .setOnClickListener(v -> replyPage(replyNowPage + 1)));
                }))
        );
    }

    public static void startActivity(Activity activity, View commentItem, int comic_id, long rpid) {
        ActivityOptionsCompat compat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        commentItem,
                        activity.getString(R.string.app_name)
                );
        ActivityCompat.startActivity(
                activity,
                new Intent(activity, CommentDetailsActivity.class)
                        .putExtra("comic_id", comic_id)
                        .putExtra("rpid", rpid),
                compat.toBundle()
        );
    }
}
