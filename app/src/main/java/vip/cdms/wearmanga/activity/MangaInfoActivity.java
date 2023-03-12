package vip.cdms.wearmanga.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
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
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.BiliAPI;
import vip.cdms.wearmanga.api.BookshelfAPI;
import vip.cdms.wearmanga.api.ComicAPI;
import vip.cdms.wearmanga.databinding.ActivityMangaInfoBinding;
import vip.cdms.wearmanga.ui.AppBarStateChangeListener;
import vip.cdms.wearmanga.ui.CommentsView;
import vip.cdms.wearmanga.ui.MangaListAdapter;
import vip.cdms.wearmanga.utils.*;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class MangaInfoActivity extends AppCompatActivity {
    private final MangaInfoActivity CONTEXT = this;
    private ActivityMangaInfoBinding binding;

    /** appbar是否展开 */
    private boolean appbarIsExpanded = true;

    private int comicId;
    private JSONArray ep_list;
    private int read_epid;

    private boolean manga_cover_loaded = false;
    private boolean manga_header_loaded = false;

    private MangaListAdapter mangaListAdapter;

    private CommentsView commentsView;

    private boolean scrollLoadMoreRecommend = false;
    private boolean scrollLoadReply = false;

    private SharedPreferences settings;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMangaInfoBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportPostponeEnterTransition();

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        comicId = getIntent().getIntExtra("comic_id", -1);

        settings = SettingsUtils.getSettings(this);
        if (!settings.getBoolean("comments_show", true)) binding.commentsHeader.setVisibility(View.GONE);

        // appBar高度全屏
        binding.appBar.post(() -> {
            ViewGroup.LayoutParams appbarLayoutParams = binding.appBar.getLayoutParams();
            appbarLayoutParams.height = getResources().getDisplayMetrics().heightPixels;
            binding.appBar.setLayoutParams(appbarLayoutParams);
        });
        // appBar展开监听
        binding.appBar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appbarIsExpanded = (state == State.EXPANDED);
            }
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
            binding.favFab.animate()
                    .alpha(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            if (appbarIsExpanded)
                ActivityCompat.finishAfterTransition(this);
            else {
                binding.appBar.setExpanded(true);
                TimeUtils.setTimeout(() -> runOnUiThread(() -> {
                    binding.favFab.setVisibility(View.GONE);
                    ActivityCompat.finishAfterTransition(CONTEXT);
                }), 200);
            }
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
            int dp30 = MathUtils.dp2px(CONTEXT, 30);

            if (touchX <= dp30) exitAction.onClick(view);
            else if (touchX >= (toolbarWidth - dp30)) chaptersListAction.onClick(view);

            binding.toolbar.performClick();
            return false;
        });

        loadInfo();

        // 配置RecyclerView
        RecyclerView recyclerView = binding.recyclerViewManga;
        mangaListAdapter = new MangaListAdapter(recyclerView);
        mangaListAdapter.setLayoutHorizontal((int) (getResources().getDisplayMetrics().widthPixels * 0.5 / 3 * 4));
        mangaListAdapter.setItemDecoration(new MangaListAdapter.ItemDecoration(this));

        // 配置评论
        commentsView = binding.commentsView;
        binding.commentsHeader.setOnClickListener(v -> replySort());

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) */binding.scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int needScrollY = scrollY + getResources().getDisplayMetrics().heightPixels;
            if (needScrollY >= binding.recyclerViewManga.getY() && !scrollLoadMoreRecommend) {
                scrollLoadMoreRecommend = true;
                loadMoreRecommend();
            }
            if (needScrollY >= binding.commentsHeader.getY() && !scrollLoadReply) {
                scrollLoadReply = true;
                replySort();
            }
            if (scrollLoadMoreRecommend && scrollLoadReply) binding.scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) null);
        });/* else {
            loadMoreRecommend();
            replySort();
        }*/

        // todo delete :)
        binding.headerCard.setOnClickListener(v -> v.animate()
//                    .rotation(new Random().nextInt(360))
//                    .scaleX((float) (new Random().nextInt(50) * 0.01 + 0.5))
//                    .scaleY((float) (new Random().nextInt(50) * 0.01 + 0.5))
                .x(new Random().nextInt(getResources().getDisplayMetrics().widthPixels - v.getWidth()))
                .y(new Random().nextInt(getResources().getDisplayMetrics().heightPixels - v.getHeight()))
                .start());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadInfo();
    }

    @SuppressLint("SetTextI18n")
    private void loadInfo() {
        ComicAPI.ComicDetail(
                new BiliCookieJar(this),
                comicId,
                API.getJsonDataCallbackAutoE(CONTEXT, json_root_data -> {
                    favStatus = json_root_data.getInteger("fav") != 0;
                    read_epid = json_root_data.getInteger("read_epid");
                    ep_list = json_root_data.getJSONArray("ep_list");
                    runOnUiThread(() -> {
                        favStatus(favStatus, false);

                        String mangaName = json_root_data.getString("title");
                        binding.appBarTitle.setText(mangaName);
                        binding.headerCardTitle.setText(mangaName);

                        binding.headerCardSubtitle.setText(StringUtils.join(", ", json_root_data.getJSONArray("author_name")));

                        double imageSize = settings.getInt("image_size", 100) * 0.01;
                        String vertical_cover = json_root_data.getString("vertical_cover");
                        vertical_cover = StringUtils.biliImageUrl(vertical_cover, 300, 400);
                        Glide.with(binding.toolbarLayout)
                                .setDefaultRequestOptions(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .override((int) (300 * imageSize), (int) (400 * imageSize))
                                        .format(DecodeFormat.PREFER_RGB_565))
                                .load(vertical_cover)
                                .placeholder(R.drawable.baseline_book_24)
                                .listener(new RequestListener<Drawable>() {
                                    private void loaded() {
                                        manga_cover_loaded = true;
                                        if (manga_header_loaded) runOnUiThread(CONTEXT::supportStartPostponedEnterTransition);
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
                                        if (manga_cover_loaded) runOnUiThread(CONTEXT::supportStartPostponedEnterTransition);
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


                        int continueReadingEpid = json_root_data.getInteger("read_epid");
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
                                continueReadingEpid = json_root_data_ep_list_last_last.getInteger("id");
                            } else if (json_root_data_ep_list_last instanceof JSONObject) {
                                JSONObject json_root_data_ep_list_last_last = (JSONObject) json_root_data_ep_list_last;
                                String json_root_data_ep_list_last_last_short_title = json_root_data_ep_list_last_last.getString("short_title");

                                continueReadingBtnText.append("开始阅读 ");
                                continueReadingBtnText.append(StringUtils.shortTitle(json_root_data_ep_list_last_last_short_title));
                                continueReadingEpid = json_root_data_ep_list_last_last.getInteger("id");
                            }
                        } else {
                            continueReadingBtnText.append("续看 ");
                            continueReadingBtnText.append(StringUtils.shortTitle(json_root_data_read_short_title));
                        }
                        int finalContinueReadingEpid = continueReadingEpid;
                        binding.continueReadingBtn.setText(continueReadingBtnText);
                        binding.continueReadingBtn.setOnClickListener(v -> ActivityUtils.comicReader(CONTEXT, json_root_data.getInteger("orientation"), comicId, finalContinueReadingEpid));

                        String introduction = json_root_data.getString("introduction");
                        if (introduction.trim().isEmpty()) binding.mangaInfoIntroduction.setVisibility(View.GONE);
                        else binding.mangaInfoIntroduction.setText(json_root_data.getString("introduction"));

                        binding.mangaInfoTag.setText(StringUtils.join(", ", json_root_data.getJSONArray("styles")));

                        binding.mangaInfoLastNum.setText("更新至 " + StringUtils.shortTitle(json_root_data.getString("last_short_title")));

                        String renewal_time = json_root_data.getString("renewal_time");
                        if (renewal_time.isEmpty()) ((View) binding.mangaInfoRenewalTime.getParent()).setVisibility(View.GONE);
                        else binding.mangaInfoRenewalTime.setText(json_root_data.getString("renewal_time"));
                    });
                })
        );
    }

    private boolean favStatus = false;
    private void favStatus(Boolean status, boolean onEvent) {
        if (status == null) favStatus = !favStatus;
        else favStatus = status;

        runOnUiThread(() -> {
            if (favStatus) {
                binding.favFab.hide();
                binding.favFab.setImageResource(R.drawable.baseline_star_24);
                binding.favFab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                binding.favFab.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fav_fab_yes));
                binding.favFab.show();
                binding.favFab.setOnClickListener(view -> favStatus(false, true));
            } else {
                binding.favFab.hide();
                binding.favFab.setImageResource(R.drawable.baseline_star_border_24);
                binding.favFab.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                binding.favFab.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.fav_fab_no));
                binding.favFab.show();
                binding.favFab.setOnClickListener(view -> favStatus(true, true));
            }
        });

        if (!onEvent) return;
        if (favStatus) BookshelfAPI.AddFavorite(new BiliCookieJar(this), comicId, API.getJsonDataCallbackAutoE(this, json_root_data -> {}));
        else BookshelfAPI.DeleteFavorite(new BiliCookieJar(this), comicId, API.getJsonDataCallbackAutoE(this, json_root_data -> {}));
    }

    private void loadMoreRecommend() {
        ComicAPI.MoreRecommend(
                new BiliCookieJar(this),
                comicId,
                API.getJsonDataCallbackAutoE(CONTEXT, json_root_data -> runOnUiThread(() -> {
                    JSONArray json_root_data_recommend_comics = json_root_data.getJSONArray("recommend_comics");
                    for (Object json_root_data_recommend_comics_item_ : json_root_data_recommend_comics) {
                        if (!(json_root_data_recommend_comics_item_ instanceof JSONObject)) continue;
                        JSONObject json_root_data_recommend_comics_item = (JSONObject) json_root_data_recommend_comics_item_;

                        StringBuilder authors = new StringBuilder();
                        for (Object json_root_data_recommend_comics_item_authors_ : json_root_data_recommend_comics_item.getJSONArray("authors")) {
                            if (!(json_root_data_recommend_comics_item_authors_ instanceof JSONObject)) continue;
                            if (!authors.toString().isEmpty()) authors.append(", ");
                            JSONObject json_root_data_recommend_comics_item_authors = (JSONObject) json_root_data_recommend_comics_item_authors_;
                            authors.append(json_root_data_recommend_comics_item_authors.getString("cname"));
                        }

                        mangaListAdapter.addDataItem(new MangaListAdapter.DataItemNormal(
                                json_root_data_recommend_comics_item.getString("vertical_cover"),
                                json_root_data_recommend_comics_item.getString("title"),
                                authors.toString()
                        ).setOnClickListener(view -> MangaInfoActivity.startActivity(CONTEXT, view, json_root_data_recommend_comics_item.getInteger("id"))));
                    }
                }))
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
            binding.commentsHeaderSubtitle.setText(spannableString);
            commentsView.adapter.clearDataItems();
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
                BiliAPI.REPLY_TYPE_MANGA_MCID,
                comicId,
                replyNowSort,
                20,
                page,
                API.getJsonDataCallbackAutoE(CONTEXT, json_root_data -> runOnUiThread(() -> {
                    if (commentsView.adapter.getItemCount() > 0) commentsView.adapter.removeDataItem(commentsView.adapter.getItemCount() - 1);

                    View.OnLongClickListener smoothScrollToTop = v -> {
                        runOnUiThread(() -> {
                            binding.scrollView.smoothScrollTo(0, (int) binding.commentsHeader.getY());
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
                                .setOnClickListener(v -> CommentDetailsActivity.startActivity(CONTEXT, v, comicId, json_root_data_replies_item.getLong("rpid")))
                                .setLikeClickListener((likeLayout, beforeLiked, beforeCount, like, unlike) -> {
                                    boolean action = !beforeLiked;
                                    BiliAPI.replyAction(
                                            new BiliCookieJar(CONTEXT),
                                            BiliAPI.REPLY_TYPE_MANGA_MCID,
                                            comicId,
                                            json_root_data_replies_item.getLong("rpid"),
                                            action,
                                            API.getJsonDataCallbackAutoE(CONTEXT, json_root_data1 -> {})
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
                                                    new BiliCookieJar(CONTEXT),
                                                    BiliAPI.REPLY_TYPE_MANGA_MCID,
                                                    comicId,
                                                    json_root_data_replies_item_replies_item.getLong("rpid"),
                                                    action,
                                                    API.getJsonDataCallbackAutoE(CONTEXT, json_root_data1 -> {})
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
                        TimeUtils.setTimeout(() -> runOnUiThread(() -> binding.scrollView.smoothScrollTo(0, (int) binding.commentsHeader.getY())), 0);
                    firstLoadReply = false;
                }))
        );
    }

    public static void startActivity(Activity activity, View cardView, int comic_id) {
        ActivityOptionsCompat compat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        cardView,
                        activity.getString(R.string.app_name)
                );
        ActivityCompat.startActivity(
                activity,
                new Intent(activity, MangaInfoActivity.class)
                        .putExtra("comic_id", comic_id),
                compat.toBundle()
        );
    }
}