package com.yjkmust.jaymusic.Adapters;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.yjkmust.jaymusic.Bean.MusicBean;
import com.yjkmust.jaymusic.R;

import java.util.List;

/**
 * Created by GEOFLY on 2017/8/29.
 */

public class MusicAdapter extends BaseQuickAdapter<MusicBean,BaseViewHolder> {

    public MusicAdapter(@Nullable List<MusicBean> data) {
        super(R.layout.item_music,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicBean musicBean) {
        helper.setText(R.id.tv_musicname, musicBean.getTitle());
        helper.setText(R.id.tv_musicsinger, musicBean.getArtist());
    }
}
