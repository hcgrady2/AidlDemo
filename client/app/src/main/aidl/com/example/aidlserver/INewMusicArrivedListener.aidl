// INewMusicArrivedListener.aidl
package com.example.aidlserver;

// Declare any non-default types here with import statements
import com.example.aidlserver.Music;

interface INewMusicArrivedListener {

    void onNewBookArrived(in Music newMusic);

}
