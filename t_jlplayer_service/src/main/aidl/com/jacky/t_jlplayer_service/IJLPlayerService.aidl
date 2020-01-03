// IJLPlayerService.aidl
package com.jacky.t_jlplayer_service;
import com.jacky.t_jlplayer_service.IJLPlayerListener;
// Declare any non-default types here with import statements

interface IJLPlayerService {

    void play(int fileId);

    void registerListener(IJLPlayerListener listener);

    void unregisterListener(IJLPlayerListener listener);

}
