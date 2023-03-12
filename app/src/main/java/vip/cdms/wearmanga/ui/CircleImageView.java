package vip.cdms.wearmanga.ui;

import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import org.jetbrains.annotations.NotNull;

public class CircleImageView extends AppCompatImageView {
    private final RectF srcRectF;
    private final Paint paint;
    private final Path path;

    private final Xfermode xfermode;

    private int width;
    private int height;
    private float radius;

    public CircleImageView(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public CircleImageView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        srcRectF = new RectF();

        paint = new Paint();
        path = new Path();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        } else {
            xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        }
    }

    /**
     * 计算图片原始区域的RectF
     */
    private void initSrcRectF() {
        radius = Math.min(width, height) / 2.0f;
        srcRectF.set(width / 2.0f - radius, height / 2.0f - radius, width / 2.0f + radius, height / 2.0f + radius);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        initSrcRectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 使用离屏缓存，新建一个srcRectF区域大小的图层
        canvas.saveLayer(srcRectF, null, Canvas.ALL_SAVE_FLAG);
//        canvas.saveLayer(srcRectF, null);
        // ImageView自身的绘制流程，即绘制图片
        super.onDraw(canvas);
        // 给path添加一个圆形
        path.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW);
        paint.setAntiAlias(true);
        // 画笔为填充模式
        paint.setStyle(Paint.Style.FILL);
        // 设置混合模式
        paint.setXfermode(xfermode);
        // 绘制path
        canvas.drawPath(path, paint);
        // 清除Xfermode
        paint.setXfermode(null);
        // 恢复画布状态
        canvas.restore();
    }
}
