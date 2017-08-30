package com.yjkmust.jaymusic.Widgets;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yjkmust.jaymusic.R;

/**
 * Created by GEOFLY on 2017/8/30.
 */

public class MyItemDecoration extends RecyclerView.ItemDecoration {
    private Paint mPaint;

    @SuppressLint("ResourceAsColor")
    public MyItemDecoration() {
        //初始化画笔
        mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        //颜色设置为黑色
        mPaint.setColor(R.color.colorGreen);
    }

    /**
     * 基本操作是留出分割线位置
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State
            state) {
        //获取条目
        int position = parent.getChildAdapterPosition(view);
        //如果不是第一个条目，每个条目上方预留10px位置用来绘制分割线
        if (position != 0) {
            outRect.top = 1;
        }
    }

    /**
     * 绘制分割线
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        Rect mRect = new Rect();
        //设置矩形左边位置
        mRect.left = parent.getPaddingLeft()+10;
        //设置矩形右边位置
        mRect.right = parent.getWidth() - parent.getPaddingRight()-10;
        int chilCount = parent.getChildCount();
        for (int i = 1; i < chilCount; i++) {
            //每个分割线的底部位置都是上一个条目的头部
            mRect.bottom = parent.getChildAt(i).getTop();
            //每个分割线的头部位置都是底部位置-10px
            mRect.top = mRect.bottom - 1;
            canvas.drawRect(mRect, mPaint);
        }
    }
}
