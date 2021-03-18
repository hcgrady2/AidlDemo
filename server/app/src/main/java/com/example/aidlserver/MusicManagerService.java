package com.example.aidlserver;


import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐管理的服务类
 */
public class MusicManagerService extends Service {


    private static final String TAG = "MainActivityTag";
    private ArrayList<Music> mMusicList = new ArrayList<>();  //生成的音乐列表
    private List<INewMusicArrivedListener> mListenerList = new ArrayList<>(); //客户端注册的接口列表
    private boolean isServiceDestroy = false; //当前服务是否结束
    private int num = 0;

    /**
     * 解绑服务
     * @param conn
     */
    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        Log.e(TAG,"unbindService-----");
    }

    /**
     * 服务端通过Binder实现AIDL的IMusicManager.Stub接口
     * 这个类需要实现IMusicManager相关的抽象方法
     */
    private Binder mBinder = new IMusicManager.Stub() {
        @Override
        public List<Music> getMusicList() throws RemoteException {
            SystemClock.sleep(1000); // 延迟加载
            return mMusicList;
        }

        @Override
        public void addMusic(Music music) throws RemoteException {
            mMusicList.add(music);
            try {

                onNewMusicArrived(music);

            }catch (Exception e){

            }

        }

        @Override
        public void registerListener(INewMusicArrivedListener listener) throws RemoteException {
            mListenerList.add(listener);
            int num = mListenerList.size();
            Log.e(TAG, "添加完成, 注册接口数: " + num);
        }

        @Override
        public void unregisterListener(INewMusicArrivedListener listener) throws RemoteException {
            mListenerList.add(listener);
            int num = mListenerList.size();
            Log.e(TAG, "添加完成, 注册接口数: " + num);
        }
    };


    //新音乐到达后给客户端发送相关通知
    private void onNewMusicArrived(Music music) throws Exception {
        mMusicList.add(music);
        Log.e(TAG, "发送通知的数量: " + mMusicList.size());
        int num = mListenerList.size();
        for (int i = 0; i < num; ++i) {
            INewMusicArrivedListener listener = mListenerList.get(i);
            listener.onNewBookArrived(music);
        }
        for (Music b : mMusicList){
            Log.e(TAG,b.name+"  "+b.author);
        }
    }

    @Override public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate-------------");
        //首先添加两首歌曲
        mMusicList.add(new Music("服务器1", "people1"));
        mMusicList.add(new Music("服务器2", "people2"));
        //音乐制造机器
    }

    @Override public void onDestroy() {
        isServiceDestroy = true;
        super.onDestroy();
        Log.e(TAG,"onDestroy-----");
    }





    @Nullable
    @Override public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        int check = checkCallingOrSelfPermission("com.example.zs.ipcdemo.permission.ACCESS_BOOK_SERVIC");
        Log.i(TAG, "onBind: ");
        if(check == PackageManager.PERMISSION_DENIED){
            return null;
        }

        return mBinder;
    }

}