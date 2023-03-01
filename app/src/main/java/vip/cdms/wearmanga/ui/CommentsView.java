package vip.cdms.wearmanga.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.text.Html;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.divider.MaterialDivider;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * 评论列表
 */
public class CommentsView extends RecyclerView {
    public final CommentsViewAdapter adapter;
    public final LinearLayoutManager layoutManager;
    public final ItemDecoration itemDecoration;

    // 构造函数
    public CommentsView(@NonNull @NotNull Context context) {
        this(context, null);
    }
    public CommentsView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, androidx.recyclerview.R.attr.recyclerViewStyle);
    }
    public CommentsView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        adapter = new CommentsViewAdapter(this);
        this.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.setLayoutManager(layoutManager);

        itemDecoration = new ItemDecoration(context);
        this.addItemDecoration(itemDecoration);
    }

    public int _getScrollY() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) this.getLayoutManager();
        assert layoutManager != null;
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = layoutManager.findViewByPosition(position);
        assert firstVisiableChildView != null;
        int itemHeight = firstVisiableChildView.getHeight();
        return (position) * itemHeight - firstVisiableChildView.getTop();
    }

    public void reloadItemDecoration() {
        removeItemDecoration(itemDecoration);
        addItemDecoration(itemDecoration);
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        private final int LAST_MARGIN_BOTTOM;

        public ItemDecoration(Context context) {
            LAST_MARGIN_BOTTOM = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.6);
        }

        @Override
        public void getItemOffsets(
                @NotNull Rect outRect,
                @NotNull View view,
                @NotNull RecyclerView parent,
                @NotNull RecyclerView.State state
        ) {
            super.getItemOffsets(outRect, view, parent, state);
            if (
                    parent.getChildLayoutPosition(view) == (state.getItemCount() - 1)
            )
                outRect.bottom = LAST_MARGIN_BOTTOM;
        }
    }

    /**
     * 适配器
     */
    public static class CommentsViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final CommentsView commentsView;
        private final ArrayList<DataItem> localDataSet;

        public static class DataItem {
            public static final int TYPE_HEADER = 0;
            public static final int TYPE_COMMENT = 1;
            public static final int TYPE_COMMENT_CHILD = 2;

            protected int TYPE;
            public int getType() {
                return TYPE;
            }
        }

        public static class DataItemHeader extends DataItem {
            private String title = null;
            private String subtitle = null;

            private String more = null;

            private View.OnClickListener onClickListener = null;
            private View.OnLongClickListener onLongClickListener = null;

            public DataItemHeader() {
                TYPE = DataItem.TYPE_HEADER;
            }

            public DataItemHeader header(String title, String subtitle) {
                this.title = title;
                this.subtitle = subtitle;
                return this;
            }

            public DataItemHeader more(String more) {
                this.more = more;
                return this;
            }

            public String getTitle() {
                return title;
            }

            public String getSubtitle() {
                return subtitle;
            }

            public String getMore() {
                return more;
            }

            public DataItemHeader setOnClickListener(OnClickListener onClickListener) {
                this.onClickListener = onClickListener;
                return this;
            }
            public OnClickListener getOnClickListener() {
                return onClickListener;
            }

            public DataItemHeader setOnLongClickListener(OnLongClickListener onLongClickListener) {
                this.onLongClickListener = onLongClickListener;
                return this;
            }
            public OnLongClickListener getOnLongClickListener() {
                return onLongClickListener;
            }
        }
        public static class ViewHolderHeader extends RecyclerView.ViewHolder {
            private final View view;

            private final LinearLayout header;
            private final TextView headerTitle;
            private final TextView headerSubtitle;

            private final RelativeLayout more;
            private final TextView moreText;

            public ViewHolderHeader(View view) {
                super(view);

                this.view = view;

                header = view.findViewById(R.id.header);
                headerTitle = view.findViewById(R.id.title);
                headerSubtitle = view.findViewById(R.id.subtitle);

                more = view.findViewById(R.id.more);
                moreText = view.findViewById(R.id.more_text);
            }

            public View getView() {
                return view;
            }

            public LinearLayout getHeader() {
                return header;
            }

            public TextView getHeaderTitle() {
                return headerTitle;
            }

            public TextView getHeaderSubtitle() {
                return headerSubtitle;
            }

            public RelativeLayout getMore() {
                return more;
            }

            public TextView getMoreText() {
                return moreText;
            }
        }

        public static class DataItemComment extends DataItem {
            private final String avatarUrl;
            private final String userName;
            private final CharSequence content;
            private String contentHtml = null;
            private final String time;

            private boolean liked;
            private String likeCount;
            private LikeClickListener likeClickListener = null;
            public interface LikeClickListener {
                String onClick(LinearLayout likeLayout, boolean beforeLiked, String beforeCount, Runnable like, Runnable unlike);
            }

            private View.OnClickListener onClickListener = null;
            private View.OnLongClickListener onLongClickListener = null;

            public DataItemComment(String avatarUrl, String userName, CharSequence content, String time, boolean liked, String likeCount) {
                TYPE = DataItem.TYPE_COMMENT;
                this.avatarUrl = avatarUrl;
                this.userName = userName;
                this.content = content;
                this.time = time;
                this.liked = liked;
                this.likeCount = likeCount;
            }
            public DataItemComment(String avatarUrl, String userName, CharSequence content, String time) {
                this(avatarUrl, userName, content, time, false, "...");
            }

            public String getAvatarUrl() {
                return avatarUrl;
            }

            public String getUserName() {
                return userName;
            }

            public CharSequence getContent() {
                return content;
            }

            public String getContentHtml() {
                return contentHtml;
            }
            public DataItemComment setContentHtml(String contentHtml) {
                this.contentHtml = contentHtml;
                return this;
            }

            public String getTime() {
                return time;
            }

            public boolean isLiked() {
                return liked;
            }

            public String getLikeCount() {
                return likeCount;
            }

            public LikeClickListener getLikeClickListener() {
                return likeClickListener;
            }

            public OnClickListener getOnClickListener() {
                return onClickListener;
            }

            public OnLongClickListener getOnLongClickListener() {
                return onLongClickListener;
            }

            public DataItemComment setLiked(boolean liked) {
                this.liked = liked;
                return this;
            }

            public DataItemComment setLikeCount(String likeCount) {
                this.likeCount = likeCount;
                return this;
            }

            public DataItemComment setLikeClickListener(LikeClickListener likeClickListener) {
                this.likeClickListener = likeClickListener;
                return this;
            }

            public DataItemComment setOnClickListener(OnClickListener onClickListener) {
                this.onClickListener = onClickListener;
                return this;
            }

            public DataItemComment setOnLongClickListener(OnLongClickListener onLongClickListener) {
                this.onLongClickListener = onLongClickListener;
                return this;
            }
        }
        public static class ViewHolderComment extends ViewHolderCommentChild {
            private final MaterialDivider divider;

            public ViewHolderComment(View view) {
                super(view);
                divider = view.findViewById(R.id.divider);
            }

            public MaterialDivider getDivider() {
                return divider;
            }
        }

        public static class DataItemCommentChild extends DataItemComment {
            public DataItemCommentChild(String avatarUrl, String userName, String content, String time, boolean liked, String likeCount) {
                super(avatarUrl, userName, content, time, liked, likeCount);
                TYPE = DataItem.TYPE_COMMENT_CHILD;
            }
            public DataItemCommentChild(String avatarUrl, String userName, String content, String time) {
                this(avatarUrl, userName, content, time, false, "...");
            }
        }
        public static class ViewHolderCommentChild extends RecyclerView.ViewHolder {
            private final View view;

//            private final MaterialDivider divider;
            private final CircleImageView avatar;
            private final TextView userName;
            private final TextView content;
            private final TextView time;

            private final LinearLayout likeLayout;
            private final ImageView likeIcon;
            private final TextView likeCount;

            public ViewHolderCommentChild(View view) {
                super(view);

                this.view = view;

//                divider = view.findViewById(R.id.divider);
                avatar = view.findViewById(R.id.avatar);
                userName = view.findViewById(R.id.user_name);
                content = view.findViewById(R.id.content);
                time = view.findViewById(R.id.time);

                likeLayout = view.findViewById(R.id.like_layout);
                likeIcon = likeLayout.findViewById(R.id.like_icon);
                likeCount = likeLayout.findViewById(R.id.like_count);

                likeCount.setText(new SpannableString(""));
            }

            public View getView() {
                return view;
            }

//            public MaterialDivider getDivider() {
//                return divider;
//            }

            public CircleImageView getAvatar() {
                return avatar;
            }

            public TextView getUserName() {
                return userName;
            }

            public TextView getContent() {
                return content;
            }

            public TextView getTime() {
                return time;
            }

            public LinearLayout getLikeLayout() {
                return likeLayout;
            }

            public ImageView getLikeIcon() {
                return likeIcon;
            }

            public TextView getLikeCount() {
                return likeCount;
            }
        }

        public CommentsViewAdapter(CommentsView commentsView) {
            this.commentsView = commentsView;
            localDataSet = new ArrayList<>();
        }

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case DataItem.TYPE_HEADER:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.comments_item_header, parent, false);
                    return new ViewHolderHeader(view);

                default:
                case DataItem.TYPE_COMMENT:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.comments_item_list, parent, false);
                    return new ViewHolderComment(view);

                case DataItem.TYPE_COMMENT_CHILD:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.comments_item_child, parent, false);
                    return new ViewHolderCommentChild(view);
            }
        }

        private boolean foundFirstDataItemComment = false;
        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
            DataItem dataItem = localDataSet.get(position);

            if (position == 0) foundFirstDataItemComment = false;

            if (
                    (viewHolder instanceof ViewHolderHeader)
                    && (dataItem instanceof DataItemHeader)
            ) {
                ViewHolderHeader viewHolderHeader = (ViewHolderHeader) viewHolder;
                DataItemHeader dataItemHeader = (DataItemHeader) dataItem;

                if (dataItemHeader.getTitle() != null) {
                    viewHolderHeader.getHeader().setVisibility(VISIBLE);
                    viewHolderHeader.getHeaderTitle().setText(dataItemHeader .getTitle());
                    if (dataItemHeader.getSubtitle() == null) viewHolderHeader.getHeaderSubtitle().setVisibility(GONE);
                    else viewHolderHeader.getHeaderSubtitle().setText(dataItemHeader.getSubtitle());
                } else if (dataItemHeader.getMore() != null) {
                    viewHolderHeader.getMore().setVisibility(VISIBLE);
                    viewHolderHeader.getMoreText().setText(dataItemHeader.getMore());
                }

                if (dataItemHeader.getOnClickListener() != null) viewHolderHeader.getView().setOnClickListener(dataItemHeader.getOnClickListener());
                if (dataItemHeader.getOnLongClickListener() != null) viewHolderHeader.getView().setOnLongClickListener(dataItemHeader.getOnLongClickListener());
            } else if (
                    (viewHolder instanceof ViewHolderComment)
                    && (dataItem instanceof DataItemComment)
                    && (dataItem.getType() == DataItem.TYPE_COMMENT)
            ) {
                ViewHolderComment viewHolderComment = (ViewHolderComment) viewHolder;
                DataItemComment dataItemComment = (DataItemComment) dataItem;

                if (!foundFirstDataItemComment) {
                    foundFirstDataItemComment = true;
                    viewHolderComment.getDivider().setVisibility(GONE);
                }

                viewHolderComment.getAvatar().post(() -> {
                    String avatarUrl = dataItemComment.getAvatarUrl();
                    if (Pattern.compile(
                            "^*.hdslb.com/bfs/.+/.+\\..*$"
                    ).matcher(avatarUrl).matches()) avatarUrl = avatarUrl + "@300w_300h";
                    Glide.with(viewHolderComment.getView())
                            .setDefaultRequestOptions(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(viewHolderComment.getAvatar().getWidth(), viewHolderComment.getAvatar().getHeight())
                                    .format(DecodeFormat.PREFER_RGB_565))
                            .load(avatarUrl)
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(viewHolderComment.getAvatar());
                });

                viewHolderComment.getUserName().setText(dataItemComment.getUserName());
                if (dataItemComment.getContentHtml() != null) viewHolderComment.getContent().setText(Html.fromHtml(dataItemComment.getContentHtml(), new URLImageParser(viewHolderComment.getContent(), commentsView.getContext()), null));
                else viewHolderComment.getContent().setText(dataItemComment.getContent());
                viewHolderComment.getTime().setText(dataItemComment.getTime());

                if (dataItemComment.getOnClickListener() != null) viewHolderComment.getView().setOnClickListener(dataItemComment.getOnClickListener());
                if (dataItemComment.getOnLongClickListener() != null) viewHolderComment.getView().setOnLongClickListener(dataItemComment.getOnLongClickListener());

                if (dataItemComment.getLikeCount() == null) {
                    viewHolderComment.getLikeLayout().setVisibility(GONE);
                    return;
                }
                viewHolderComment.getLikeIcon().setImageResource(dataItemComment.isLiked() ? R.drawable.baseline_thumb_up_24 : R.drawable.outline_thumb_up_24);
                if (dataItemComment.isLiked()) {
                    viewHolderComment.getLikeIcon().setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                    viewHolderComment.getLikeIcon().setImageTintList(ContextCompat.getColorStateList(commentsView.getContext(), R.color.fav_fab_yes));
                }
                viewHolderComment.getLikeCount().setText(dataItemComment.getLikeCount());
                DataItemComment.LikeClickListener likeClickListener = dataItemComment.getLikeClickListener();
                if (likeClickListener != null) {
                    LinearLayout likeLayout = viewHolderComment.getLikeLayout();
                    likeLayout.setOnClickListener(v -> {
                        String likeCount = likeClickListener.onClick(
                                likeLayout,
                                ((DataItemComment) localDataSet.get(position)).isLiked(),
                                ((DataItemComment) localDataSet.get(position)).getLikeCount(),
                                () -> {
                                    DataItemComment nowDataItemComment = (DataItemComment) localDataSet.get(position);
                                    nowDataItemComment.setLiked(true);
                                    localDataSet.set(position, nowDataItemComment);

                                    viewHolderComment.getLikeIcon().setImageResource(R.drawable.baseline_thumb_up_24);
                                    viewHolderComment.getLikeIcon().setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                                    viewHolderComment.getLikeIcon().setImageTintList(ContextCompat.getColorStateList(commentsView.getContext(), R.color.fav_fab_yes));

                                    // 抖动动画
                                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                                            viewHolderComment.getLikeLayout(),
                                            View.TRANSLATION_X.getName(),
                                            0,
                                            4f  // 抖动幅度0到8
                                    );
                                    objectAnimator.setDuration(200);
                                    objectAnimator.setInterpolator(new CycleInterpolator(2));  // 抖动次数
                                    objectAnimator.start();
                                },
                                () -> {
                                    DataItemComment nowDataItemComment = (DataItemComment) localDataSet.get(position);
                                    nowDataItemComment.setLiked(false);
                                    localDataSet.set(position, nowDataItemComment);

                                    viewHolderComment.getLikeIcon().setImageResource(R.drawable.outline_thumb_up_24);
                                    viewHolderComment.getLikeIcon().setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                                    viewHolderComment.getLikeIcon().setImageTintList(ContextCompat.getColorStateList(commentsView.getContext(), R.color.icon));
                                }
                        );
                        viewHolderComment.getLikeCount().setText(likeCount);
                        DataItemComment nowDataItemComment = (DataItemComment) localDataSet.get(position);
                        nowDataItemComment.setLikeCount(likeCount);
                        localDataSet.set(position, nowDataItemComment);
                    });
                }
            } else if (
                    (viewHolder instanceof ViewHolderCommentChild)
                    && (dataItem instanceof DataItemCommentChild)
                    && (dataItem.getType() == DataItem.TYPE_COMMENT_CHILD)
            ) {
                ViewHolderCommentChild viewHolderCommentChild = (ViewHolderCommentChild) viewHolder;
                DataItemCommentChild dataItemCommentChild = (DataItemCommentChild) dataItem;

                viewHolderCommentChild.getAvatar().post(() -> {
                    String avatarUrl = dataItemCommentChild.getAvatarUrl();
                    if (Pattern.compile(
                            "^*.hdslb.com/bfs/.+/.+\\..*$"
                    ).matcher(avatarUrl).matches()) avatarUrl = avatarUrl + "@300w_300h";
                    Glide.with(viewHolderCommentChild.getView())
                            .setDefaultRequestOptions(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(viewHolderCommentChild.getAvatar().getWidth(), viewHolderCommentChild.getAvatar().getHeight())
                                    .format(DecodeFormat.PREFER_RGB_565))
                            .load(avatarUrl)
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(viewHolderCommentChild.getAvatar());
                });

                viewHolderCommentChild.getUserName().setText(dataItemCommentChild.getUserName());
//                viewHolderCommentChild.getContent().setText(dataItemCommentChild.getContent());
                if (dataItemCommentChild.getContentHtml() != null) viewHolderCommentChild.getContent().setText(Html.fromHtml(dataItemCommentChild.getContentHtml(), new URLImageParser(viewHolderCommentChild.getContent(), commentsView.getContext()), null));
                else viewHolderCommentChild.getContent().setText(dataItemCommentChild.getContent());
                viewHolderCommentChild.getTime().setText(dataItemCommentChild.getTime());

                if (dataItemCommentChild.getOnClickListener() != null) viewHolderCommentChild.getView().setOnClickListener(dataItemCommentChild.getOnClickListener());
                if (dataItemCommentChild.getOnLongClickListener() != null) viewHolderCommentChild.getView().setOnLongClickListener(dataItemCommentChild.getOnLongClickListener());

                if (dataItemCommentChild.getLikeCount() == null) {
                    viewHolderCommentChild.getLikeLayout().setVisibility(GONE);
                    return;
                }
                viewHolderCommentChild.getLikeIcon().setImageResource(dataItemCommentChild.isLiked() ? R.drawable.baseline_thumb_up_24 : R.drawable.outline_thumb_up_24);
                if (dataItemCommentChild.isLiked()) {
                    viewHolderCommentChild.getLikeIcon().setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                    viewHolderCommentChild.getLikeIcon().setImageTintList(ContextCompat.getColorStateList(commentsView.getContext(), R.color.fav_fab_yes));
                }
                viewHolderCommentChild.getLikeCount().setText(dataItemCommentChild.getLikeCount());
                DataItemComment.LikeClickListener likeClickListener = dataItemCommentChild.getLikeClickListener();
                if (likeClickListener != null) {
                    LinearLayout likeLayout = viewHolderCommentChild.getLikeLayout();
                    likeLayout.setOnClickListener(v -> {
                        String likeCount = likeClickListener.onClick(
                                likeLayout,
                                ((DataItemCommentChild) localDataSet.get(position)).isLiked(),
                                ((DataItemCommentChild) localDataSet.get(position)).getLikeCount(),
                                () -> {
                                    DataItemCommentChild nowDataItemCommentChild = (DataItemCommentChild) localDataSet.get(position);
                                    nowDataItemCommentChild.setLiked(true);
                                    localDataSet.set(position, nowDataItemCommentChild);

                                    viewHolderCommentChild.getLikeIcon().setImageResource(R.drawable.baseline_thumb_up_24);
                                    viewHolderCommentChild.getLikeIcon().setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                                    viewHolderCommentChild.getLikeIcon().setImageTintList(ContextCompat.getColorStateList(commentsView.getContext(), R.color.fav_fab_yes));

                                    // 抖动动画
                                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                                            viewHolderCommentChild.getLikeLayout(),
                                            View.TRANSLATION_X.getName(),
                                            0,
                                            4f  // 抖动幅度0到8
                                    );
                                    objectAnimator.setDuration(200);
                                    objectAnimator.setInterpolator(new CycleInterpolator(2));  // 抖动次数
                                    objectAnimator.start();
                                },
                                () -> {
                                    DataItemCommentChild nowDataItemCommentChild = (DataItemCommentChild) localDataSet.get(position);
                                    nowDataItemCommentChild.setLiked(false);
                                    localDataSet.set(position, nowDataItemCommentChild);

                                    viewHolderCommentChild.getLikeIcon().setImageResource(R.drawable.outline_thumb_up_24);
                                    viewHolderCommentChild.getLikeIcon().setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                                    viewHolderCommentChild.getLikeIcon().setImageTintList(ContextCompat.getColorStateList(commentsView.getContext(), R.color.icon));
                                }
                        );
                        viewHolderCommentChild.getLikeCount().setText(likeCount);
                        DataItemCommentChild nowDataItemCommentChild = (DataItemCommentChild) localDataSet.get(position);
                        nowDataItemCommentChild.setLikeCount(likeCount);
                        localDataSet.set(position, nowDataItemCommentChild);
                    });
                }
            }
        }

        public void addDataItem(DataItem dataItem) {
            localDataSet.add(dataItem);
            notifyItemInserted(localDataSet.size() - 1);
            commentsView.reloadItemDecoration();
        }
        public void removeDataItem(int position) {
            localDataSet.remove(position);
            notifyItemRemoved(position);
            commentsView.reloadItemDecoration();
        }
        public void clearDataItems() {
            int beforeSize = localDataSet.size();
            localDataSet.clear();
            notifyItemRangeRemoved(0, beforeSize);
            commentsView.reloadItemDecoration();
        }

        @Override
        public int getItemViewType(int position) {
            try {
                return localDataSet.get(position).getType();
            } catch (Exception e) {
                return -1;
            }
        }
        @Override
        public int getItemCount() {
            return localDataSet.size();
        }
    }

    /* Utils */

    public static String commentContent2Html(JSONObject json_root_data_replies_item_content) {
        JSONObject json_root_data_replies_item_content_emote = json_root_data_replies_item_content.getJSONObject("emote");
        String contentHtml = json_root_data_replies_item_content.getString("message");
        if (json_root_data_replies_item_content_emote != null) {
            for (String key : json_root_data_replies_item_content_emote.keySet()) {
                JSONObject value = json_root_data_replies_item_content_emote.getJSONObject(key);
                int size = 30 * value.getJSONObject("meta").getInteger("size");
                contentHtml = contentHtml.replaceAll(
                        key.replaceAll("\\[", "\\\\["),
                        "<img src=\"" + value.getString("url") + "@" + size + "w_" + size + "h\" alt=\"" + key + "\" border=\"0\"/>"
                );
            }
        }
        return contentHtml;
    }
}
