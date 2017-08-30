package com.yjkmust.jaymusic;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toolbar;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yjkmust.jaymusic.Adapters.MusicAdapter;
import com.yjkmust.jaymusic.Bean.MusicBean;
import com.yjkmust.jaymusic.Widgets.MyBottomSheetDialog;
import com.yjkmust.jaymusic.Widgets.MyItemDecoration;
import com.yjkmust.jaymusic.Widgets.MyRecyclerView;
import com.yjkmust.jaymusic.databinding.ActivityMainBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private int musicPosition = 0;
    private Timer timer;
    private TimerTask timerTask;
    private Boolean isSeekBarTouch = false;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setOnClick(new OnClick());
        mContext = this;
        mediaPlayer = new MediaPlayer();
        initView();
        initListener();
        initData();
        initMediaPlayer();
//        sendProgress();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "拒绝权限无法使用！", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void initMediaPlayer() {
        binding.tvTitle.setText(musicList.get(musicPosition).getTitle());
        binding.tvSingler.setText(musicList.get(musicPosition).getArtist());
        binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
        try {
            mediaPlayer.setDataSource(musicList.get(musicPosition).getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initData() {
        musicList = new ArrayList<>();
        getLocalMusic();

    }

    private void initView() {
        seekbarTime = binding.seekbarTime;
        tvStartTime = binding.startTime;

    }

    private void initListener() {
        seekbarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                tvStartTime.setText(toMinute(currentPosition));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouch = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouch = false;
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });
    }

    public class OnClick {

        public void showMusicList(View v) {
            dialog = new MyBottomSheetDialog(MainActivity.this, R.style.MyBottomSheetDialogStyle);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_music_list_view, null);
            MyRecyclerView musicRecyclerView = (MyRecyclerView) view.findViewById(R.id.music_recyclerview);
            musicRecyclerView.addItemDecoration(new MyItemDecoration());
            musicRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            adapter = new MusicAdapter(musicList);
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                    try {
                        if (isPlaying) {
                            mediaPlayer.reset();
                        }
                        musicPosition = position;
                        mediaPlayer.setDataSource(musicList.get(musicPosition).getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        isPlaying = true;
                        dialog.dismiss();
                        updateSeekbar();
                        binding.musicPlayorstop.setImageResource(R.drawable.img_play);
                        binding.tvTitle.setText(musicList.get(position).getTitle());
                        binding.tvSingler.setText(musicList.get(position).getArtist());
                        binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, "Item" + position, Toast.LENGTH_SHORT).show();
                }
            });
            adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                    switch (view.getId()) {
                        case R.id.iv_star:
                            Toast.makeText(MainActivity.this, "ItemClickone" + position, Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.iv_delete:
                            Toast.makeText(MainActivity.this, "ItemClicktwo" + position, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            musicRecyclerView.setAdapter(adapter);
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        }

        public void preMusic(View view) {
            if (musicPosition == 0) {
                musicPosition = musicList.size() - 1;
            } else {
                musicPosition--;
            }
            try {
                if (isPlaying) {
                    mediaPlayer.reset();
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicList.get(musicPosition).getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                updateSeekbar();
                binding.tvTitle.setText(musicList.get(musicPosition).getTitle());
                binding.tvSingler.setText(musicList.get(musicPosition).getArtist());
                binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void playOrStopMusic(View view) {
            if (isFirst){
                mediaPlayer.start();
                isFirst = false;
                isPlaying = true;
                updateSeekbar();
                binding.musicPlayorstop.setImageResource(R.drawable.img_play);
            }else {
                if (isPlaying) {
                    isPlaying = false;
                    mediaPlayer.pause();
                    binding.musicPlayorstop.setImageResource(R.drawable.img_stop);
                } else {
                    isPlaying = true;
                    mediaPlayer.start();
                    binding.musicPlayorstop.setImageResource(R.drawable.img_play);
                }
            }

        }

        public void nextMusic(View view) {
            if (musicList.size() == musicPosition + 1) {
                musicPosition = 0;
            } else {
                musicPosition++;
            }
            try {
                if (isPlaying) {
                    mediaPlayer.reset();
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicList.get(musicPosition).getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                updateSeekbar();
                binding.tvTitle.setText(musicList.get(musicPosition).getTitle());
                binding.tvSingler.setText(musicList.get(musicPosition).getArtist());
                binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void getLocalMusic() {
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
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
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
    /**
     * 毫秒转成分
     */
    private String toMinute(long ms) {
        Date date = new Date(ms);
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(date);
    }

    /**
     * 播放更新进度条
     */
    private void updateSeekbar(){
        seekbarTime.setMax(mediaPlayer.getDuration());
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isSeekBarTouch) {
                    return;
                }
                seekbarTime.setProgress(mediaPlayer.getCurrentPosition());

            }
        };
        timer.schedule(timerTask,0,10);
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
