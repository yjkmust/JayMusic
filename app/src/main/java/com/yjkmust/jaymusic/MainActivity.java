package com.yjkmust.jaymusic;


import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yjkmust.jaymusic.Adapters.MusicAdapter;
import com.yjkmust.jaymusic.Adapters.MyPagerAdapter;
import com.yjkmust.jaymusic.Bean.MusicBean;
import com.yjkmust.jaymusic.Service.MyService;
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

import static android.app.Notification.FLAG_AUTO_CANCEL;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private List<MusicBean> musicList;
    private Context mContext;
    private String TAG = "MainActivity";
    private SeekBar seekbarTime;
    private TextView tvStartTime;
    private BottomSheetDialog dialog;
    private MusicAdapter adapter;
    private boolean isPlaying = false;
    private int musicPosition = 0;
    private Boolean isSeekBarTouch = false;
    private boolean isFirst = true;
    private int musicProgress = 0;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;
    private MyService.MusicControlBinder binder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MyService.MusicControlBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private View viewpager1;
    private View viewpager2;
    private List<View> viewList;
    private MyPagerAdapter myPagerAdapter;
    private ImageView ivPic;
    private Animation animations;
    private LinearInterpolator interpolator;
    private ObjectAnimator anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setOnClick(new OnClick());
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        mContext = this;
        initBroadCastReceiver();
        initView();
        initListener();
        initData();
        initMediaPlayer();
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
        if (musicList.size() > 0) {
            binding.tvTitle.setText(musicList.get(musicPosition).getTitle());
            binding.tvSingler.setText(musicList.get(musicPosition).getArtist());
            binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
        }

    }

    private void initData() {
        musicList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            getLocalMusic();
        }


    }

    private void initView() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        viewpager1 = layoutInflater.inflate(R.layout.item_viewpager_pic, null);
        viewpager2 = layoutInflater.inflate(R.layout.item_viewpager_lyric, null);
        viewList = new ArrayList<>();
        viewList.add(viewpager1);
        viewList.add(viewpager2);
        myPagerAdapter = new MyPagerAdapter(viewList);
        binding.musicViewpager.setAdapter(myPagerAdapter);
        ivPic = (ImageView) viewpager1.findViewById(R.id.iv_viewpager_pic);
        animations = AnimationUtils.loadAnimation(mContext, R.anim.pic_rotate);
        seekbarTime = binding.seekbarTime;
        tvStartTime = binding.startTime;

    }

    private void initListener() {
        seekbarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int currentPosition = binder.getCurrentDuaration();
                tvStartTime.setText(toMinute(currentPosition));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouch = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTouch = false;
                binder.musicSeekTo(seekBar.getProgress());
            }
        });
    }

    public class OnClick {
        public void showMusicList(View v) {
            if (musicList == null) {
                getLocalMusic();
            }
            dialog = new MyBottomSheetDialog(MainActivity.this, R.style.MyBottomSheetDialogStyle);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_music_list_view, null);
            MyRecyclerView musicRecyclerView = (MyRecyclerView) view.findViewById(R.id.music_recyclerview);
            musicRecyclerView.addItemDecoration(new MyItemDecoration());
            musicRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            adapter = new MusicAdapter(musicList);
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                    musicPosition = position;
                    binder.prepareMusic(musicList.get(musicPosition).getPath());
                    binder.startMusic();
                    isPlaying = true;
                    dialog.dismiss();
                    updateSeekbar();
                    startNotification();
                    binding.musicPlayorstop.setImageResource(R.drawable.img_play);
                    binding.tvTitle.setText(musicList.get(position).getTitle());
                    binding.tvSingler.setText(musicList.get(position).getArtist());
                    binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
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
            binder.PreMusic(musicList.get(musicPosition).getPath());
            updateSeekbar();
            binding.musicPlayorstop.setImageResource(R.drawable.img_play);
            binding.tvTitle.setText(musicList.get(musicPosition).getTitle());
            binding.tvSingler.setText(musicList.get(musicPosition).getArtist());
            binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
        }

        public void playOrStopMusic(View view) {
            startNotification();
            if (isFirst) {
                binder.prepareMusic(musicList.get(musicPosition).getPath());
                binder.startMusic();
                startPropertyAnim();
                isFirst = false;
                isPlaying = true;
                updateSeekbar();
                binding.musicPlayorstop.setImageResource(R.drawable.img_play);
            } else {
                if (isPlaying) {
                    isPlaying = false;
                    binder.StopMusic();
                    anim.pause();
                    binding.musicPlayorstop.setImageResource(R.drawable.img_stop);
                } else {
                    isPlaying = true;
                    binder.startMusic();
                    anim.resume();
                    binding.musicPlayorstop.setImageResource(R.drawable.img_play);
                }
            }
        }

        public void nextMusic(View view) {
            if (isFirst) {
                startPropertyAnim();
            }
            if (musicList.size() == musicPosition + 1) {
                musicPosition = 0;
            } else {
                musicPosition++;
            }
            changePic();
            binder.NextMusic(musicList.get(musicPosition).getPath());
            updateSeekbar();
            binding.musicPlayorstop.setImageResource(R.drawable.img_play);
            binding.tvTitle.setText(musicList.get(musicPosition).getTitle());
            binding.tvSingler.setText(musicList.get(musicPosition).getArtist());
            binding.endTime.setText(toMinute(musicList.get(musicPosition).getDuration()));
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
    private void updateSeekbar() {
        seekbarTime.setMax(binder.getDuration());
    }

    /**
     * 更新图片
     */
    private void changePic() {
        ivPic.setImageResource(R.mipmap.jay1);
        anim.cancel();
        anim.start();
    }

    private void initBroadCastReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("YJKMUST.RECEIVER");
        localReceiver = new LocalReceiver();
        registerReceiver(localReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            musicProgress = intent.getIntExtra("time", 0);
            if (isSeekBarTouch) {
                return;
            } else {
                seekbarTime.setProgress(musicProgress);
            }
            if (musicProgress >= seekbarTime.getMax() - 1000) {
                if (musicList.size() == musicPosition + 1) {
                    musicPosition = 0;
                } else {
                    musicPosition++;
                }
                initMediaPlayer();
                if (musicPosition % 2 == 0) {
                    ivPic.setImageResource(R.mipmap.jay1);
                } else {
                    ivPic.setImageResource(R.mipmap.jay);
                }
                startPropertyAnim();
                binder.NextMusic(musicList.get(musicPosition).getPath());
                updateSeekbar();
            }
        }
    }

    private void startPropertyAnim() {
        // 第二个参数"rotation"表明要执行旋转
        // 0f -> 360f，从旋转360度，也可以是负值，负值即为逆时针旋转，正值是顺时针旋转。
        anim = ObjectAnimator.ofFloat(ivPic, "rotation", 0f, 360f);

        // 动画的持续时间，执行多久？
        anim.setDuration(20000);
        anim.setRepeatCount(-1);
        anim.setInterpolator(new LinearInterpolator());

        // 回调监听
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                Log.d("zhangphil", value + "");
            }
        });

        // 正式开始启动执行动画
        anim.start();
    }

    private void startNotification() {
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent intents=new Intent(getApplicationContext(), MainActivity.class);
//        PendingIntent intent=PendingIntent.getActivity(getApplicationContext(), 1, intents, Notification.FLAG_AUTO_CANCEL);
//        Notification notification = new Notification.Builder(this)
////                .setContent(new RemoteViews(getPackageName(),R.layout.item_notification))
//                 .setContentIntent(intent)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("888")
//                .setContentText("66")
//                .setTicker("6")
//                .setWhen(System.currentTimeMillis())
//                .build();
//        manager.notify(1,notification);
//        NotificationManager mNotifyMgr =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        PendingIntent contentIntent = PendingIntent.getActivity(
//                this, 0, new Intent(this, MainActivity.class),0);
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle("My notification")
//                        .setContentText("Hello World!")
//                        .setContentIntent(contentIntent);
//        Notification notification = mBuilder.build();
//        notification.defaults = Notification.DEFAULT_ALL;
//        mNotifyMgr.notify(1,notification);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        NotificationManager notifyManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(mainPendingIntent)
                //设置通知时间，默认为系统发出通知的时间，通常不用设置
                .setWhen(System.currentTimeMillis());
        Notification notification = builder.build();
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.item_notification);
        if(Build.VERSION.SDK_INT >= 16){
            notification.bigContentView = remoteViews;
        }
        //通过builder.build()方法生成Notification对象,并发送通知,id=1
        notifyManager.notify(1, notification);

    }
}
