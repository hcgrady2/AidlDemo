// IMusicManager.aidl
package com.example.aidlserver;

// Declare any non-default types here with import statements

import com.example.aidlserver.Music;
import com.example.aidlserver.INewMusicArrivedListener;

interface IMusicManager {

    List<Music> getMusicList();
    void addMusic(in Music music);

    void registerListener(INewMusicArrivedListener listener); // 注册接口
    void unregisterListener(INewMusicArrivedListener listener); // 注销接口

}
