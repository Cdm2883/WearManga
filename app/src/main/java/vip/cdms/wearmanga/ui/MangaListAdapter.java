package vip.cdms.wearmanga.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.utils.MathUtils;
import vip.cdms.wearmanga.utils.SettingsUtils;
import vip.cdms.wearmanga.utils.StringUtils;
import vip.cdms.wearmanga.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * 漫画列表 - 适配器
 */
public class MangaListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final RecyclerView recyclerView;
    private ItemDecoration itemDecoration = null;
    private final ArrayList<DataItem> localDataSet;

    private int orientation = RecyclerView.VERTICAL;
    private int horizontalHeight;

    private boolean checkable = false;
    private boolean checkableControl = false;
    private final ArrayList<Integer> checkList = new ArrayList<>();
    public MangaListAdapter setCheckable(boolean checkable) {
        this.checkable = checkable;
        checkableControl = checkable;
        notifyItemRangeRemoved(0, localDataSet.size());
        notifyItemRangeInserted(0, localDataSet.size());
        reloadItemDecoration();
        return this;
    }
    public boolean isCheckable() {
        return checkable;
    }
    public int[] checkList() {
//        return checkList.stream().mapToInt(value -> value).toArray();
        int[] ints = new int[checkList.size()];
        for (int i = 0; i < checkList.size(); i++) {
            ints[i] = checkList.get(i);
        }
        return ints;
    }
    public void checkListClear() {
        checkList.clear();
        setCheckable(false);
        checkable = true;
        checkableControl = false;
        TimeUtils.setTimeout(() -> recyclerView.post(() -> setCheckable(true)), 100);
        checkList.clear();
    }

    public void setItemDecoration(ItemDecoration itemDecoration) {
        if (this.itemDecoration != null) recyclerView.removeItemDecoration(this.itemDecoration);
        this.itemDecoration = itemDecoration;
        recyclerView.addItemDecoration(itemDecoration);
    }

    public void setLayoutVertical() {
        orientation = RecyclerView.VERTICAL;

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                );  // 瀑布流
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
    }
    public void setLayoutHorizontal(/*Context context, */int heightPX) {
        orientation = RecyclerView.HORIZONTAL;
        horizontalHeight = heightPX;

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(
                        1,
                        StaggeredGridLayoutManager.HORIZONTAL
                );  // 瀑布流
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public static class DataItem {
        public static final int TYPE_NORMAL = 0;
        public static final int TYPE_HEADER = 1;
        public static final int TYPE_MIDDLE_HEADER = 2;

        protected int TYPE;
        public int getType() {
            return TYPE;
        }
    }


    public static class DataItemHeader extends DataItem {
        private final String title;
        private final CharSequence subtitle;

        private View.OnClickListener onClickListener = null;

        public DataItemHeader(String title, CharSequence subtitle) {
            TYPE = DataItem.TYPE_HEADER;
            this.title = title;
            this.subtitle = subtitle;
        }

        public String getTitle() {
            return title;
        }

        public CharSequence getSubtitle() {
            return subtitle;
        }

        public View.OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public DataItemHeader setOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }
    }
    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView title;
        private final TextView subtitle;

        public ViewHolderHeader(View view) {
            super(view);

            this.view = view;
            title = view.findViewById(R.id.title);
            subtitle = view.findViewById(R.id.subtitle);
        }

        public View getView() {
            return view;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getSubtitle() {
            return subtitle;
        }
    }

    public static class DataItemMiddleHeader extends DataItem {
        private final String content;

        public DataItemMiddleHeader(String content) {
            TYPE = DataItem.TYPE_MIDDLE_HEADER;
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
    public static class ViewHolderMiddleHeader extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView content;

        public ViewHolderMiddleHeader(View view) {
            super(view);

            this.view = view;
            content = view.findViewById(R.id.content);
        }

        public View getView() {
            return view;
        }

        public TextView getContent() {
            return content;
        }
    }

    public static class DataItemNormal extends DataItem {
        private final String imageUrl;
        private final String title;
        private final String subtitle;
        private int id = -1;

        private View.OnClickListener onClickListener = null;
        private View.OnLongClickListener onLongClickListener = null;

        public DataItemNormal(String image, String title, String subtitle) {
            TYPE = DataItem.TYPE_NORMAL;
            this.imageUrl = image;
            this.title = title;
            this.subtitle = subtitle.trim().isEmpty() ? null : subtitle;
        }

        public int getId() {
            return id;
        }
        public DataItemNormal id(int id) {
            this.id = id;
            return this;
        }

        public DataItemNormal setOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

        public DataItemNormal setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
            this.onLongClickListener = onLongClickListener;
            return this;
        }

        public View.OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public View.OnLongClickListener getOnLongClickListener() {
            return onLongClickListener;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }
    }
    public static class ViewHolderNormal extends RecyclerView.ViewHolder {
        private final View view;

        private final MaterialCardView card;

        private final ImageView image;
        private final TextView title;
        private final TextView subtitle;

        public ViewHolderNormal(View view) {
            super(view);
            // 为视图持有人的视图定义单击侦听器
            // Define click listener for the ViewHolder's View

            this.view = view;
            card = view.findViewById(R.id.card);
            image = view.findViewById(R.id.image);
            title = view.findViewById(R.id.title);
            subtitle = view.findViewById(R.id.subtitle);
        }

        public View getView() {
            return view;
        }

        public MaterialCardView getCard() {
            return card;
        }

        public ImageView getImage() {
            return image;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getSubtitle() {
            return subtitle;
        }
    }

    /**
     * ItemDecoration 允许应用程序添加特殊的绘图和布局偏移
     * 到适配器数据集中的特定项目视图。这对于绘制在项目、突出显示、
     * 视觉分组边界等之间的分隔线很有用。
     */
    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        private final int LAST_MARGIN_BOTTOM;

        public ItemDecoration() {
            LAST_MARGIN_BOTTOM = 150;
        }
        public ItemDecoration(int LAST_MARGIN_BOTTOM) {
            this.LAST_MARGIN_BOTTOM = LAST_MARGIN_BOTTOM;
        }
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

            if (Objects.requireNonNull(parent.getAdapter()).getItemViewType(parent.getChildAdapterPosition(view)) != DataItem.TYPE_NORMAL) return;

            view.post(() -> {
                Context viewContext = view.getContext();
                int halfScreenWidthPixels = viewContext.getResources().getDisplayMetrics().widthPixels / 2;
                float viewX = view.getX();

                ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
                if (!(viewLayoutParams instanceof StaggeredGridLayoutManager.LayoutParams)) return;
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewLayoutParams;

                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = 0;
                if (viewX < halfScreenWidthPixels)
                    layoutParams.leftMargin = MathUtils.dp2px(viewContext, 4);
                else
                    layoutParams.rightMargin = MathUtils.dp2px(viewContext, 4);

                view.setLayoutParams(layoutParams);
            });

            if (
                    parent.getChildLayoutPosition(view) == (state.getItemCount() - 1)
            )
                outRect.bottom = LAST_MARGIN_BOTTOM;
        }
    }

    /**
     * 初始化适配器的数据集。
     */
    public MangaListAdapter(RecyclerView recyclerView, ArrayList<DataItem> dataSet) {
        this.recyclerView = recyclerView;
        localDataSet = dataSet;

        recyclerView.setAdapter(this);
    }
    public MangaListAdapter(RecyclerView recyclerView) {
        this(recyclerView ,new ArrayList<>());
    }

    // 创建新视图（由布局管理器调用）
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        // 创建一个新视图，用于定义列表项的 UI

        View view;
        switch (viewType) {
            case DataItem.TYPE_HEADER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.manga_item_header, viewGroup, false);
                return new ViewHolderHeader(view);

            case DataItem.TYPE_MIDDLE_HEADER:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.manga_item_middle_header, viewGroup, false);
                return new ViewHolderMiddleHeader(view);

            case DataItem.TYPE_NORMAL:
            default:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.manga_item_list, viewGroup, false);
                return new ViewHolderNormal(view);
        }
    }

    // 替换视图的内容（由布局管理器调用）
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, final int position) {
        // 从数据集中获取此位置的元素并替换具有该元素的视图的内容
        DataItem dataItem = localDataSet.get(position);

        if (
                (viewHolder instanceof ViewHolderHeader)
                && (dataItem instanceof DataItemHeader)
        ) {
            ViewHolderHeader viewHolderHeader = (ViewHolderHeader) viewHolder;
            DataItemHeader dataItemHeader = (DataItemHeader) dataItem;

            viewHolderHeader.getTitle().setText(dataItemHeader.getTitle());
            if (dataItemHeader.getSubtitle() == null) viewHolderHeader.getSubtitle().setVisibility(View.GONE);
            else {
                viewHolderHeader.getSubtitle().setVisibility(View.VISIBLE);
                viewHolderHeader.getSubtitle().setText(dataItemHeader.getSubtitle());
            }

            if (dataItemHeader.getOnClickListener() != null) viewHolderHeader.getView().setOnClickListener(dataItemHeader.getOnClickListener());
        } else if (
                (viewHolder instanceof ViewHolderMiddleHeader)
                && (dataItem instanceof DataItemMiddleHeader)
        ) {
            ViewHolderMiddleHeader viewHolderMiddleHeader = (ViewHolderMiddleHeader) viewHolder;
            DataItemMiddleHeader dataItemMiddleHeader = (DataItemMiddleHeader) dataItem;

            viewHolderMiddleHeader.getContent().setText(dataItemMiddleHeader.getContent());
        } else if (
                (viewHolder instanceof ViewHolderNormal)
                && (dataItem instanceof DataItemNormal)
        ) {
            ViewHolderNormal viewHolderNormal = (ViewHolderNormal) viewHolder;
            DataItemNormal dataItemNormal = (DataItemNormal) dataItem;

            viewHolderNormal.getCard().post(() -> {
                ConstraintLayout.LayoutParams cardLayoutParams = (ConstraintLayout.LayoutParams) viewHolderNormal.getCard().getLayoutParams();

//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (orientation == RecyclerView.VERTICAL) {
                    cardLayoutParams.height = viewHolderNormal.getCard().getWidth() / 3 * 4;
                } else if (orientation == RecyclerView.HORIZONTAL) {
                    cardLayoutParams.height = horizontalHeight;
                    cardLayoutParams.width = cardLayoutParams.height / 4 * 3;
                }
                viewHolderNormal.getCard().setLayoutParams(cardLayoutParams);

                double imageSize = SettingsUtils.getInt("image_size", 100) * 0.01;
                String imageUrl = dataItemNormal.getImageUrl();
                imageUrl = StringUtils.biliImageUrl(imageUrl, 300, 400);
                Glide.with(viewHolderNormal.getView())
                        .setDefaultRequestOptions(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                                .override(300, 400)
//                                .override(viewHolderNormal.getCard().getWidth(), viewHolderNormal.getCard().getHeight())
                                .override((int) (300 * imageSize), (int) (400 * imageSize))
                                .format(DecodeFormat.PREFER_RGB_565))
                        .load(imageUrl)
                        .placeholder(R.drawable.baseline_book_24)
                        .into(viewHolderNormal.getImage());
            });

            viewHolderNormal.getTitle().setText(dataItemNormal.getTitle());
            String subtitle = dataItemNormal.getSubtitle();
            if (subtitle == null) viewHolderNormal.getSubtitle().setVisibility(View.GONE);
            else viewHolderNormal.getSubtitle().setText(subtitle);

//            if (dataItemNormal.getOnClickListener() != null) viewHolderNormal.getCard().setOnClickListener(dataItemNormal.getOnClickListener());
//            if (dataItemNormal.getOnLongClickListener() != null) viewHolderNormal.getCard().setOnLongClickListener(dataItemNormal.getOnLongClickListener());
//            if (checkable) {
//                viewHolderNormal.getCard().setOnLongClickListener(v -> {
//                    MaterialCardView card = (MaterialCardView) v;
//                    boolean checked = !card.isChecked();
//                    card.setChecked(checked);
//                    if (checked) checkList.add(dataItemNormal.getId());
//                    else checkList.remove((Integer) dataItemNormal.getId());
//                    return dataItemNormal.getOnLongClickListener() != null && dataItemNormal.getOnLongClickListener().onLongClick(v);
//                });
//            } else if (dataItemNormal.getOnClickListener() != null) viewHolderNormal.getCard().setOnClickListener(dataItemNormal.getOnClickListener());
//            if (!checkable) viewHolderNormal.getCard().setChecked(false);
            if (!checkable || !checkableControl) viewHolderNormal.getCard().setChecked(false);
            Runnable toggleChecked = () -> {
                boolean checked = !viewHolderNormal.getCard().isChecked();
                viewHolderNormal.getCard().setChecked(checked);
                if (checked) checkList.add(dataItemNormal.getId());
                else checkList.remove((Integer) dataItemNormal.getId());
            };
            viewHolderNormal.getCard().setOnClickListener(v -> {
                if (!checkable) {
                    if (dataItemNormal.getOnClickListener() != null) dataItemNormal.getOnClickListener().onClick(v);
                    return;
                }
                if (checkList.size() > 0) toggleChecked.run();
                else if (dataItemNormal.getOnClickListener() != null) dataItemNormal.getOnClickListener().onClick(v);
            });
            viewHolderNormal.getCard().setOnLongClickListener(v -> {
                if (!checkable) {
                    if (dataItemNormal.getOnLongClickListener() != null) return dataItemNormal.getOnLongClickListener().onLongClick(v);
                    return true;
                }
                if (checkList.size() > 0) {
                    checkListClear();
                } else toggleChecked.run();
                if (dataItemNormal.getOnLongClickListener() != null) return dataItemNormal.getOnLongClickListener().onLongClick(v);
                return true;
            });
        }
    }

    // 当此适配器创建的视图已附加到窗口时调用。
    //  这可以用作用户即将看到视图的合理信号。如果适配器以前释放了这些资源中的任何 onViewDetachedFromWindow 资源，则应在此处还原。
    @Override
    public void onViewAttachedToWindow(@NotNull RecyclerView.ViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);

        if (
                (viewHolder instanceof ViewHolderHeader)
                || (viewHolder instanceof ViewHolderMiddleHeader)
        ) {
            ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                // Header在瀑布流下宽度全屏
                StaggeredGridLayoutManager.LayoutParams staggeredGridLayoutParams = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                staggeredGridLayoutParams.setFullSpan(true);
                viewHolder.itemView.setLayoutParams(staggeredGridLayoutParams);
            }
        }
    }

    private void reloadItemDecoration() {
        if (itemDecoration != null) {
            recyclerView.removeItemDecoration(itemDecoration);
            recyclerView.addItemDecoration(itemDecoration);
        }
    }

    public void addDataItem(int position, DataItem dataItem) {
        localDataSet.add(position, dataItem);
        notifyItemInserted(position);
        reloadItemDecoration();
    }
    public void addDataItem(DataItem dataItem) {
        localDataSet.add(dataItem);
        notifyItemInserted(localDataSet.size() - 1);
        reloadItemDecoration();
    }
    public void removeDataItem(int position) {
        localDataSet.remove(position);
        notifyItemRemoved(position);
        reloadItemDecoration();
    }

    public void addDataItems(int position, Collection<DataItem> dataItems) {
        localDataSet.addAll(position, dataItems);
        notifyItemRangeInserted(position, dataItems.size());
        reloadItemDecoration();
    }
    public void addDataItems(Collection<DataItem> dataItems) {
        localDataSet.addAll(dataItems);
        notifyItemRangeInserted(localDataSet.size() - dataItems.size(), dataItems.size());
        reloadItemDecoration();
    }
    public void removeDataItems(int positionStart, int itemCount) {
        for (int i = positionStart; i < Math.min(positionStart + itemCount, localDataSet.size()); i++) localDataSet.set(i, null);
        localDataSet.removeAll(Collections.singleton(null));
        notifyItemRangeRemoved(positionStart, itemCount);
        reloadItemDecoration();
    }

    public void clearDataItems() {
        int beforeSize = localDataSet.size();
        localDataSet.clear();
        notifyItemRangeRemoved(0, beforeSize);
        reloadItemDecoration();
    }

    // 获取Item的类型
    @Override
    public int getItemViewType(int position) {
        try {
            return localDataSet.get(position).getType();
        } catch (Exception e) {
            return -1;
        }
    }

    // 返回数据集的大小（由布局管理器调用）
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
