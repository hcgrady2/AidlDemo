package com.example.aidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aidlserver.IMusicManager;
import com.example.aidlserver.INewMusicArrivedListener;
import com.example.aidlserver.Music;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView music_list;
    private static final int MESSAGE_ARRIVED = 1;

    private static final String TAG = "MainActivityTag";
    private IMusicManager mRemoteMusicManager;

    //监听新音乐的到达的接口
    private INewMusicArrivedListener musicArrivedListener = new INewMusicArrivedListener.Stub() {
        /**
         * 服务端有新音乐生成
         * @param newMusic
         * @throws RemoteException
         */
        @Override
        public void onNewBookArrived(Music newMusic) throws RemoteException {
            Log.i(TAG, "onNewBookArrived: 收到了远程服务回调");
            mHandler.obtainMessage(MESSAGE_ARRIVED, newMusic ).sendToTarget();
        }
    };

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ARRIVED:
                    new BookListAsyncTask().execute();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };




    //绑定服务时的链接参数
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected: 绑定成功，准备添加音乐");
            IMusicManager musicManager = IMusicManager.Stub.asInterface(service);
            try {
                mRemoteMusicManager = musicManager;
                Music newMusic = new Music("《客户端音乐》", "author");
                mRemoteMusicManager.addMusic(newMusic);
                mRemoteMusicManager.registerListener(musicArrivedListener);
              //  new BookListAsyncTask().execute();
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.i(TAG, "onServiceConnected: error:"  + e.toString());
            }
        }

        @Override public void onServiceDisconnected(ComponentName name) {
            mRemoteMusicManager = null;
            Log.i(TAG, "onServiceDisconnected: 绑定结束");
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        music_list = findViewById(R.id.music_list);
    }


    public void getMusicList(View view) {

        Log.i(TAG, "getMusicList: 准备获取列表");
        if (mRemoteMusicManager !=null){
            List<Music> list = null;
            try {
                list = mRemoteMusicManager.getMusicList();
            }catch (Exception e){

            }
            if (list!=null){
                String content = "";
                for (int i = 0; i < list.size(); ++i) {
                    content += list.get(i).toString() + "\n";
                }
                music_list.setText(content);
            }
        }
        Toast.makeText(getApplicationContext(), "正在获取中...", Toast.LENGTH_SHORT).show();


    }

    /**
     * meizu 无法绑定
     * @param view
     */
    public void bindService(View view) {

        //常规绑定
//        Log.i(TAG, "bindService: 准备绑定远程服务");
//        Intent intent = new Intent();
//        intent.setPackage("com.example.aidlserver");
//        intent.setAction("com.example.aidlserver.action");
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//
//
//

        /**
         * meizu 手机，必须这样才行
         */
        Log.i(TAG, "bindService: 准备绑定远程服务");
        Intent intent = new Intent();
        intent.setPackage("com.example.aidlserver");
        intent.setAction("com.example.aidlserver.action");
      //  boolean bb=  bindService(intent,mConnection, Context.BIND_AUTO_CREATE);         //其他手机都可以正常调用 唯独魅族手机不可以  真坑啊


        ComponentName cn = new ComponentName("com.example.aidlserver", "com.example.aidlserver.MusicManagerService");
        intent.setAction("com.example.aidlserver.action");
        intent.setComponent(cn);

        getApplicationContext().startService(intent);//这里必须要先startServer然后在bindService不知道魅族在搞什么鬼
        boolean result = bindService(intent, mConnection, Context.BIND_AUTO_CREATE); //绑定远程服务

        Log.i(TAG, "bindService: 准备绑定远程服务结束,result :" + result);


    }



    public void unbindService(View view) {
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public void addMusicList(View view) {

        Log.i(TAG, "addMusicList: 准备添加音乐");
        try {
            if (mRemoteMusicManager != null){
                Music newMusic = new Music("《客户端音乐》", "rock");
                mRemoteMusicManager.addMusic(newMusic);

            }else {
                Log.i(TAG, "addMusicList: manager is null !");

            }
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i(TAG, "onServiceConnected: error:"  + e.toString());
        }

    }

    private class BookListAsyncTask extends AsyncTask<Void, Void, List<Music>> {
        @Override
        protected List<Music> doInBackground(Void... params) {
            Log.i(TAG, "doInBackground: 后台获取服务");
            List<Music> list = null;
            try {
                list = mRemoteMusicManager.getMusicList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Music> musicList) {
            Log.i(TAG, "onPostExecute: 后台拉取完成");
            String content = "";
            for (int i = 0; i < musicList.size(); ++i) {
                content += musicList.get(i).toString() + "\n";
            }
            music_list.setText(content);
        }
    }

}
