package vip.cdms.wearmanga;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.navigation.NavigationView;
import vip.cdms.wearmanga.api.API;
import vip.cdms.wearmanga.api.BiliAPIError;
import vip.cdms.wearmanga.api.UserAPI;
import vip.cdms.wearmanga.databinding.ActivityMainBinding;
import vip.cdms.wearmanga.utils.ActivityUtils;
import vip.cdms.wearmanga.utils.BiliCookieJar;
import vip.cdms.wearmanga.utils.TimeUtils;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    public ActivityMainBinding binding;

    //    private View navHeader;
    private TextView drawerHeaderTitle;
    private TextView drawerHeaderSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.bottomAppBar);
        View navHeader = binding.navView.inflateHeaderView(R.layout.nav_header_main);
        drawerHeaderTitle = navHeader.findViewById(R.id.drawerHeaderTitle);
        drawerHeaderSubtitle = navHeader.findViewById(R.id.drawerHeaderSubtitle);

        /* 侧边栏 */
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // 侧边栏宽度全屏
        ViewGroup.LayoutParams navigationViewLayoutParams = navigationView.getLayoutParams();
        navigationViewLayoutParams.width = getResources().getDisplayMetrics().widthPixels;
        navigationView.setLayoutParams(navigationViewLayoutParams);
        // 配置导航
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_mine)  // 在这里添加item
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        UserAPI.nav(new BiliCookieJar(this), new API.JsonDataCallback() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof BiliAPIError) {
                    BiliAPIError biliAPIError = (BiliAPIError) e;
                    int code = biliAPIError.getCode();
                    if (code == -101) runOnUiThread(() -> {
                            drawerHeaderTitle.setText("你还没有登录");
                            drawerHeaderSubtitle.setText("登录获取更多功能~ (点击\"我的\"进行扫码登录)");
                    }); else ActivityUtils.alert(MainActivity.this, e);
                } else ActivityUtils.alert(MainActivity.this, e);
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(JSONObject json_root_data) {
                runOnUiThread(() -> {
                    drawerHeaderTitle.setText(json_root_data.getString("uname"));
                    drawerHeaderSubtitle.setText("WearManga");
                });
            }
        });
    }

    private boolean onRestartFabShow = false;
    private boolean onRestartAppbarShow = false;
    // 跳转activity
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {if (!binding.appBarMain.fab.isOrWillBeHidden()) onRestartFabShow = true;
//        if (!binding.appBarMain.bottomAppBar.isScrolledDown()) onRestartAppbarShow = true;

        binding.appBarMain.fab.hide();
        binding.appBarMain.bottomAppBar.performHide();

        super.startActivity(intent, options);
    }
    // 回到activity
    @Override
    protected void onRestart() {
        super.onRestart();

        TimeUtils.setTimeout(() -> runOnUiThread(() -> {
            if (onRestartFabShow) binding.appBarMain.fab.show();
            if (onRestartAppbarShow) binding.appBarMain.bottomAppBar.performShow();

            onRestartFabShow = false;
            onRestartAppbarShow = false;
        }), 700);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // 修改图标颜色
        for(int i = 0; i < menu.size(); i++) {
            Drawable iconDrawable = menu.getItem(i).getIcon();
            if (iconDrawable != null) {
                iconDrawable.mutate();
                iconDrawable.setColorFilter(
                        getResources().getColor(R.color.icon),
                        PorterDuff.Mode.SRC_ATOP
                );
            }
        }
        // 通过反射使其图标可见
//        if (menu != null) {
        if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
            try {
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}