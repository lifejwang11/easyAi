package org.wlld.imageRecognition;

import org.wlld.i.OutBack;
import org.wlld.imageRecognition.border.FrameBody;

/**
 * @author lidapeng
 * @description
 * @date 3:44 下午 2020/1/26
 */

public class ImageBack implements OutBack {
    private FrameBody frameBody;

    public void setFrameBody(FrameBody frameBody) {
        this.frameBody = frameBody;
    }

    @Override
    public void getBack(double out, int id, long eventId) {
        frameBody.setPointAndId(out, id);
    }
}
