package com.example.uploaddocuments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static int ALBUM_CODE1 = 300;
    public static int ALBUM_CODE2 = 400;
    public static int CAMERA_CODE1 = 100;
    public static int CAMERA_CODE2 = 200;
    /**
     * 营业执照上传
     */
    private TextView mBusiness;
    /**
     * 营业执照号：123123123123123
     */
    private TextView mEtSerial;
    private FrameLayout mFlBusinessLicense;
    /**
     * 许可证上传
     */
    private TextView mLicenseUpload;
    /**
     * 许可证号：13123123123123123123
     */
    private TextView mLicense;
    private ImageView mIvOpenPhotos;
    private ImageView mPermitPhoto;
    private Intent intent;
    private final int PICK = 1;
    private File mFile;
    private Uri mImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePick();
        creatFile();
        initView();
    }
    private void takePick() {//相册Sd卡权限

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,Manifest.permission.READ_EXTERNAL_STORAGE
                ,Manifest.permission.CAMERA}, 200);

    }
    private void creatFile() {
        // 2.创建空白文件
        mFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        if (!mFile.exists()){
            try {
                mFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //3.将File文件转换为Uri路径
        //适配7.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mImageUri = Uri.fromFile(mFile);
        } else {
            //第二个参数要和清单文件中的配置保持一致
            mImageUri = FileProvider.getUriForFile(this,
                    "com.example.uploaddocuments", mFile);
        }
    }
    private void initView() {
        mBusiness = (TextView) findViewById(R.id.business);
        mEtSerial = (TextView) findViewById(R.id.et_Serial);
        mFlBusinessLicense = (FrameLayout) findViewById(R.id.fl_business_license);
        mLicenseUpload = (TextView) findViewById(R.id.License_upload);
        mLicense = (TextView) findViewById(R.id.License);


        mIvOpenPhotos = (ImageView) findViewById(R.id.iv_Open_photos);
        mIvOpenPhotos.setOnClickListener(this);
        mPermitPhoto = (ImageView) findViewById(R.id.Permit_photo);
        mPermitPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.iv_Open_photos:
                initAlertDialog(1);
                break;
            case R.id.Permit_photo:
                initAlertDialog(2);
        }
    }

    private void initAlertDialog(final int i) {

            final String[] item = {"相机", "相册"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请选择");
            builder.setItems(item, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {

                        //打开相机
                        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent1.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);//将拍照图片存入mImageUri
                        if (i==1){
                            startActivityForResult(intent1, CAMERA_CODE1);
                        }else{
                            startActivityForResult(intent1, CAMERA_CODE2);
                        }
                    } else {
                        //打开相册
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        if (i==1){
                            startActivityForResult(intent, ALBUM_CODE1);
                        }else{
                            startActivityForResult(intent, ALBUM_CODE2);
                        }
                    }
                }
            });
            // 取消可以不添加
            //builder.setNegativeButton("取消",null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //相册可以使用glide加载 相机不能使用glide加载
        if (resultCode == RESULT_OK){

            //拍照
            if (requestCode == CAMERA_CODE1){

                //显示拍照后的图片
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
                    mIvOpenPhotos.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else if (requestCode==CAMERA_CODE2){
                //显示拍照后的图片
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
                    mPermitPhoto.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
                //相册
            if (requestCode == ALBUM_CODE1){

                //1.获取相册中选中的图片的URi路径
                Uri imageUri = data.getData();

                //显示相册中选中的图片
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    mIvOpenPhotos.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //2.将Uri路径转换为File文件
                File file = getFileFromUri(imageUri, this);


            }else if(requestCode == ALBUM_CODE2){
                //1.获取相册中选中的图片的URi路径
                Uri imageUri = data.getData();

                //显示相册中选中的图片
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    mPermitPhoto.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //2.将Uri路径转换为File文件
                File file = getFileFromUri(imageUri, this);
            }
        }
    }
    //下面两个方法配合相册使用
    public File getFileFromUri(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }
        switch (uri.getScheme()) {
            case "content":
                return getFileFromContentUri(uri, context);
            case "file":
                return new File(uri.getPath());
            default:
                return null;
        }
    }

    /**
     通过内容解析中查询uri中的文件路径
     */
    private File getFileFromContentUri(Uri contentUri, Context context) {
        if (contentUri == null) {
            return null;
        }
        File file = null;
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            cursor.close();

            if (!TextUtils.isEmpty(filePath)) {
                file = new File(filePath);
            }
        }
        return file;
    }
}
