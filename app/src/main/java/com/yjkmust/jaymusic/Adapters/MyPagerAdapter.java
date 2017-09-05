package com.yjkmust.jaymusic.Adapters;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.yjkmust.jaymusic.Service.MyService;

import java.util.List;

/**
 * Created by 11432 on 2017/9/1.
 */

public class MyPagerAdapter extends PagerAdapter {
    private List<View> list;
    public MyPagerAdapter(List<View> list){
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position));
        return list.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }
}
