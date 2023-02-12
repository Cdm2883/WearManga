package vip.cdms.wearmanga.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.utils.DensityUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChaptersListAdapter extends RecyclerView.Adapter<ChaptersListAdapter.ViewHolder> {
    private final RecyclerView recyclerView;
    private final ItemOnClick itemOnClick;

    private final ArrayList<JSONObject> localDataSet = new ArrayList<>();
    private int read_epid = -2;

    private HashMap<Integer ,Float> posYs = new HashMap<>();

    public interface ItemOnClick {
        void onClick(JSONObject jsonObject);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final MaterialCardView card;
        private final TextView subtitle;
        private final TextView title;
        private final ImageView iconImage;
        private final MaterialButton iconButton;

        public ViewHolder(View view) {
            super(view);

            this.view = view;
            card = view.findViewById(R.id.card);
            subtitle = view.findViewById(R.id.subtitle);
            title = view.findViewById(R.id.title);
            iconImage = view.findViewById(R.id.icon_img);
            iconButton = view.findViewById(R.id.icon_btn);
        }

        public View getView() {
            return view;
        }

        public MaterialCardView getCard() {
            return card;
        }

        public TextView getSubtitle() {
            return subtitle;
        }

        public TextView getTitle() {
            return title;
        }

        public ImageView getIconImage() {
            return iconImage;
        }

        public MaterialButton getIconButton() {
            return iconButton;
        }
    }

    public static class SmoothScrollLayoutManager extends LinearLayoutManager {

        public SmoothScrollLayoutManager(Context context) {
            super(context);
            setOrientation(LinearLayoutManager.VERTICAL);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView,
                                           RecyclerView.State state, final int position) {

            LinearSmoothScroller smoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        // 返回：滑过1px时经历的时间(ms)。
                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return 150f / displayMetrics.densityDpi;
                        }
                    };

            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }
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

            if (Objects.requireNonNull(parent.getAdapter()).getItemViewType(parent.getChildAdapterPosition(view)) != MangaListAdapter.DataItem.TYPE_NORMAL) return;

            if (
                    parent.getChildLayoutPosition(view) == (state.getItemCount() - 1)
            )
                outRect.bottom = LAST_MARGIN_BOTTOM;
        }
    }

    public ChaptersListAdapter(RecyclerView recyclerView, ItemOnClick itemOnClick) {
        this.itemOnClick = itemOnClick;

        this.recyclerView = recyclerView;
        recyclerView.setAdapter(this);
        recyclerView.setLayoutManager(new SmoothScrollLayoutManager(recyclerView.getContext()));
        recyclerView.addItemDecoration(new ItemDecoration(recyclerView.getContext()));
    }

    public void setReadEpId(int read_epid) {
        this.read_epid = read_epid;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.horizontal3_item_list, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, final int position) {
        JSONObject jsonObject = localDataSet.get(position);

        TextView subtitle = viewHolder.getSubtitle();
        TextView title = viewHolder.getTitle();
        subtitle.setText(jsonObject.getString("short_title"));
        title.setText(jsonObject.getString("title"));

        if (jsonObject.getInteger("id") == read_epid) {
            viewHolder.getCard().setChecked(true);
            int color = Color.rgb(0, 0, 0);
            subtitle.setTextColor(color);
            title.setTextColor(color);
        } else if (jsonObject.getInteger("read") == 1) {
            viewHolder.getCard().setChecked(false);
            int color = Color.rgb(117, 117, 117);
            subtitle.setTextColor(color);
            title.setTextColor(color);
        }

//        MaterialCardView materialCardView = viewHolder.getCard();
        ImageView imageView = viewHolder.getIconImage();
        MaterialButton materialButton = viewHolder.getIconButton();
        if (jsonObject.getBoolean("allow_wait_free")) {  // 等免
            materialButton.setVisibility(View.GONE);
            imageView.setImageResource(R.drawable.wait_free);
        } else if (jsonObject.getBoolean("is_locked")) {
            imageView.setVisibility(View.GONE);
            materialButton.setIconResource(R.drawable.baseline_lock_24);
        } else {
            imageView.setVisibility(View.GONE);
            materialButton.setVisibility(View.GONE);
        }

        View view = viewHolder.getView();
        view.post(() -> posYs.put(position, view.getY()));
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void setLocalDataSet(ArrayList<JSONObject> localDataSet) {
        int beforeSize = this.localDataSet.size();
        this.localDataSet.clear();
        notifyItemRangeRemoved(0, beforeSize);

        this.localDataSet.addAll(localDataSet);
        notifyItemRangeInserted(0, localDataSet.size());
    }

    public float getYByPosition(int position) {
        Float f = posYs.get(position);
        return f == null ? 0f : f;
    }
}
