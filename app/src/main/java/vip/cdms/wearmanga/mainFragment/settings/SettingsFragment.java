package vip.cdms.wearmanga.mainFragment.settings;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import vip.cdms.wearmanga.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onStart() {
        super.onStart();

        // 配置appbar
        View appbarMain = requireActivity().findViewById(R.id.app_bar_main);
        FloatingActionButton floatingActionButton = appbarMain.findViewById(R.id.fab);
        floatingActionButton.hide();
    }
}
