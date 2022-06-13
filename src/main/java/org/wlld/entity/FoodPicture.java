package org.wlld.entity;

import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
public class FoodPicture {
    private int id;//类别id
    private List<PicturePosition> picturePositionList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<PicturePosition> getPicturePositionList() {
        return picturePositionList;
    }

    public void setPicturePositionList(List<PicturePosition> picturePositionList) {
        this.picturePositionList = picturePositionList;
    }
}
