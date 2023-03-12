package vip.cdms.wearmanga.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import vip.cdms.wearmanga.databinding.ActivityComicHorizontalReaderBinding;

/**
 * 漫画阅读器 - 左右翻页
 */
public class ComicHorizontalReaderActivity extends AppCompatActivity {
    private ActivityComicHorizontalReaderBinding binding;

    private int comicId;
    private int epId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityComicHorizontalReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        comicId = intent.getIntExtra("comic_id", -1);
        epId = intent.getIntExtra("ep_id", -1);
    }

    public static void startActivity(Activity activity, int comic_id, int ep_id) {
        activity.startActivity(
                new Intent(activity, ComicHorizontalReaderActivity.class)
                        .putExtra("comic_id", comic_id)
                        .putExtra("ep_id", ep_id)
        );
    }
}
