package org.dromara.easyai.conv.dcn;

/**
 * @author lidapeng
 * @time 2026/6/5 15:31
 */
public class DcnErrorBody {
    private float errorM;
    private float errorNextX;
    private float errorNextY;
    private float errorF00;
    private float errorF01;
    private float errorF10;
    private float errorF11;

    public float getErrorF00() {
        return errorF00;
    }

    public void setErrorF00(float errorF00) {
        this.errorF00 = errorF00;
    }

    public float getErrorF01() {
        return errorF01;
    }

    public void setErrorF01(float errorF01) {
        this.errorF01 = errorF01;
    }

    public float getErrorF10() {
        return errorF10;
    }

    public void setErrorF10(float errorF10) {
        this.errorF10 = errorF10;
    }

    public float getErrorF11() {
        return errorF11;
    }

    public void setErrorF11(float errorF11) {
        this.errorF11 = errorF11;
    }

    public float getErrorM() {
        return errorM;
    }

    public void setErrorM(float errorM) {
        this.errorM = errorM;
    }

    public float getErrorNextX() {
        return errorNextX;
    }

    public void setErrorNextX(float errorNextX) {
        this.errorNextX = errorNextX;
    }

    public float getErrorNextY() {
        return errorNextY;
    }

    public void setErrorNextY(float errorNextY) {
        this.errorNextY = errorNextY;
    }
}
