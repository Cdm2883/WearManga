package vip.cdms.wearmanga;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
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
import vip.cdms.wearmanga.utils.SettingsUtils;
import vip.cdms.wearmanga.utils.TimeUtils;

import java.lang.reflect.Method;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    public ActivityMainBinding binding;

    //    private View navHeader;
    private TextView drawerHeaderTitle;
    private TextView drawerHeaderSubtitle;

    private SharedPreferences temp;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.bottomAppBar);
        View navHeader = binding.navView.inflateHeaderView(R.layout.nav_header_main);
        drawerHeaderTitle = navHeader.findViewById(R.id.drawerHeaderTitle);
        drawerHeaderSubtitle = navHeader.findViewById(R.id.drawerHeaderSubtitle);

        temp = getSharedPreferences("temp", MODE_PRIVATE);
        settings = SettingsUtils.getSettings(this);

        /* ????????? */
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // ?????????????????????
        ViewGroup.LayoutParams navigationViewLayoutParams = navigationView.getLayoutParams();
        navigationViewLayoutParams.width = getResources().getDisplayMetrics().widthPixels;
        navigationView.setLayoutParams(navigationViewLayoutParams);
        // ????????????
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_bookshelf, R.id.nav_mine/*, R.id.nav_settings*/)  // ???????????????item
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        NavInflater navInflater = navController.getNavInflater();
        NavGraph navGraph = navInflater.inflate(R.navigation.mobile_navigation);

        int startDestination = R.id.nav_home;
        String[] mainStartDestinationValues = getResources().getStringArray(R.array.main_start_destination_values);
        String mainStartDestination = settings.getString("main_start_destination", mainStartDestinationValues[0]);
        if (Objects.equals(mainStartDestination, mainStartDestinationValues[0])) startDestination = temp.getInt("main_start_destination_id", R.id.nav_home);
//        else if (Objects.equals(mainStartDestination, mainStartDestinationValues[1])) startDestination = R.id.nav_home;
        else if (Objects.equals(mainStartDestination, mainStartDestinationValues[2])) startDestination = R.id.nav_classification;
        else if (Objects.equals(mainStartDestination, mainStartDestinationValues[3])) startDestination = R.id.nav_bookshelf;
        else if (Objects.equals(mainStartDestination, mainStartDestinationValues[4])) startDestination = R.id.nav_mine;

        navGraph.setStartDestination(startDestination);
        navController.setGraph(navGraph);

        UserAPI.nav(new BiliCookieJar(this), new API.JsonDataCallback<JSONObject>() {
            @Override
            public void onFailure(Exception e, JSONObject json_root) {
                if (e instanceof BiliAPIError) {
                    BiliAPIError biliAPIError = (BiliAPIError) e;
                    int code = biliAPIError.getCode();
                    if (code == -101) runOnUiThread(() -> {
                            drawerHeaderTitle.setText("??????????????????");
                            drawerHeaderSubtitle.setText("????????????????????????~ (??????\"??????\"??????????????????)");
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
    // ??????activity
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {if (!binding.appBarMain.fab.isOrWillBeHidden()) onRestartFabShow = true;
//        if (!binding.appBarMain.bottomAppBar.isScrolledDown()) onRestartAppbarShow = true;

        binding.appBarMain.fab.hide();
        binding.appBarMain.bottomAppBar.performHide();

        super.startActivity(intent, options);
    }
    // ??????activity
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
        // ??????????????????
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
        // ??????????????????????????????
        if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
            try {
                Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}