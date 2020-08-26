package com.studioios.linhlee.icamera.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.studioios.linhlee.icamera.R;
import com.studioios.linhlee.icamera.adapters.MyPagerAdapter;
import com.studioios.linhlee.icamera.fragments.PhotoFragment;
import com.studioios.linhlee.icamera.utils.Constant;

import java.util.ArrayList;

/**
 * Created by lequy on 12/23/2016.
 */

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<String> listImagePath;
    private ArrayList<Fragment> listFragment;
    private MyPagerAdapter adapter;
    private ViewPager pager;
    private LinearLayout topLayout;
    private LinearLayout bottomLayout;
    private TextView quantityText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        pager = (ViewPager) findViewById(R.id.pager);
        topLayout = (LinearLayout) findViewById(R.id.top_layout);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        quantityText = (TextView) findViewById(R.id.quantity_text);

        listImagePath = new ArrayList<>();
        listImagePath.addAll(Constant.getListGPUPath());

        quantityText.setText("1/" + listImagePath.size());

        listFragment = new ArrayList<>();
        for (int i = 0; i < listImagePath.size(); i++) {
            PhotoFragment photoFragment = new PhotoFragment();
            Bundle extras = new Bundle();
            extras.putString("img_path", listImagePath.get(i));
            photoFragment.setArguments(extras);
            listFragment.add(photoFragment);
        }

        adapter = new MyPagerAdapter(getSupportFragmentManager(), listFragment);
        pager.setAdapter(adapter);

        quantityText.setOnClickListener(this);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                quantityText.setText((position + 1) + "/" + listImagePath.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constant.hideNavigationBar(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Constant.hideNavigationBar(this);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
