package com.bird2fish;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bird2fish.travelbook.R;

import java.util.ArrayList;
import java.util.List;

public class Carousel {

    private Context mContext;   //全局的Context用于加载图片
    private LinearLayout dotLinearLayout;   //用于加载标志点的LinearLayout
    private List<ImageView> mDotViewList = new ArrayList<>();//点视图集合 每个ImageView表示一个点

    private Handler autoScrollHandler;  //控制自动轮播的线程

    private List<String> originalImages = new ArrayList<>();   //存放这需要轮播的图片

    private ViewPager2 viewPager2;

    private long AUTO_SCROLL_INTERVAL = 1_500; // 设置自动滚动的间隔时间，单位为毫秒

    private boolean AUTO_SCROLL = false;    //是否设置自动播放


    /**
     * @param AUTO_SCROLL_INTERVAL 设置轮播图自动滚动时间
     */
    public void setAUTO_SCROLL_INTERVAL(long AUTO_SCROLL_INTERVAL) {
        this.AUTO_SCROLL_INTERVAL = AUTO_SCROLL_INTERVAL;
    }

    public Carousel(Context mContext, LinearLayout dotLinearLayout, ViewPager2 viewPager2) {
        this.mContext = mContext;
        this.dotLinearLayout = dotLinearLayout;
        this.viewPager2 = viewPager2;

        autoScrollHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 用于启动轮播图效果
     *
     * @param resource 图片的资源ID
     */
    public void initViews(List<String> resource, RecyclerView.Adapter adapter) {

        //加载初始化绑定轮播图
        for (String id : resource) {
            originalImages.add(id);

            //制作标志点的ImageView，并且初始化加载第一张图片标志点
            ImageView dotImageView = new ImageView(mContext);
            if (originalImages.size() == 1) {
                dotImageView.setImageResource(R.drawable.red_dot);
            } else {
                dotImageView.setImageResource(R.drawable.grey_dot);
            }

            //设置标志点的布局参数
            LinearLayout.LayoutParams dotImageLayoutParams = new LinearLayout.LayoutParams(10, 10);
            dotImageLayoutParams.setMargins(5, 0, 5, 0);

            //将布局参数绑定到标志点视图
            dotImageView.setLayoutParams(dotImageLayoutParams);

            //保存标志点便于后续动态修改
            mDotViewList.add(dotImageView);

            //将标志点的视图绑定在Layout中
            dotLinearLayout.addView(dotImageView);
        }

        //originalImages.add(0, originalImages.get(originalImages.size() - 1));  //将originalImages的最后一张照片插入到开头
        //originalImages.add(originalImages.get(1));  //将originalImages的第2张照片插入到结尾

        viewPager2.setAdapter(adapter);

        // 设置当前项为数据集的第一个元素，使其显示为轮播图的开始
        viewPager2.setCurrentItem(0, false);

        // 添加页面更改监听器，以实现循环滚动
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                // 当滑动开始时停止自动滚动
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                }
                // 当滑动结束时重新启动自动滚动
                else if (state == ViewPager2.SCROLL_STATE_IDLE && AUTO_SCROLL) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable); // 移除之前的回调,防止多次启动的情况
                    autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                // position 表示当前页面的索引
                // positionOffset 表示当前页面的偏移比例
                // positionOffsetPixels 表示当前页面的偏移像素

                // 在这里可以根据需要处理从哪一页拖向哪一页的逻辑
                mixColor(mDotViewList.get(position),  0.5f, Color.RED, Color.GRAY);
                if (positionOffset > 0){
                    int nextPos = position +1;
                    if (nextPos < mDotViewList.size()){
                        //Math.abs(positionOffset)
                        mixColor(mDotViewList.get(nextPos),   0.5f,  Color.GRAY, Color.RED);
                    }
                }else{
                    int nextPos = position -1;
                    if (nextPos  >= 0){
                        mixColor(mDotViewList.get(nextPos),  0.5f, Color.GRAY, Color.RED);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

                //动态设置图片的下标点位
                for (int i = 0; i < mDotViewList.size(); i++) {
                    //由于在第一张图片前添加一张过渡图，因此position比mDotViewList对于的标志点多一位
                    if (i == position) {
                        mDotViewList.get(i).setImageResource(R.drawable.red_dot);
                    } else {
                        mDotViewList.get(i).setImageResource(R.drawable.grey_dot);
                    }
                }

                // 在滑动到最后一个元素时，跳转到第一个元素
//                if (position == originalImages.size() - 1) {
//                    viewPager2.setCurrentItem(1, false);
//                }
//                // 在滑动到第一个元素时，跳转到最后一个元素
//                else if (position == 0) {
//                    viewPager2.setCurrentItem(originalImages.size() - 2, false);
//                }
            }
        });

    }

    public int blendColors(int color1, int color2, float ratio) {
        float inverseRatio = 1f - ratio;

        int r = (int) (Color.red(color1) * ratio + Color.red(color2) * inverseRatio);
        int g = (int) (Color.green(color1) * ratio + Color.green(color2) * inverseRatio);
        int b = (int) (Color.blue(color1) * ratio + Color.blue(color2) * inverseRatio);

        return Color.rgb(r, g, b);
    }

    public void mixColor(ImageView yourImageView, float positionOffset, int color1, int color2){

        // 通过资源ID获取Drawable
        Drawable shapeDrawable = ContextCompat.getDrawable(mContext, R.drawable.red_dot);
        String drawableType = shapeDrawable.getClass().getSimpleName();
        System.out.println("Drawable 类型：" + drawableType);
                //yourImageView.getDrawable();
        // 将Drawable转换为ShapeDrawable
        if (shapeDrawable instanceof ShapeDrawable) {
            ShapeDrawable customShapeDrawable = (ShapeDrawable) shapeDrawable;

            // 然后你可以对customShapeDrawable进行一些定制，比如修改颜色
            int color = blendColors(color1, color2, positionOffset);
            customShapeDrawable.getPaint().setColor(color);
            yourImageView.setBackground(customShapeDrawable);
        } else if (shapeDrawable instanceof GradientDrawable){
            GradientDrawable customShapeDrawable = (GradientDrawable) shapeDrawable;

            // 然后你可以对customShapeDrawable进行一些定制，比如修改颜色
            int color = blendColors(color1, color2, positionOffset);
            customShapeDrawable.setColor(color);
            yourImageView.setBackground(customShapeDrawable);
        }
    }

    /**
     * 启动自动滚动
     */
    public void startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable); // 移除之前的回调,防止多次启动的情况
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL);
        AUTO_SCROLL = true;
    }

    /**
     * 停止自动滚动
     */
    public void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        AUTO_SCROLL = false;
    }

    // 定义自动滚动的 Runnable
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            // 在这里处理自动滚动逻辑
            int currentItem = viewPager2.getCurrentItem();

            //当自动轮播到最后一张图片时，又从头开始轮播
            if (currentItem == originalImages.size() - 2) {
                viewPager2.setCurrentItem(1);
            } else {
                viewPager2.setCurrentItem(currentItem + 1);
            }

            // 重新调度下一次自动滚动
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_INTERVAL);
        }
    };
}

