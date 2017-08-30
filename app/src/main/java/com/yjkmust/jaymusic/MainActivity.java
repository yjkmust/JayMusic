package com.yjkmust.jaymusic;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yjkmust.jaymusic.Adapters.MusicAdapter;
import com.yjkmust.jaymusic.Bean.MusicBean;
import com.yjkmust.jaymusic.Widgets.MyBottomSheetDialog;
import com.yjkmust.jaymusic.Widgets.MyItemDecoration;
import com.yjkmust.jaymusic.Widgets.MyRecyclerView;
import com.yjkmust.jaymusic.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private List<MusicBean> musicList;
    private Context mContext;
    private String TAG = "MainActivity";
    private SeekBar seekbarTime;
    private TextView tvStartTime;
    private BottomSheetDialog dialog;
    private View view;
    private MyRecyclerView musicRecyclerView;
    private MusicAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setOnClick(new OnClick());
        mContext = this;
        initView();
        initListener();
        initData();
        for (MusicBean bean : musicList){
            Log.d(TAG, "onCreate: "+bean.toString());
        }

    }
    private void initData(){
        musicList = new ArrayList<>();
        getLocalMusic();

    }
    private void initView(){
        seekbarTime = binding.seekbarTime;
        tvStartTime = binding.startTime;
        initBottomDialog();
    }
    private void initBottomDialog(){


    }
    private void initListener(){
        seekbarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               tvStartTime.setText(progress+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    public class OnClick{

        public void showMusicList(View v){
            dialog = new MyBottomSheetDialog(MainActivity.this,R.style.MyBottomSheetDialogStyle);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_music_list_view, null);
            MyRecyclerView musicRecyclerView = (MyRecyclerView) view.findViewById(R.id.music_recyclerview);
            musicRecyclerView.addItemDecoration(new MyItemDecoration());
            musicRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            adapter = new MusicAdapter(musicList);
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                    Toast.makeText(MainActivity.this,"Item"+position,Toast.LENGTH_SHORT).show();
                }
            });
            adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                    switch (view.getId()){
                        case R.id.iv_star:
                            Toast.makeText(MainActivity.this,"ItemClickone"+position,Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.iv_delete:
                            Toast.makeText(MainActivity.this,"ItemClicktwo"+position,Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            musicRecyclerView.setAdapter(adapter);
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();


        }
    }
    private void getLocalMusic(){
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                        BaseColumns._ID,
                        MediaStore.Audio.AudioColumns.IS_MUSIC,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.ARTIST,
                        MediaStore.Audio.AudioColumns.ALBUM,
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        MediaStore.Audio.AudioColumns.DATA,
                        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                        MediaStore.Audio.AudioColumns.SIZE,
                        MediaStore.Audio.AudioColumns.DURATION
                }, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null){
            return;
        }
        while (cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            // 标题
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            // 艺术家
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
            // 专辑
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            // 专辑封面id，根据该id可以获得专辑封面图片
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
            // 持续时间
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            // 音乐文件路径
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
            // 音乐文件名
            String fileName = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)));
            // 音乐文件大小
            long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            MusicBean bean = new MusicBean();
            bean.setId(id);
            bean.setTitle(title);
            bean.setArtist(artist);
            bean.setAlbum(album);
            bean.setAlbumId(albumId);
            bean.setDuration(duration);
            bean.setPath(path);
            bean.setFileName(fileName);
            bean.setFileSize(fileSize);
            musicList.add(bean);
        }
    }

}
