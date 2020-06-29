package co.herxun.impp.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONObject;

import com.arrownock.social.IAnSocialCallback;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.view.SettingView;

public class UserInfoSetting extends BaseActivity {

    private TextView takePhoto, choosePhoto;
    private SettingView settingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        init();
        initData();
    }

    private void init() {
        takePhoto = (TextView) findViewById(R.id.take_photo);
        choosePhoto = (TextView) findViewById(R.id.choose_photo);
        settingView = new SettingView(this);
    }

    private void initData() {
        takePhoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    Uri mImageCaptureUri = null;
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        mImageCaptureUri = mImageCaptureUri = Uri.fromFile(getFileTemp());
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, Constant.REQUESTCODE_PHOTO_TAKE);
                } catch (ActivityNotFoundException e) {
                    Log.d("", "cannot take picture", e);
                }
            }
        });

        choosePhoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = new Intent();
                // intent.setType("image/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(Intent.createChooser(intent, ""),
                // Constant.REQUESTCODE_PHOTO_PICK);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Constant.REQUESTCODE_PHOTO_PICK);
            }
        });
    }

    public File getFileTemp() {
        final String TEMP_PHOTO_FILE_NAME = getString(R.string.app_name) + "temp_photo.png";
        File mFileTemp;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }
        return mFileTemp;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUESTCODE_PHOTO_PICK) {
            String imageFilePath = ImageUtility.getFilePathFromGallery(this, data);
            updatePhoto(imageFilePath);

        } else if (requestCode == Constant.REQUESTCODE_PHOTO_TAKE) {
            String imageFilePath = ImageUtility.getFileTemp(this).getPath();
            updatePhoto(imageFilePath);
        }
    }

    private void updatePhoto(String filePath) {
        byte[] fileToUpload;
        File file = new File(filePath);
        int size = (int) file.length();
        fileToUpload = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(fileToUpload, 0, fileToUpload.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UserManager.getInstance(this).updateMyPhoto(fileToUpload, new IAnSocialCallback() {
            @Override
            public void onFailure(JSONObject arg0) {

            }

            @Override
            public void onSuccess(JSONObject arg0) {
                settingView.setUserInfo(UserManager.getInstance(UserInfoSetting.this).getCurrentUser());
            }
        });
    }

}
