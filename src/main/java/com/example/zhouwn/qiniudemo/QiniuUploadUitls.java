package com.example.zhouwn.qiniudemo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.PutPolicy;


public class QiniuUploadUitls {

    /**
     * 在网站上查看
     */
//	private static final String ACCESS_KEY = "-K5W7y9j-G-3QjWt29TRzAeyQQXAU6OmRtJGn65n";
    private static final String ACCESS_KEY = "0hwEuRUQsvOEUr29fWBcr7MMB44b5X7_0Tj0gDfO";
    /**
     * 在网站上查看
     */
//	private static final String SECRET_KEY = "xYzT9Tm2nP0yy6-fTMOoozdU117dSzrTiVbMJvF1";
    private static final String SECRET_KEY = "PZ-oe7QGCH2OHXWkDQNu5TgvrTIdIObVpYJF1WIi";
    /**
     * 你所创建的空间的名称
     */
//    private static final String bucketName = "bluemobi";
    private static final String bucketName = "qiniudemo";

    /**
     * 网站上去查看供外部访问的外链地址,在空间设置--域名设置中
     */
//    http://7xkedg.com1.z0.glb.clouddn.com/
    private static final String FILEPATH = "http://7xkedg.com1.z0.glb.clouddn.com/";

    private static final String fileName = "temp.jpg";

    private static final String tempJpeg = Environment
            .getExternalStorageDirectory().getPath() + "/" + fileName;

    private int maxWidth = 720;
    private int maxHeight = 1080;

    public interface QiniuUploadUitlsListener {
        public void onSucess(String fileUrl);

        public void onError(int errorCode, String msg);

        public void onProgress(int progress);
    }

    private QiniuUploadUitls() {

    }

    private static QiniuUploadUitls qiniuUploadUitls = null;

    private UploadManager uploadManager = new UploadManager();

    public static QiniuUploadUitls getInstance() {
        if (qiniuUploadUitls == null) {
            qiniuUploadUitls = new QiniuUploadUitls();
        }
        return qiniuUploadUitls;
    }

    public boolean saveBitmapToJpegFile(Bitmap bitmap, String filePath) {
        return saveBitmapToJpegFile(bitmap, filePath, 75);
    }

    public boolean saveBitmapToJpegFile(Bitmap bitmap, String filePath,
                                        int quality) {
        try {
            FileOutputStream fileOutStr = new FileOutputStream(filePath);
            BufferedOutputStream bufOutStr = new BufferedOutputStream(
                    fileOutStr);
            resizeBitmap(bitmap).compress(CompressFormat.JPEG, quality, bufOutStr);
            bufOutStr.flush();
            bufOutStr.close();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    /**
     * 缩小图片
     *
     * @param bitmap
     * @return
     */
    public Bitmap resizeBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxWidth) {
                int pWidth = maxWidth;
                int pHeight = maxWidth * height / width;
                Bitmap result = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false);
                bitmap.recycle();
                return result;
            }
            if (height > maxHeight) {
                int pHeight = maxHeight;
                int pWidth = maxHeight * width / height;
                Bitmap result = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false);
                bitmap.recycle();
                return result;
            }
        }
        return bitmap;
    }

    public void uploadImage(Bitmap bitmap, QiniuUploadUitlsListener listener) {
//        saveBitmapToJpegFile(bitmap, tempJpeg);
//        uploadImage(tempJpeg, listener);
        uploadImage(Bitmap2Bytes(bitmap), listener);
    }

    public void uploadImage(byte[] bytes, final QiniuUploadUitlsListener listener) {
        final String fileUrlUUID = getFileUrlUUID();
        String token = getToken();
        if (token == null) {
            if (listener != null) {
                listener.onError(-1, "token is null");
            }
            return;
        }
        uploadManager.put(bytes, fileUrlUUID, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info,
                                         JSONObject response) {
                        System.out.println("debug:info = " + info
                                + ",response = " + response);
                        if (info != null && info.statusCode == 200) {// 上传成功
                            String fileRealUrl = getRealUrl(fileUrlUUID);
                            System.out.println("debug:fileRealUrl = " + fileRealUrl);
                            if (listener != null) {
                                listener.onSucess(fileRealUrl);
                            }
                        } else {
                            if (listener != null) {
                                listener.onError(info.statusCode, info.error);
                            }
                        }
                    }
                }, new UploadOptions(null, null, false,
                        new UpProgressHandler() {
                            public void progress(String key, double percent) {
                                if (listener != null) {
                                    listener.onProgress((int) (percent * 100));
                                }
                            }
                        }, null));

    }

    public void uploadImage(String filePath, final QiniuUploadUitlsListener listener) {
        final String fileUrlUUID = getFileUrlUUID();
        String token = getToken();
        if (token == null) {
            if (listener != null) {
                listener.onError(-1, "token is null");
            }
            return;
        }
        uploadManager.put(filePath, fileUrlUUID, token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info,
                                         JSONObject response) {
                        System.out.println("debug:info = " + info
                                + ",response = " + response);
                        if (info != null && info.statusCode == 200) {// 上传成功
                            String fileRealUrl = getRealUrl(fileUrlUUID);
                            System.out.println("debug:fileRealUrl = " + fileRealUrl);
                            if (listener != null) {
                                listener.onSucess(fileRealUrl);
                            }
                        } else {
                            if (listener != null) {
                                listener.onError(info.statusCode, info.error);
                            }
                        }
                    }
                }, new UploadOptions(null, null, false,
                        new UpProgressHandler() {
                            public void progress(String key, double percent) {
                                if (listener != null) {
                                    listener.onProgress((int) (percent * 100));
                                }
                            }
                        }, null));

    }

    /**
     * 生成远程文件路径（全局唯一）
     *
     * @return
     */
    private String getFileUrlUUID() {
        String filePath = android.os.Build.MODEL + "__"
                + System.currentTimeMillis() + "__"
                + (new Random().nextInt(500000)) + "_"
                + (new Random().nextInt(10000));
        return filePath.replace(".", "0");
    }

    private String getRealUrl(String fileUrlUUID) {
        String filePath = FILEPATH + fileUrlUUID;
        return filePath;
    }

    /**
     * 获取token 本地生成
     *
     * @return
     */
    private String getToken() {
        Mac mac = new Mac(ACCESS_KEY, SECRET_KEY);
        PutPolicy putPolicy = new PutPolicy(bucketName);
        putPolicy.returnBody = "{\"name\": $(fname),\"size\": \"$(fsize)\",\"w\": \"$(imageInfo.width)\",\"h\": \"$(imageInfo.height)\",\"key\":$(etag)}";
        try {
            String uptoken = putPolicy.token(mac);
            System.out.println("debug:uptoken = " + uptoken);
            return uptoken;
        } catch (AuthException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.PNG, 80, baos);
        return baos.toByteArray();
    }

}
