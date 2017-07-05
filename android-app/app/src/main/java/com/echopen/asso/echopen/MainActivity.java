package com.echopen.asso.echopen;

import android.annotation.TargetApi;
import android.content.Context;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.echopen.asso.echopen.echography_image_streaming.EchographyImageStreamingService;
import com.echopen.asso.echopen.echography_image_streaming.modes.EchographyImageStreamingTCPMode;
import com.echopen.asso.echopen.echography_image_visualisation.EchographyImageVisualisationContract;
import com.echopen.asso.echopen.echography_image_visualisation.EchographyImageVisualisationPresenter;
import com.echopen.asso.echopen.utils.Constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * MainActivity class handles the main screen of the app.
 */

public class MainActivity extends AppCompatActivity implements EchographyImageVisualisationContract.View {

    private ImageHandler ImageHandler;
    FragmentManager mFragmentManager;
    //private RenderingContextController mRenderingContextController;
    private EchographyImageStreamingService mEchographyImageStreamingService;
    private EchographyImageVisualisationContract.Presenter mEchographyImageVisualisationPresenter;

    /**
     * This method calls all the UI methods and then gives hand to  UDPToBitmapDisplayer class.
     * UDPToBitmapDisplayer listens to UDP data, processes them with the help of ScanConversion,
     * and then displays them.
     * Also, this method uses the Config singleton class that provides device-specific constants
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setEchoImage();

        // create file handler to save images
        ImageHandler = new ImageHandler(getFilesDir());

        mFragmentManager = getSupportFragmentManager();
        SplashFragment splashFragment = new SplashFragment();
        mFragmentManager.beginTransaction().add(R.id.main, splashFragment).commit();
    }

    public void setEchoImage() {
        mEchographyImageStreamingService = ((EchOpenApplication) this.getApplication()).getEchographyImageStreamingService();
        //mRenderingContextController = mEchographyImageStreamingService.getRenderingContextController();

        mEchographyImageVisualisationPresenter = new EchographyImageVisualisationPresenter(mEchographyImageStreamingService, this);
        this.setPresenter(mEchographyImageVisualisationPresenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEchographyImageVisualisationPresenter.start();
        EchographyImageStreamingTCPMode lTCPMode = new EchographyImageStreamingTCPMode(Constants.Http.REDPITAYA_IP, Constants.Http.REDPITAYA_PORT);
        mEchographyImageStreamingService.connect(lTCPMode, this);
    }

    /**
     * Following the doc https://developer.android.com/intl/ko/training/basics/intents/result.html,
     * onActivityResult is “Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.”,
     * See more here : https://stackoverflow.com/questions/20114485/use-onactivityresult-android
     *
     * @param requestCode, integer argument that identifies your request
     * @param resultCode,  to get its values, check RESULT_CANCELED, RESULT_OK here https://developer.android.com/reference/android/app/Activity.html#RESULT_OK
     * @param data,        Intent instance
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void refreshImage(final Bitmap iBitmap) {
        try {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doFinish(iBitmap);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPresenter(EchographyImageVisualisationContract.Presenter presenter) {
        mEchographyImageVisualisationPresenter = presenter;
    }

    public void doFinish(Bitmap img) {
        changeFragment(img);
    }

    public void changeFragment(Bitmap img) {
        HomeFragment homeFragment = new HomeFragment(img);
        mFragmentManager.beginTransaction().replace(R.id.main, homeFragment).addToBackStack(homeFragment.getClass().getName()).commit();
    }

    public void switchActivity() {
        Intent intent = new Intent(this, ListImagesActivity.class);
        startActivity(intent);
    }

    public ImageHandler getImageHandler() {
        return this.ImageHandler;
    }
}