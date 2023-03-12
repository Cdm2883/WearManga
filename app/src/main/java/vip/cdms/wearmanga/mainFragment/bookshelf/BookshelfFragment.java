package vip.cdms.wearmanga.mainFragment.bookshelf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.activity.MangaInfoActivity;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.BookshelfAPI;
import vip.cdms.wearmanga.api.UserAPI;
import vip.cdms.wearmanga.databinding.FragmentBookshelfBinding;
import vip.cdms.wearmanga.ui.MangaListAdapter;
import vip.cdms.wearmanga.utils.BiliCookieJar;
import vip.cdms.wearmanga.utils.StringUtils;

public class BookshelfFragment extends Fragment {
    private FragmentBookshelfBinding binding;

    private boolean isRefreshing = false;
    private MangaListAdapter mangaListAdapter;
    private FloatingActionButton floatingActionButton;

    private SharedPreferences temp;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentBookshelfBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        temp = requireActivity().getSharedPreferences("temp", Context.MODE_PRIVATE);
        SharedPreferences.Editor tempEditor = temp.edit();
        tempEditor.putInt("main_start_destination_id", R.id.nav_bookshelf);
        tempEditor.apply();

        // 配置SwipeRefresh
        binding.swipeRefresh.setColorSchemeResources(R.color.purple_500);
        binding.swipeRefresh.setOnRefreshListener(this::refresh);

        // 配置RecyclerView
        RecyclerView recyclerView = binding.recyclerView;
        mangaListAdapter = new MangaListAdapter(recyclerView);
        mangaListAdapter.setLayoutVertical();
        mangaListAdapter.setItemDecoration(new MangaListAdapter.ItemDecoration(requireActivity()));
        mangaListAdapter.setCheckable(true);

        refresh();

        return root;
    }
    @Override
    public void onStart() {
        super.onStart();

        // 配置appbar
        View appbarMain = requireActivity().findViewById(R.id.app_bar_main);
//        BottomAppBar bottomAppBar = appbarMain.findViewById(R.id.bottomAppBar);
        floatingActionButton = appbarMain.findViewById(R.id.fab);
        floatingActionButton.setImageResource(R.drawable.baseline_delete_24);
        floatingActionButton.hide();
        floatingActionButton.setOnClickListener(v -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
            materialAlertDialogBuilder.setTitle("你真的要删除这些吗?");
            materialAlertDialogBuilder.setPositiveButton("删除", (dialog, which) -> {
                BiliCookieJar biliCookieJar = new BiliCookieJar(requireActivity());
                int[] checkList = mangaListAdapter.checkList();
                API.JsonDataCallback<JSONObject> callback = API.getJsonDataCallbackAutoE(requireActivity(), json_root_data -> requireActivity().runOnUiThread(this::refresh));
                if (loadType == 1) BookshelfAPI.DeleteFavorite(biliCookieJar, checkList, callback);
                else if (loadType == 2) BookshelfAPI.DeleteHistory(biliCookieJar, checkList, callback);
                mangaListAdapter.checkListClear();
                floatingActionButton.hide();
            });
            materialAlertDialogBuilder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
            materialAlertDialogBuilder.show();
        });
    }

    private int loadType = 1;
//    private int loadType1Type = BookshelfAPI.LIST_FAV_ORDER_CHRONOLOGICAL;
    private int loadType1Type = -1;
    private void refresh() {
        if (isRefreshing) {
            requireActivity().runOnUiThread(() -> binding.swipeRefresh.setRefreshing(false));
            return;
        }
        isRefreshing = true;
        if (loadType1Type == -1) loadType1Type = temp.getInt("bookshelf_fragment_load_type_1_type", BookshelfAPI.LIST_FAV_ORDER_CHRONOLOGICAL);
        requireActivity().runOnUiThread(() -> {
            binding.swipeRefresh.setRefreshing(true);
            mangaListAdapter.clearDataItems();
        });
        loadPage(1);
    }

    private void loadPage(int page) {
        Runnable noMore = () -> mangaListAdapter.addDataItem(new MangaListAdapter.DataItemMiddleHeader("没有更多了 ＞△＜"));
        if (loadType == 1) BookshelfAPI.ListFavorite(new BiliCookieJar(requireActivity()), loadType1Type, page, 15, API.getJsonDataCallbackAutoE(requireActivity(), json_root_data -> requireActivity().runOnUiThread(() -> {
            if (!mangaListAdapter.isCheckable()) mangaListAdapter.setCheckable(true);
            if (mangaListAdapter.getItemCount() == 0) {
                SpannableString spannableString = new SpannableString("追漫顺序 更新时间 最近阅读 完成等免");
                int spannableStart = -1;
                int spannableEnd = -1;
                if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_CHRONOLOGICAL) {
                    spannableStart = 0;
                    spannableEnd = 4;
                } else if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_UPDATE) {
                    spannableStart = 5;
                    spannableEnd = 9;
                } else if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_RECENTLY_READ) {
                    spannableStart = 10;
                    spannableEnd = 14;
                } else if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_WAIT_FREE) {
                    spannableStart = 15;
                    spannableEnd = 19;
                }
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), spannableStart, spannableEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new RelativeSizeSpan(1.2f), spannableStart, spannableEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mangaListAdapter.addDataItem(new MangaListAdapter.DataItemHeader(
                        "我的追漫",
                        spannableString
                ).setOnClickListener(v -> {
                    if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_CHRONOLOGICAL) loadType1Type = BookshelfAPI.LIST_FAV_ORDER_UPDATE;
                    else if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_UPDATE) loadType1Type = BookshelfAPI.LIST_FAV_ORDER_RECENTLY_READ;
                    else if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_RECENTLY_READ) loadType1Type = BookshelfAPI.LIST_FAV_ORDER_WAIT_FREE;
                    else if (loadType1Type == BookshelfAPI.LIST_FAV_ORDER_WAIT_FREE) loadType1Type = BookshelfAPI.LIST_FAV_ORDER_CHRONOLOGICAL;

                    SharedPreferences.Editor tempEditor = temp.edit();
                    tempEditor.putInt("bookshelf_fragment_load_type_1_type", loadType1Type);
                    tempEditor.apply();

                    refresh();
                }));
            }
            isRefreshing = false;
            binding.swipeRefresh.setRefreshing(false);
            if (json_root_data.size() == 0) {
                noMore.run();
                return;
            }
            load(json_root_data);
            loadPage(page + 1);
        })));
        else if (loadType == 2) BookshelfAPI.ListHistory(new BiliCookieJar(requireActivity()), page, 15, API.getJsonDataCallbackAutoE(requireActivity(), json_root_data -> requireActivity().runOnUiThread(() -> {
            if (!mangaListAdapter.isCheckable()) mangaListAdapter.setCheckable(true);
            if (mangaListAdapter.getItemCount() == 0) mangaListAdapter.addDataItem(new MangaListAdapter.DataItemHeader(
                    "阅读历史",
                    null
            ));
            isRefreshing = false;
            binding.swipeRefresh.setRefreshing(false);
            if (json_root_data.size() == 0) {
                noMore.run();
                return;
            }
            load(json_root_data);
            loadPage(page + 1);
        })));
        else if (loadType == 3) UserAPI.GetAutoBuyComics(new BiliCookieJar(requireActivity()), page, 15, API.getJsonDataCallbackAutoE(requireActivity(), json_root_data -> requireActivity().runOnUiThread(() -> {
            if (mangaListAdapter.isCheckable()) mangaListAdapter.setCheckable(false);
            if (mangaListAdapter.getItemCount() == 0) mangaListAdapter.addDataItem(new MangaListAdapter.DataItemHeader(
                    "已购漫画",
                    null
            ));
            isRefreshing = false;
            binding.swipeRefresh.setRefreshing(false);
            if (json_root_data.size() == 0) {
                noMore.run();
                return;
            }
            load(json_root_data);
            loadPage(page + 1);
        })));
    }

    private void load(JSONArray data) {
        for (int i = 0; i < data.size(); i++) {
            JSONObject json_root_data_list_item = data.getJSONObject(i);

            mangaListAdapter.addDataItem(new MangaListAdapter.DataItemNormal(
                    json_root_data_list_item.getString("vcover"),
                    json_root_data_list_item.getString("title"),
                    "看到 " + StringUtils.shortTitle(json_root_data_list_item.getString("last_ep_short_title")) + " / 更新至 " + StringUtils.shortTitle(json_root_data_list_item.getString("latest_ep_short_title"))
            ).setOnClickListener(view -> MangaInfoActivity.startActivity(requireActivity(), view, json_root_data_list_item.getInteger("comic_id")))
                    .id(json_root_data_list_item.getInteger("comic_id"))
                    .setOnLongClickListener(v -> {
                        if (!mangaListAdapter.isCheckable()) return true;
                        if (mangaListAdapter.checkList().length == 1) floatingActionButton.show();
                        else floatingActionButton.hide();
                        return true;
                    }));
        }
    }

    // menu
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        menu.clear();  // 清空菜单
        // menu.add(GroupID, ItemId, OrderId, Title);
        // - GroupID: 代表的是组概念, 你可以将几个菜单项归为一组, 以便更好的以组的方式管理你的菜单按钮
        // - ItemId:  代表的是项目编号. 这个参数非常重要, 一个itemID对应一个menu中的选项
        // - OrderId: 代表的是菜单项的显示顺序, 默认是0. 表示菜单的显示顺序就是按照add的显示顺序来显示
        // - Title:   表示选项中显示的文字
        int itemId = 0;
        menu.add(Menu.FIRST, ++itemId, Menu.NONE, "我的追漫")
                .setIcon(R.drawable.baseline_check_24)
                .setOnMenuItemClickListener(item -> {
                    loadType = 1;
                    refresh();
                    menu.getItem(0).setIcon(R.drawable.baseline_check_24);
                    menu.getItem(1).setIcon(R.drawable.empty_24);
                    menu.getItem(2).setIcon(R.drawable.empty_24);
                    return true;
                });
        menu.add(Menu.FIRST, ++itemId, Menu.NONE, "阅读历史")
                .setIcon(R.drawable.empty_24)
                .setOnMenuItemClickListener(item -> {
                    loadType = 2;
                    refresh();
                    menu.getItem(0).setIcon(R.drawable.empty_24);
                    menu.getItem(1).setIcon(R.drawable.baseline_check_24);
                    menu.getItem(2).setIcon(R.drawable.empty_24);
                    return true;
                });
        menu.add(Menu.FIRST, ++itemId, Menu.NONE, "已购漫画")
                .setIcon(R.drawable.empty_24)
                .setOnMenuItemClickListener(item -> {
                    loadType = 3;
                    refresh();
                    menu.getItem(0).setIcon(R.drawable.empty_24);
                    menu.getItem(1).setIcon(R.drawable.empty_24);
                    menu.getItem(2).setIcon(R.drawable.baseline_check_24);
                    return true;
                });
    }
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
