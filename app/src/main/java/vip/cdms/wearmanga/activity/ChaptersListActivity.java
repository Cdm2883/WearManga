package vip.cdms.wearmanga.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.ComicAPI;
import vip.cdms.wearmanga.databinding.ActivityChaptersListBinding;
import vip.cdms.wearmanga.ui.ChaptersListAdapter;
import vip.cdms.wearmanga.utils.ActivityUtils;
import vip.cdms.wearmanga.utils.BiliCookieJar;
import vip.cdms.wearmanga.utils.DensityUtil;
import vip.cdms.wearmanga.utils.SnackbarMaker;

import java.util.ArrayList;

public class ChaptersListActivity extends AppCompatActivity {
    private ActivityChaptersListBinding binding;

    private int comicId;
    private JSONArray ep_list_json;
    private final ArrayList<ArrayList<JSONObject>> ep_list = new ArrayList<>();

    private int readEpId;

    private int nowPage = 0;

    private RecyclerView recyclerView;
    private ChaptersListAdapter chaptersListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChaptersListBinding.inflate(getLayoutInflater());

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.right_drawer);
        getWindow().setExitTransition(transition);  // 退出时使用
        getWindow().setEnterTransition(transition);  //第一次进入时使用
        getWindow().setReenterTransition(transition);  //再次进入时使用

        setContentView(binding.getRoot());

        binding.chipExit.setOnClickListener(view -> ActivityCompat.finishAfterTransition(this));

        Bundle bundle = getIntent().getExtras();
        comicId = bundle.getInt("comic_id", -1);
        readEpId = bundle.getInt("read_epid", -1);

        recyclerView = binding.recyclerView;
        chaptersListAdapter = new ChaptersListAdapter(recyclerView, jsonObject -> {
            // todo
            SnackbarMaker.makeTop(binding.getRoot(), jsonObject.getString("title"), Snackbar.LENGTH_SHORT).show();
        });
        chaptersListAdapter.setReadEpId(readEpId);

        binding.fabScrollToTop.setOnClickListener(view -> binding.nestedScrollView.smoothScrollTo(0, 0));
        binding.fabScrollToTop.post(() -> binding.fabScrollToTop.hide());
        binding.nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > 10) runOnUiThread(() -> binding.fabScrollToTop.show());
            else runOnUiThread(() -> binding.fabScrollToTop.hide());
        });

        binding.fab.setOnClickListener(view -> {
            int pageNum = -1;
            int itemOrder = -1;
            flag: for (int i = 0; i < ep_list.size(); i++) {
                ArrayList<JSONObject> aPage = ep_list.get(i);
                for (int j = 0; j < aPage.size(); j++) {
                    JSONObject jsonObject = aPage.get(j);
                    if (jsonObject.getInteger("id") == readEpId) {
                        pageNum = i;
                        itemOrder = j;
                        break flag;
                    }
                }
            }

            if (pageNum == -1)
                SnackbarMaker.makeTop(binding.getRoot(), "没有找到阅读记录 :(", Snackbar.LENGTH_LONG).show();
            else {
                if (nowPage != pageNum) loadPage(pageNum);

                int finalItemOrder = itemOrder;
                runOnUiThread(() -> binding.nestedScrollView.smoothScrollTo(
                        0,
                        (int) (binding.chipGroup.getHeight() + chaptersListAdapter.getYByPosition(finalItemOrder) - DensityUtil.dp2px(ChaptersListActivity.this, 8)))
                );
            }
        });

        if (bundle.get("ep_list") == null)
            ComicAPI.ComicDetail(
                    new BiliCookieJar(this),
                    comicId,
                    new API.JsonDataCallback() {
                        @Override
                        public void onFailure(Exception e) {
                            ActivityUtils.alert(ChaptersListActivity.this, e);
                        }
                        @Override
                        public void onResponse(JSONObject json_root_data) {
                            readEpId = json_root_data.getInteger("read_epid");
                            chaptersListAdapter.setReadEpId(readEpId);
                            ep_list_json = json_root_data.getJSONArray("ep_list");

                            load();
                        }
                    }
            );
        else {
            ep_list_json = JSONArray.parseArray(bundle.getString("ep_list", "[]"));
            load();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void load() {
        if (ep_list_json.get(0) instanceof JSONArray) {
//            for (Object arrayItem : ep_list_json) {
//                for (Object item : (JSONArray) arrayItem) epListPut((JSONObject) item);
//            }
            for (int i = ep_list_json.size() - 1; i >= 0; i--) {
                JSONArray arrayItem = (JSONArray) ep_list_json.get(i);
                for (int j = arrayItem.size() - 1; j >= 0; j--) epListPut((JSONObject) arrayItem.get(i));
            }
        } else if (ep_list_json.get(0) instanceof JSONObject) {
            for (int i = ep_list_json.size() - 1; i >= 0; i--) epListPut((JSONObject) ep_list_json.get(i));
//            for (Object item : ep_list_json) epListPut((JSONObject) item);
        }

        ep_list_json = null;
        for (int i = 0; i < ep_list.size(); i++) addPageItem(i);
    }
    private void epListPut(JSONObject jsonObject) {
        if (ep_list.isEmpty()) {
            ep_list.add(new ArrayList<>());
        } else if (ep_list.get(ep_list.size() - 1).size() >= 50) {
            ep_list.add(new ArrayList<>());
        }

        ep_list.get(ep_list.size() - 1).add(jsonObject);
    }

    private final ArrayList<Chip> pageItems = new ArrayList<>();
    @SuppressLint("SetTextI18n")
    private void addPageItem(int page) {
        page = page + 1;
        int finalPage = page;
        runOnUiThread(() -> {
            Chip chip = (Chip) this.getLayoutInflater().inflate(R.layout.pages_item_chip_choice, binding.chipGroup, false);
            /*
            1   ->   1            50
            2   ->   51           100
            3   ->   101          150
            ...
            n   ->   (n-1)*50+1   n*50
             */
            chip.setText(((finalPage - 1) * 50 + 1) + " - " + (finalPage * 50));
            chip.setOnClickListener(view -> loadPage(finalPage - 1));
            binding.chipGroup.addView(chip);
            pageItems.add(chip);

            if (finalPage == 1) loadPage(0);
        });
    }

    private void loadPage(int page) {
        nowPage = page;
        runOnUiThread(() -> {
            for (Chip chip : pageItems) chip.setChecked(false);
            pageItems.get(page).setChecked(true);

            chaptersListAdapter.setLocalDataSet(ep_list.get(page));
        });
    }

    public static void startActivity(Activity activity, int comic_id, JSONArray ep_list, int read_epid) {
        Bundle bundle = new Bundle();

        String epListString = ep_list.toString();
        if (epListString.getBytes(/*StandardCharsets.UTF_8*/).length >= 300000) bundle.putString("ep_list", null);  // 传输不能大小大于1MB
        else bundle.putString("ep_list", epListString);

        bundle.putInt("comic_id", comic_id);
        bundle.putInt("read_epid", read_epid);

        activity.startActivity(
                new Intent(activity, ChaptersListActivity.class)
                        .putExtras(bundle),
                ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
        );
    }
}