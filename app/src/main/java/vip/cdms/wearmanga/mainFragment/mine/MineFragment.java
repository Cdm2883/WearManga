package vip.cdms.wearmanga.mainFragment.mine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.R;
import vip.cdms.wearmanga.activity.LoginActivity;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.BiliAPIError;
import vip.cdms.wearmanga.api.UserAPI;
import vip.cdms.wearmanga.databinding.FragmentMineBinding;
import vip.cdms.wearmanga.utils.ActivityUtils;
import vip.cdms.wearmanga.utils.BiliCookieJar;

import static android.content.Context.MODE_PRIVATE;

public class MineFragment extends Fragment {

    private FragmentMineBinding binding;

    /* fragment的控件还没有加到activity中 */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences tempSharedPreferences = requireActivity().getSharedPreferences("temp", Context.MODE_PRIVATE);
        SharedPreferences.Editor tempEditor = tempSharedPreferences.edit();
        tempEditor.putInt("main_start_destination_id", R.id.nav_mine);
        tempEditor.apply();

        // 设置有选项菜单, 在onCreateOptionsMenu中设置内容
        setHasOptionsMenu(true);

        UserAPI.nav(new BiliCookieJar(requireActivity()), new API.JsonDataCallback<JSONObject>() {
            @Override
            public void onFailure(Exception e, JSONObject json_root) {
                if (e instanceof BiliAPIError) {
                    BiliAPIError biliAPIError = (BiliAPIError) e;
                    int code = biliAPIError.getCode();
                    if (code == -101) requireActivity().runOnUiThread(() -> {
                        binding.avatar.setVisibility(View.GONE);
                        binding.title.setText("你还没有登录");
                        binding.subtitle.setText("点击卡片进行登录");
                        binding.card.setOnClickListener(view -> requireActivity().startActivity(new Intent(requireActivity(), LoginActivity.class)));
                        requireActivity().startActivity(new Intent(requireActivity(), LoginActivity.class));
                    }); else ActivityUtils.alert(requireActivity(), e);
                } else ActivityUtils.alert(requireActivity(), e);
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject json_root_data) {
                requireActivity().runOnUiThread(() -> {
                    binding.title.setText(json_root_data.getString("uname"));
                    binding.subtitle.setText("UID: " + json_root_data.getInteger("mid"));
                    Glide.with(binding.card)
                            .setDefaultRequestOptions(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(binding.avatar.getWidth(), binding.avatar.getHeight())
                                    .format(DecodeFormat.PREFER_RGB_565))
                            .load(json_root_data.getString("face"))
                            .placeholder(R.drawable.baseline_account_circle_24)
                            .into(binding.avatar);
                });
            }
        });

//        binding.avatar.setImageResource(R.drawable.baseline_account_circle_24);

        return root;
    }
    /* fragment的控件已经加到activity中, 可以对MainActivity的控件进行操作 */
    @Override
    public void onStart() {
        super.onStart();

        // 配置appbar
        View appbarMain = requireActivity().findViewById(R.id.app_bar_main);
//        BottomAppBar bottomAppBar = appbarMain.findViewById(R.id.bottomAppBar);
//        TextView textView = bottomAppBar.findViewById(R.id.title);
        FloatingActionButton floatingActionButton = appbarMain.findViewById(R.id.fab);

//        textView.setVisibility(View.GONE);
        floatingActionButton.hide();
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        menu.clear();  // 清空菜单
        // menu.add(GroupID, ItemId, OrderId, Title);
        // - GroupID: 代表的是组概念, 你可以将几个菜单项归为一组, 以便更好的以组的方式管理你的菜单按钮
        // - ItemId:  代表的是项目编号. 这个参数非常重要, 一个itemID对应一个menu中的选项
        // - OrderId: 代表的是菜单项的显示顺序, 默认是0. 表示菜单的显示顺序就是按照add的显示顺序来显示
        // - Title:   表示选项中显示的文字
        int itemId = 0;
        menu.add(Menu.FIRST, ++itemId, Menu.NONE, "退出登录");
    }
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("bili", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("DedeUserID", "");
                editor.putString("DedeUserID__ckMd5", "");
                editor.putString("SESSDATA", "");
                editor.putString("bili_jct", "");
                if (editor.commit()) {
                    ActivityUtils.restartApp(requireActivity());
                } else ActivityUtils.alert(requireActivity(), null, "commit failed");

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
