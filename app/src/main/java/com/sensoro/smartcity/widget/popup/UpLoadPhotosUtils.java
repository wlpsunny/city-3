package com.sensoro.smartcity.widget.popup;

import android.content.Context;
import android.text.TextUtils;

import com.lzy.imagepicker.bean.ImageItem;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadOptions;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.response.QiNiuToken;
import com.sensoro.smartcity.util.AESUtil;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.util.luban.CompressionPredicate;
import com.sensoro.smartcity.util.luban.Luban;
import com.sensoro.smartcity.util.luban.OnCompressListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UpLoadPhotosUtils {
    private final Context mContext;
    private final List<ImageItem> imageItems = new ArrayList<>();
    private final List<String> ImagesUrl = new ArrayList<>();
    private int pictureNum = 0;
    private final UpLoadPhotoListener upLoadPhotoListener;
    private String baseUrl;

    public UpLoadPhotosUtils(Context context, UpLoadPhotoListener upLoadPhotoListener) {
        mContext = context;
        this.upLoadPhotoListener = upLoadPhotoListener;
    }

    public void doUploadPhoto(List<ImageItem> imageItems) {
        if (imageItems != null && imageItems.size() > 0) {
            baseUrl = "";
            this.ImagesUrl.clear();
            this.imageItems.clear();
            this.imageItems.addAll(imageItems);
            upLoadPhotoListener.onStart();
            getToken();
        }
    }

    private void getToken() {
        RetrofitServiceHelper.INSTANCE.getQiNiuToken().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                .mainThread()).subscribe(new CityObserver<QiNiuToken>() {


            @Override
            public void onCompleted() {
            }

            @Override
            public void onNext(QiNiuToken qiNiuToken) {
                String upToken = qiNiuToken.getUptoken();
                String domain = qiNiuToken.getDomain();
                baseUrl = domain;
                doUpLoadPhoto(upToken);
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                upLoadPhotoListener.onError(errorMsg);
            }
        });
    }

    private void doUpLoadPhoto(final String token) {
        if (imageItems.size() == 0) {
            pictureNum = 0;
            return;
        }
        if (pictureNum == imageItems.size()) {
            //完成
            upLoadPhotoListener.onComplete(ImagesUrl);
            pictureNum = 0;
        } else {
            if (imageItems.size() == 1 && imageItems.get(0).isRecord) {
                File file = new File(imageItems.get(0).recordPath);
                final String key = AESUtil.stringToMD5(file.getName());
                SensoroCityApplication.getInstance().uploadManager.put(file, key, token, new
                        UpCompletionHandler() {
                            @Override
                            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                                if (responseInfo.isOK()) {
                                    ImagesUrl.add(baseUrl + key);
                                    //TODO 拼接key 传入参数
                                    upLoadPhotoListener.onComplete(ImagesUrl);
//                                        LogUtils.loge(this, "responseInfo -->" + responseInfo.toString());
                                    LogUtils.loge(this, "文件路径-->> " + baseUrl + key);
                                } else {
                                    //TODO 可以添加重试获取token机制
                                    LogUtils.loge(this, "上传七牛服务器失败：" + "第【" + (pictureNum + 1) + "】张-" +
                                            imageItems.get
                                                    (pictureNum).name + "-->" + responseInfo.error);
                                    upLoadPhotoListener.onError("上传 " + "第 " + (pictureNum + 1) + " 张失败");
                                    pictureNum = 0;
                                }

                            }
                        }, new UploadOptions(null, null, false, new UpProgressHandler() {
                    @Override
                    public void progress(String key, double percent) {
                        LogUtils.loge(this, key + ": " + "progress ---->>" + percent);
                        upLoadPhotoListener.onProgress(-1, percent);
                    }
                }, null));
            } else {
                String currentPath = imageItems.get(pictureNum).path;
                Luban.with(mContext).ignoreBy(200).load(currentPath).filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
//                    return (!TextUtils.isEmpty(path)) && (path.toLowerCase().endsWith("jpg") || path.toLowerCase()
//                            .endsWith("jpeg") ||
//                            path.toLowerCase().endsWith("png") && !path.toLowerCase().endsWith(".9.png"));

                    }
                }).setCompressListener(new OnCompressListener
                        () {
                    @Override
                    public void onStart() {
                        upLoadPhotoListener.onProgress(pictureNum + 1, 0);
                    }

                    @Override
                    public void onSuccess(File file) {
                        final String key = AESUtil.stringToMD5(file.getName());
                        SensoroCityApplication.getInstance().uploadManager.put(file, key, token, new
                                UpCompletionHandler() {
                                    @Override
                                    public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                                        if (responseInfo.isOK()) {
                                            ImagesUrl.add(baseUrl + key);
                                            //TODO 拼接key 传入参数
//                                        LogUtils.loge(this, "responseInfo -->" + responseInfo.toString());
                                            LogUtils.loge(this, "文件路径-->> " + baseUrl + key);
                                            pictureNum++;
                                            doUpLoadPhoto(token);
                                        } else {
                                            //TODO 可以添加重试获取token机制
                                            LogUtils.loge(this, "上传七牛服务器失败：" + "第【" + (pictureNum + 1) + "】张-" +
                                                    imageItems.get
                                                            (pictureNum).name + "-->" + responseInfo.error);
                                            upLoadPhotoListener.onError("上传 " + "第 " + (pictureNum + 1) + " 张失败");
                                            pictureNum = 0;
                                        }

                                    }
                                }, new UploadOptions(null, null, false, new UpProgressHandler() {
                            @Override
                            public void progress(String key, double percent) {
                                LogUtils.loge(this, key + ": " + "progress ---->>" + percent);
                                upLoadPhotoListener.onProgress(pictureNum + 1, percent);
                            }
                        }, null));
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.loge(this, "压缩 " + "第【" + (pictureNum + 1) + "】张-" + imageItems.get(pictureNum)
                                .name + "失败--->>" + e.getMessage());
                        upLoadPhotoListener.onError("上传 " + "第 " + (pictureNum + 1) + " 张失败");
                        pictureNum = 0;
                    }
                }).launch();
            }
        }

    }

    public interface UpLoadPhotoListener {
        void onStart();

        void onComplete(List<String> imagesUrl);

        void onError(String errMsg);

        void onProgress(int index, double percent);
    }
// responseInfo -->{ver:7.3.12,ResponseInfo:1531386163309485,status:200, reqId:ekAAAGhyR3uhk0AV, xlog:body;0s.ph;0s
// .put.in;0s.put.disk:1;1s.put.in;1s.put.disk:1;1s.ph;PFDS:2;0s.put.out:1;PFDS:3;body;rs38_4.sel:1;rwro.ins:1/same
// entry;rs38_4.sel:5;rwro.getInstance:5;MQ;RS.not:;RS:11;rs.put:17;rs-upload.putFile:20;UP:22, xvia:vdn-tj-cnc-1-2,
// host:upload.qiniup.com, path:/, ip:220.194.102.99, port:443, duration:184 s, time:1531386195, sent:180803,error:null}

    ///////////////
    //            Luban.with(mContext).ignoreBy(200).load(selImageList.getInstance(0).path).setCompressListener(new
    // OnCompressListener() {
//
//
//                @Override
//                public void onStart() {
//                    LogUtils.loge(this, "onStart");
//                }
//
//                @Override
//                public void onSuccess(File file) {
//                    long length = file.length();
//                    LogUtils.loge("file  = " + length);
//                    SensoroCityApplication.getInstance().uploadManager.put(file, "key", "token", new
//                            UpCompletionHandler() {
//
//
//                                @Override
//                                public void complete(String key, ResponseInfo responseInfo, JSONObject
//                                        jsonObject) {
//                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
//                                    if (responseInfo.isOK()) {
//                                        String path = responseInfo.path;
//                                        Log.i("qiniu", "Upload Success");
//                                    } else {
//                                        Log.i("qiniu", "Upload Fail");
//                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
//                                    }
//                                    Log.i("qiniu", key + ",\r\n " + responseInfo + ",\r\n " + jsonObject);
//                                }
//                            }, new UploadOptions(null, null, false, new UpProgressHandler() {
//                        @Override
//                        public void progress(String key, double percent) {
//                            Log.i("qiniu", key + ": " + percent);
//                        }
//                    }, null));
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    LogUtils.loge(this, "onError:" + e.getMessage());
//                }
//            }).launch();
//            Flowable.just(photos)
//                    .observeOn(Schedulers.io())
//                    .map(new Function<List<String>, List<File>>() {
//                        @Override public List<File> apply(@NonNull List<String> list) throws Exception {
//                            // 同步方法直接返回压缩后的文件
//                            return Luban.with(MainActivity.this).load(list).getInstance();
//                        }
//                    })
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe();

//    List<ImageItem> images = adapter.getImages();
//            for (ImageItem imageItem : images) {
//                String path = imageItem.path;
//
//            }
    //
//            RetrofitServiceHelper.INSTANCE.getQiNiuToken().subscribeOn(Schedulers.io()).map(new Func1<ResponseBase,
//                    List<File>>() {
//
//
//                @Override
//                public List<File> call(ResponseBase responseBase) {
//                    ArrayList<String> strings = new ArrayList<>();
//                    for (ImageItem imageItem : selImageList) {
//                        strings.add(imageItem.path);
//                    }
//                    try {
//
//                        return Luban.with(mContext).load(strings).ignoreBy(200).getInstance();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }
//            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<File>>() {
//                @Override
//                public void onCompleted() {
//
//                }
//
//                @Override
//                public void onError(Throwable e) {
//
//                }
//
//                @Override
//                public void onNext(List<File> imageItems) {
//
//                }
//            });
    //


    //
//            Observable.just(selImageList).subscribeOn(Schedulers.io()).map(new Func1<List<ImageItem>, List<File>>() {
//                @Override
//                public List<File> call(List<ImageItem> imageItems) {
//                    ArrayList<String> strings = new ArrayList<>();
//                    for (ImageItem imageItem : imageItems) {
//                        strings.add(imageItem.path);
//                    }
//                    try {
//
//                        return Luban.with(mContext).load(strings).ignoreBy(200).getInstance();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    // 同步方法直接返回压缩后的文件
////                            return Luban.with(MainActivity.this).load(list).getInstance();
////                    Luban.with(mContext).load(imageItems).getInstance()
//                    return null;
//                }
//            }).flatMap(new Func1<List<File>, Observable<ResponseBase>>() {
//                @Override
//                public Observable<ResponseBase> call(List<File> imageItems) {
//
//                    return RetrofitServiceHelper.INSTANCE.doUpdatePhotos("");
//                }
//            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseBase>() {
//                @Override
//                public void onCompleted() {
//
//                }
//
//                @Override
//                public void onNext(ResponseBase responseBase) {
//
////                    SensoroCityApplication.getInstance().uploadManager.put(
////                    );
//                }
//
//                @Override
//                public void onErrorMsg(int errorCode, String errorMsg) {
//
//                }
//            });


    //
//    public Observable upload(final String path,final String key) {
//
//        return Observable.create(new Observable.OnSubscribe() {
//
//    @Override
//
//    public void call(final Subscriber subscriber) {
//
//                uploadManager.put(path,key, YebaConstants.QINIUTOKEN,
//
//                        new UpCompletionHandler() {
//
//                            @Override
//
//                            public void complete(String key, ResponseInfo info, JSONObject res) {
//
//                                if(info.isOK()){
//
//                                    subscriber.onNext(key);
//
//                                    subscriber.onCompleted();
//
//                                }else{
//
//                                    LogUtil.e(info.toString());
//
//                                    APIError apiError=newAPIError(info.statusCode,info.error);
//
//                                    subscriber.onError(apiError);
//
//                                }
//
//                            }，null);
//
//                        }).retryWhen(new HttpTokenExpireFunc());
//
//            }

}
