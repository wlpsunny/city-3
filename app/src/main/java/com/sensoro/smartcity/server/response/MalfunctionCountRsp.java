package com.sensoro.smartcity.server.response;

public class MalfunctionCountRsp extends ResponseBase {
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int count;
}
