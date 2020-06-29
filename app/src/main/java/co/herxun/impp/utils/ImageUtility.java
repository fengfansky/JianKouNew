package co.herxun.impp.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import co.herxun.impp.R;

public class ImageUtility {
    public static Bitmap getRoundedTopCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        final Rect topRightRect = new Rect(bitmap.getWidth() / 2, 0, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        final Rect bottomRect = new Rect(0, bitmap.getHeight() / 2, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        // Fill in upper right corner
        canvas.drawRect(topRightRect, paint);
        // Fill in bottom corners
        canvas.drawRect(bottomRect, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);
        final float roundPx = size;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        // TODO Auto-generated method stub
        if (scaleBitmapImage.getWidth() >= scaleBitmapImage.getHeight()) {
            scaleBitmapImage = Bitmap.createBitmap(scaleBitmapImage,
                    scaleBitmapImage.getWidth() / 2 - scaleBitmapImage.getHeight() / 2, 0,
                    scaleBitmapImage.getHeight(), scaleBitmapImage.getHeight());
        } else {
            scaleBitmapImage = Bitmap.createBitmap(scaleBitmapImage, 0, scaleBitmapImage.getHeight() / 2
                    - scaleBitmapImage.getWidth() / 2, scaleBitmapImage.getWidth(), scaleBitmapImage.getWidth());
        }

        int size = Math.min(scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight());
        Bitmap targetBitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) size - 1) / 2, ((float) size - 1) / 2, (Math.min(((float) size), ((float) size)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0,
                0, size, size), null);
        return targetBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Context ct, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(ct.getResources(), resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(ct.getResources(), resId, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Context ct, int resId, int sampleSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(ct.getResources(), resId, options);

        // Calculate inSampleSize
        options.inSampleSize = sampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(ct.getResources(), resId, options);
    }

    public static File getFileTemp(Context ct) {
        final String TEMP_PHOTO_FILE_NAME = ct.getString(R.string.app_name) + "temp_photo.png";
        File mFileTemp;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(ct.getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }
        return mFileTemp;
    }

    public static void deleteTempFile(Context ct) {
        File mFileTemp = getFileTemp(ct);
        mFileTemp.delete();
    }

    public static Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    // 根据Uri提取图片路径
    public static String getFilePathFromGallery(Context context, Intent data) {
        Uri uri = data.getData();
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // Android版本在KITKAT之上
        if (isKitKat) {
            // 新方式提取
            if (DocumentsContract.isDocumentUri(context, uri)) {

                // 图片在扩展卡
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // 图片在下载路径里
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // 图片在相册路径里
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] { split[1] };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else {
                if ("content".equalsIgnoreCase(uri.getScheme())) {
                    // 图片在google driver中
                    if (isGooglePhotosUri(uri)) {
                        return uri.getLastPathSegment();
                    }
                    // 图片在相册路径里
                    else {
                        return getDataColumn(context, uri, null, null);
                    }
                }
                // 图片在文件路径里
                else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    return uri.getPath();
                } else {
                    // 图片在相册里
                    Cursor cursor = null;
                    String picturePath = null;
                    try {
                        if (uri != null) {
                            String uriStr = uri.toString();
                            String path = uriStr.substring(10, uriStr.length());
                            if (path.startsWith("com.sec.android.gallery3d")) {
                                Log.e("selectImage", "It's auto backup pic path:" + uri.toString());
                                return null;
                            }
                        }
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);
                    } catch (Exception e) {
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    // 直接获取Uri的path
                    if (picturePath == null && uri != null) {
                        return uri.getPath();
                    } else {
                        return picturePath;
                    }
                }
            }
        } else {
            // Android版本在KITKAT之下
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                // 图片在google driver中
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }
                // 图片在相册路径里
                else {
                    return getDataColumn(context, uri, null, null);
                }
            }
            // 图片在文件路径里
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            } else {
                // 图片在相册里
                Cursor cursor = null;
                String picturePath = null;
                try {
                    if (uri != null) {
                        String uriStr = uri.toString();
                        String path = uriStr.substring(10, uriStr.length());
                        if (path.startsWith("com.sec.android.gallery3d")) {
                            Log.e("selectImage", "It's auto backup pic path:" + uri.toString());
                            return null;
                        }
                    }
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                } catch (Exception e) {
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                // 直接获取Uri的path
                if (picturePath == null && uri != null) {
                    return uri.getPath();
                } else {
                    return picturePath;
                }
            }
        }
        return null;
    }

    public static byte[] getDataFromFilePath(String filePath) {
        Bitmap bmResized = null;
        File file = new File(filePath);
        int size = (int) file.length();
        byte[] imgData = new byte[size];
        try {
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(imgData, 0, imgData.length);
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(filePath);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 256;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(filePath);
            bmResized = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        // Bitmap original = BitmapFactory.decodeByteArray(imgData, 0,
        // imgData.length);
        // original.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        if (bmResized == null) {
            return null;
        }
        bmResized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        imgData = blob.toByteArray();

        return imgData;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}