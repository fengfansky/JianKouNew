package co.herxun.impp.fragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.activity.LoginActivity;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.view.SettingView;

import com.arrownock.social.IAnSocialCallback;

public class SettingFragment extends BaseFragment {
    private SettingView mUserDetailView;
    private Dialog mActionDialog;

    public SettingFragment() {
        this("");
    }

    public SettingFragment(String title) {
        super(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        return rootView;
    }

    public void onViewCreated(View fragView, Bundle savedInstanceState) {
        super.onViewCreated(fragView, savedInstanceState);
        final Context ct = fragView.getContext();
        setTitle(getActivity().getString(R.string.tab_title_setting));

        mUserDetailView = (SettingView) fragView.findViewById(R.id.setting_view);
        mUserDetailView.setButton(fragView.getContext().getString(R.string.else_sign_out), new OnClickListener() {

            @Override
            public void onClick(final View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.setting_message)
                        .setPositiveButton(R.string.setting_ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserManager.getInstance(v.getContext()).logout();
                                getActivity().startActivity(new Intent(ct, LoginActivity.class));
                                getActivity().finish();
                            }
                        }).setNegativeButton(R.string.setting_cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();

            }
        });
        mUserDetailView.getLinearLayout().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.show();
            }
        });

        mUserDetailView.setNickname(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                final View view = inflater.inflate(R.layout.setting_custom_dialog, null);
                final EditText getNickNameText1 = (EditText) view.findViewById(R.id.setting_custom_dialog);
                String oldNickname = UserManager.getInstance(getActivity()).getCurrentUser().nickname;

                if (oldNickname != null) {
                    getNickNameText1.setText(oldNickname);
                    getNickNameText1.setSelection(oldNickname.length());
                }

                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(view)
                        .setPositiveButton(R.string.general_ok, null).setNegativeButton(R.string.general_cancel, null)
                        .create();

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String text = null;
                        if (getNickNameText1.getText() != null) {
                            text = getNickNameText1.getText().toString();
                        }
                        if (TextUtils.isEmpty(text) || text.trim().isEmpty()) {
                            Toast.makeText(getActivity(), R.string.setting_dialog_notnull, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        UserManager.getInstance(getActivity()).updateNickName(
                                UserManager.getInstance(getActivity()).getCurrentUser().userName, text,
                                new IAnSocialCallback() {
                                    @Override
                                    public void onSuccess(JSONObject arg0) {
                                        try {
                                            JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                                            final User user = new User(userJson);
                                            user.update();

                                            dialog.dismiss();

                                            getActivity().runOnUiThread(new Runnable() {
                                                public void run() {
                                                    UserManager.getInstance(getActivity()).setCurrentUser(user);
                                                    mUserDetailView.setUserInfo(user);
                                                }
                                            });

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(JSONObject arg0) {

                                    }
                                });
                    }
                });

            }
        });

        AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(ct);
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_friend_alert, null);
        ImageView imgTakePic = (ImageView) view.findViewById(R.id.dialog_img1);
        imgTakePic.setImageResource(R.drawable.dialog_camera);
        ImageView imgPickPic = (ImageView) view.findViewById(R.id.dialog_img2);
        imgPickPic.setImageResource(R.drawable.dialog_upload);
        TextView textTakePic = (TextView) view.findViewById(R.id.dialog_text1);
        textTakePic.setText(R.string.chat_take_picture);
        TextView textPickPic = (TextView) view.findViewById(R.id.dialog_text2);
        textPickPic.setText(R.string.chat_pick_picture);
        view.findViewById(R.id.action_dialog_friend_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    Uri mImageCaptureUri = null;
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        mImageCaptureUri = mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(getActivity()));
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, Constant.REQUESTCODE_PHOTO_TAKE);
                } catch (ActivityNotFoundException e) {
                    Log.d("", "cannot take picture", e);
                }
            }
        });
        view.findViewById(R.id.action_dialog_topic_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();

                // Intent intent = new Intent();
                // intent.setType("image/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(Intent.createChooser(intent, ""),
                // Constant.REQUESTCODE_PHOTO_PICK);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Constant.REQUESTCODE_PHOTO_PICK);
            }
        });
        dialogBuiler.setView(view);
        mActionDialog = dialogBuiler.create();
    }

    @Override
    public void onViewShown() {
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mUserDetailView.setUserInfo(UserManager.getInstance(getActivity()).getCurrentUser());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Context ct = getActivity();

        if (requestCode == Constant.REQUESTCODE_PHOTO_PICK) {
            String imageFilePath = ImageUtility.getFilePathFromGallery(ct, data);
            updatePhoto(imageFilePath);

        } else if (requestCode == Constant.REQUESTCODE_PHOTO_TAKE) {
            String imageFilePath = ImageUtility.getFileTemp(ct).getPath();
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

        UserManager.getInstance(getActivity()).updateMyPhoto(fileToUpload, new IAnSocialCallback() {
            @Override
            public void onFailure(JSONObject arg0) {

            }

            @Override
            public void onSuccess(JSONObject arg0) {
                mUserDetailView.setUserInfo(UserManager.getInstance(getActivity()).getCurrentUser());
            }
        });
    }

}
