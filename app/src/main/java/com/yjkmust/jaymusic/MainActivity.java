package com.yjkmust.jaymusic;


import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;


import com.yjkmust.jaymusic.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setOnClick(new OnClick());

    }
    public class OnClick{
        public void showMusicList(View v){
            BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_music_list_view, null);
            dialog.setContentView(view);
            dialog.show();
        }
    }
}
