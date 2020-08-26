package com.studioios.linhlee.icamera.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.studioios.linhlee.icamera.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by lequy on 12/23/2016.
 */

public class PhotoFragment extends Fragment implements View.OnClickListener {
    private ImageView mainImage;
    private View shadowView;
    private ImageView playButton;
    private Bitmap myBitmap;
    private String imagePath;
    private String lastString;
    private int orientation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePath = getArguments().getString("img_path");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        lastString = imagePath.substring(imagePath.length() - 4);

        mainImage = (ImageView) rootView.findViewById(R.id.main_image);
        shadowView = rootView.findViewById(R.id.shadow_view);
        playButton = (ImageView) rootView.findViewById(R.id.play_button);

        if (lastString.equals(".mp4")) {
            playButton.setVisibility(View.VISIBLE);
            shadowView.setVisibility(View.VISIBLE);
            playButton.setOnClickListener(this);
        } else {
            playButton.setVisibility(View.GONE);
            shadowView.setVisibility(View.GONE);
            playButton.setOnClickListener(null);
        }

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    if (lastString.equals(".mp4")) {
                        myBitmap = ThumbnailUtils.createVideoThumbnail(imagePath,
                                MediaStore.Images.Thumbnails.MINI_KIND);
                    } else {
                        try {
                            ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
                            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Matrix matrix = new Matrix();
                        if (orientation == 6) {
                            matrix.postRotate(90);
                        }
                        else if (orientation == 3) {
                            matrix.postRotate(180);
                        }
                        else if (orientation == 8) {
                            matrix.postRotate(270);
                        }

                        myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        int nh = (int) ( myBitmap.getHeight() * (512.0 / myBitmap.getWidth()) );
                        myBitmap = Bitmap.createScaledBitmap(myBitmap, 512, nh, true);
                        myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mainImage.setImageBitmap(myBitmap);
            }
        }.execute();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        Uri uri = Uri.parse(imagePath);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/mp4");
        startActivity(intent);
    }
}
