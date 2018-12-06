package cn.edu.sdu.online.isdu.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import com.yalantis.ucrop.UCrop;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import cn.edu.sdu.online.isdu.GlideApp;
import cn.edu.sdu.online.isdu.GlideRequest;
import cn.edu.sdu.online.isdu.app.MyApplication;

import static cn.edu.sdu.online.isdu.util.FileUtil.getStringFromFile;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/3
 *
 * 相册和摄像头选取图片管理器
 ****************************************************
 */

public class ImageManager {

    public static final int TAKE_PHOTO = 0;
    public static final int OPEN_GALLERY = 1;

    private Uri imageUri;
    public String imagePath;
    public File croppedImage;

    private Uri fromUri, destUri; // 用于裁剪的URI

    public void captureByCamera(Activity activity) {
        initCrop(activity);
        // 构建图片缓存文件
        File thumb = new File(Environment.getExternalStorageDirectory() + "/iSDU/thumb/" +
            System.currentTimeMillis() + ".jpg");
        if (!thumb.exists()) {
            if (!thumb.getParentFile().exists()) thumb.getParentFile().mkdirs();
        } else thumb.delete();
        try {
            thumb.createNewFile();
        } catch (IOException e) {
            Logger.log(e);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            fromUri = FileProvider.getUriForFile(activity,
                    "cn.edu.sdu.online.isdu.fileprovider", thumb);
        } else {
            fromUri = Uri.fromFile(thumb);
        }

        imagePath = thumb.getAbsolutePath();

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fromUri);
        activity.startActivityForResult(intent, TAKE_PHOTO);
    }

    public void selectFromGallery(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openGallery(activity);
        }
    }

    private void openGallery(Activity activity) {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        activity.startActivityForResult(intent, OPEN_GALLERY);
    }

    public Uri getImageUri() {
        return imageUri;
    }

    private void initCrop(Context context) {
        //获取打开文件的URI
        croppedImage = new File(Environment.getExternalStorageDirectory() + "/iSDU/thumb/" +
                System.currentTimeMillis() + "_c.jpg");

        if (!croppedImage.exists()) {
            if (!croppedImage.getParentFile().exists()) croppedImage.getParentFile().mkdirs();
            try {
                croppedImage.createNewFile();
            } catch (IOException e) {
                Logger.log(e);
            }
        }

        if (Build.VERSION.SDK_INT >= 24) {
            destUri = FileProvider.getUriForFile(context,
                    "cn.edu.sdu.online.isdu.fileprovider", croppedImage);
        } else {
            destUri = Uri.fromFile(croppedImage);
        }
    }

    public void handleImage(Activity context, Intent data) {
        initCrop(context);

        handleImage(data, context);

        //获取打开文件的URI
        if (Build.VERSION.SDK_INT >= 24) {
            fromUri = FileProvider.getUriForFile(context,
                    "cn.edu.sdu.online.isdu.fileprovider", new File(imagePath));
        } else {
            fromUri = Uri.fromFile(new File(imagePath));
        }
        openCrop(context);
    }

    public void handleImage(Intent data, Activity context) {
        if (Build.VERSION.SDK_INT >= 19) {
            imagePath = null;
            Uri uri = data.getData();
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, context);
                } else if ("com.android.proiders.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null, context);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                imagePath = getImagePath(uri, null, context);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
            }
        } else {
            Uri uri = data.getData();
            imagePath = getImagePath(uri, null, context);
        }

    }

    public String getImagePath(Uri uri, String selection, Context context) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri,
                null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public void openCrop(Activity activity) {
        UCrop.of(fromUri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(800, 800)
                .start(activity);
    }

    public Uri getFromUri() {
        return fromUri;
    }

    public void setFromUri(Uri fromUri) {
        this.fromUri = fromUri;
    }

    public Uri getDestUri() {
        return destUri;
    }

    public void setDestUri(Uri destUri) {
        this.destUri = destUri;
    }

    /**
     * 图片转成string
     *
     * @param bitmap The bitmap to be converted
     * @return The string which is converted
     */
    public static String convertBitmapToString(Bitmap bitmap) {
        return convertBitmapToString(bitmap, 100);
    }

    /**
     * 图片转成string
     *
     * @param bitmap The bitmap to be converted
     * @param quality The quality to be compressed
     * @return The string which is converted
     */
    public static String convertBitmapToString(Bitmap bitmap, int quality) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        return baos.toByteArray();
    }

    public static byte[] convertGifToByteArray(String filePath) {
        try {
            byte[] b = new byte[1024];
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            while ((len = bis.read(b)) > 0) {
                baos.write(b, 0, len);
            }

            bis.close();
            fis.close();

            return baos.toByteArray();
        } catch (Exception e) {
            Logger.log(e);
        }
        return new byte[1];
    }

    /**
     * string转成bitmap
     *
     * @param st The string to convert to Bitmap
     */
    public static Bitmap convertStringToBitmap(String st) {
        // OutputStream out;
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * 压缩图片
     *
     * @param bitmap The bitmap to be compressed
     * @return The compressed bitmap
     */
    public static Bitmap compressBitmap(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    public static int getBitmapSize(Bitmap bitmap) {
        return bitmap.getAllocationByteCount();
    }

    public static Bitmap loadStringFromFile(String filePath) {
        return convertStringToBitmap(getStringFromFile(filePath));
    }

    public static boolean isGif(byte[] imgByte) {
        return (imgByte.length >= 3) && imgByte[0] == (byte) 'G' &&
                imgByte[1] == (byte) 'I' && imgByte[2] == (byte) 'F';
    }

    public static boolean isGif(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] buf = new byte[3];
            fis.read(buf);
            fis.close();
            return isGif(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isGif(String filePath) {
        return isGif(new File(filePath));
    }

}
