package org.dromara.easyai.yolo;

import java.util.List;

public class YoloSample {
    private String locationURL;//图片本地url
    private List<YoloBody> yoloBodies;

    public String getLocationURL() {
        return locationURL;
    }

    public void setLocationURL(String locationURL) {
        this.locationURL = locationURL;
    }

    public List<YoloBody> getYoloBodies() {
        return yoloBodies;
    }

    public void setYoloBodies(List<YoloBody> yoloBodies) {
        this.yoloBodies = yoloBodies;
    }
}
