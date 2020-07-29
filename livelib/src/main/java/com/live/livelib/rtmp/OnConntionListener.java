package com.live.livelib.rtmp;

public interface OnConntionListener {

    void onConntecting();

    void onConntectSuccess();

    void onConntectFail(String msg);
}
