package vip.cdms.wearmanga.mainFragment.home;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.activity.MangaInfoActivity;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.HomeAPI;
import vip.cdms.wearmanga.databinding.FragmentHomeBinding;
import vip.cdms.wearmanga.ui.MangaListAdapter;
import vip.cdms.wearmanga.utils.ActivityUtils;
import vip.cdms.wearmanga.utils.BiliCookieJar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private boolean isRefreshing = false;
    private MangaListAdapter mangaListAdapter;

    /* fragment的控件还没有加到activity中 */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 设置有选项菜单, 在onCreateOptionsMenu中设置内容
//        setHasOptionsMenu(true);

        // 配置SwipeRefresh
        binding.swipeRefresh.setColorSchemeResources(R.color.purple_500);
        binding.swipeRefresh.setOnRefreshListener(this::refresh);

        // 配置RecyclerView
        RecyclerView recyclerView = binding.recyclerView;
        mangaListAdapter = new MangaListAdapter(recyclerView);
        mangaListAdapter.setLayoutVertical();
        mangaListAdapter.setItemDecoration(new MangaListAdapter.ItemDecoration(requireActivity()));

        refresh();

        return root;
    }
    /* fragment的控件已经加到activity中, 可以对MainActivity的控件进行操作 */
    @Override
    public void onStart() {
        super.onStart();

        // 配置appbar
        View appbarMain = requireActivity().findViewById(R.id.app_bar_main);
        BottomAppBar bottomAppBar = appbarMain.findViewById(R.id.bottomAppBar);
//        TextView textView = bottomAppBar.findViewById(R.id.title);
        FloatingActionButton floatingActionButton = appbarMain.findViewById(R.id.fab);

        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
//        textView.setVisibility(View.GONE);
        floatingActionButton.show();
        floatingActionButton.setImageResource(R.drawable.baseline_search_24);

        // 浮动按钮
        floatingActionButton.setOnClickListener(view -> {
        });
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        menu.clear();  // 清空菜单
    }
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void error(String message) {
        ActivityUtils.alert(requireActivity(), null, message);
    }

    private final int maxShow = 8;
    private void refresh() {
        if (isRefreshing) {
            requireActivity().runOnUiThread(() -> binding.swipeRefresh.setRefreshing(false));
            return;
        }
        isRefreshing = true;
        requireActivity().runOnUiThread(() -> {
            binding.swipeRefresh.setRefreshing(true);
            mangaListAdapter.clearDataItems();

            // todo 调试用item, 记得删除
            mangaListAdapter.addDataItem(new MangaListAdapter.DataItemNormal(
                    "https://i0.hdslb.com/bfs/manga-static/6b5ab1a7cb883504db182ee46381835e70d6d460.jpg",
                    "有兽焉",
                    "靴下猫腰子, 分子互动"
            ).setOnClickListener(view -> MangaInfoActivity.startActivity(requireActivity(), view, 29329)));

            loadHomeRecommend();
        });
    }
    private void loadHomeRecommend() {
        mangaListAdapter.addDataItem(new MangaListAdapter.DataItemHeader("为你推荐", null));
        HomeAPI.HomeRecommend(new BiliCookieJar(requireActivity()), new API.JsonDataCallback() {
            @Override
            public void onFailure(Exception e) {
                error(e.toString());
            }
            @Override
            public void onResponse(JSONObject json_root_data) {
                requireActivity().runOnUiThread(() -> {
                    JSONArray json_root_data_list = json_root_data.getJSONArray("list");
                    for (int i = 0; i < Math.min(maxShow, json_root_data_list.size()); i++) {
                        JSONObject json_root_data_list_item = json_root_data_list.getJSONObject(i);

                        StringBuilder stringStyles = new StringBuilder();
                        for (Object jsonElement : json_root_data_list_item.getJSONArray("styles")) {
                            if (!(jsonElement instanceof JSONObject)) continue;
                            if (!stringStyles.toString().isEmpty()) stringStyles.append(", ");
                            stringStyles.append(((JSONObject) jsonElement).getString("name"));
                        }

                        mangaListAdapter.addDataItem(new MangaListAdapter.DataItemNormal(
                                json_root_data_list_item.getString("vertical_cover"),
                                json_root_data_list_item.getString("title"),
                                stringStyles.toString()
                        ).setOnClickListener(view -> MangaInfoActivity.startActivity(requireActivity(), view, json_root_data_list_item.getInteger("comic_id"))));
                    }
                    loadGetClassPageLayout();
//                    binding.swipeRefresh.setRefreshing(false);
//                    isRefreshing = false;
                });
            }
        });
    }
    private void loadGetClassPageLayout() {
        mangaListAdapter.addDataItem(new MangaListAdapter.DataItemHeader("热门速递", null));
        HomeAPI.GetClassPageLayout(271, new BiliCookieJar(requireActivity()), new API.JsonDataCallback() {
            @Override
            public void onFailure(Exception e) {
                error(e.toString());
            }
            @Override
            public void onResponse(JSONObject json_root_data) {
                JSONArray json_root_data_layout = json_root_data.getJSONArray("layout");
                loadGetClassPageSixComics(json_root_data_layout, 1);
            }
        });
    }
    private void loadGetClassPageSixComics(JSONArray json_root_data_layout, int index) {
        JSONObject json_root_data_layout_item = json_root_data_layout.getJSONObject(index);
        requireActivity().runOnUiThread(() -> mangaListAdapter.addDataItem(new MangaListAdapter.DataItemMiddleHeader(json_root_data_layout_item.getString("name"))));
        HomeAPI.GetClassPageSixComics(
                json_root_data_layout_item.getInteger("id"),
                1,
                8,
                new BiliCookieJar(requireActivity()), new API.JsonDataCallback() {
            @Override
            public void onFailure(Exception e) {
                error(e.toString());
            }
            @Override
            public void onResponse(JSONObject json_root_data) {
                requireActivity().runOnUiThread(() -> {
                    JSONArray json_root_data_roll_six_comics = json_root_data.getJSONArray("roll_six_comics");
                    for (int i = 0; i < Math.min(maxShow, json_root_data_roll_six_comics.size()); i++) {
                        JSONObject json_root_data_list_item = json_root_data_roll_six_comics.getJSONObject(i);
                        mangaListAdapter.addDataItem(new MangaListAdapter.DataItemNormal(
                                json_root_data_list_item.getString("vertical_cover"),
                                json_root_data_list_item.getString("title"),
                                json_root_data_list_item.getString("recommendation")
                        ).setOnClickListener(view -> MangaInfoActivity.startActivity(requireActivity(), view, json_root_data_list_item.getInteger("comic_id"))));
                    }
                    if (index == (json_root_data_layout.size() - 1)) {
                        binding.swipeRefresh.setRefreshing(false);
                        isRefreshing = false;
                    } else loadGetClassPageSixComics(json_root_data_layout, index + 1);
                });
            }
        });
    }
}
