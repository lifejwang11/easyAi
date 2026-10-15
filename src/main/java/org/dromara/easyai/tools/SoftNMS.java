package org.dromara.easyai.tools;

import org.dromara.easyai.entity.Box;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description 软非极大值抑制 Soft-NMS
 * 论文: Soft-NMS - Improving Object Detection With One Line of Code (Bodla et al., ICCV 2017)
 * <p>
 * 硬NMS({@link NMS})只要发现某框与最高分框的IoU超过阈值就直接删除,
 * 当两个真实目标靠得比较近(密集行人、遮挡场景)时, 其中一个会被误删导致漏检。
 * Soft-NMS不再直接删除, 而是按重叠程度衰减其置信度:
 * 1.线性衰减(LINEAR): IoU低于阈值不惩罚, 超过阈值按 s = s * (1 - IoU) 衰减
 * 2.高斯衰减(GAUSSIAN): s = s * exp(-IoU^2 / sigma), 重叠越多衰减越狠
 * 最后只保留置信度仍高于scoreThresh的框。
 * <p>
 * 注意: 衰减后的分数会通过{@link Box#setConfidence(float)}写回检测框;
 * 本类不会删除入参List中的元素(与父类NMS不同), 但会修改其中Box对象的confidence。
 */
public class SoftNMS extends NMS {
    public static final int LINEAR = 0;//线性衰减(论文默认)
    public static final int GAUSSIAN = 1;//高斯衰减

    public static final float DEFAULT_IOU_TH = 0.3f;//线性模式默认IoU阈值 N_t
    public static final float DEFAULT_SIGMA = 0.5f;//高斯模式默认衰减系数 sigma
    public static final float DEFAULT_SCORE_TH = 0.001f;//默认最终置信度阈值

    private final float iouThreshold;//IoU阈值 N_t(仅线性模式使用)
    private final float sigma;//高斯衰减系数 sigma
    private final float scoreThresh;//最终置信度阈值
    private final int method;//衰减方式 LINEAR / GAUSSIAN

    /**
     * 全参构造
     *
     * @param iouTh       线性模式的IoU阈值N_t(高斯模式传0即可)
     * @param sigma       高斯模式的衰减系数(线性模式用默认值即可)
     * @param scoreThresh 最终置信度阈值, 衰减后低于该值的框被丢弃
     * @param method      {@link #LINEAR} 或 {@link #GAUSSIAN}
     */
    public SoftNMS(float iouTh, float sigma, float scoreThresh, int method) {
        super(iouTh);
        if (method != LINEAR && method != GAUSSIAN) {
            throw new IllegalArgumentException("softNms method must be LINEAR(0) or GAUSSIAN(1)");
        }
        if (method == GAUSSIAN && sigma <= 0) {
            throw new IllegalArgumentException("sigma must be positive in gaussian mode");
        }
        this.iouThreshold = iouTh;
        this.sigma = sigma;
        this.scoreThresh = scoreThresh;
        this.method = method;
    }

    /**
     * 线性衰减 Soft-NMS, 推荐参数 iouTh=0.3
     * s_i = s_i * (1 - IoU), IoU >= N_t 时才衰减
     */
    public static SoftNMS linear(float iouTh, float scoreThresh) {
        //sigma 仅高斯模式使用, 线性模式不读取, 传0占位
        return new SoftNMS(iouTh, 0f, scoreThresh, LINEAR);
    }

    /**
     * 高斯衰减 Soft-NMS, 推荐参数 sigma=0.5
     * s_i = s_i * exp(-IoU^2 / sigma)
     */
    public static SoftNMS gaussian(float sigma, float scoreThresh) {
        //iouTh 仅线性模式使用, 高斯模式不读取, 传0占位
        return new SoftNMS(0f, sigma, scoreThresh, GAUSSIAN);
    }

    @Override
    public List<Box> start(List<Box> pixelPositions) {
        List<Box> result = new ArrayList<Box>();
        if (pixelPositions == null || pixelPositions.isEmpty()) {
            return result;
        }
        int n = pixelPositions.size();
        //与输入框一一对应, 实时维护衰减后的分数
        float[] scores = new float[n];
        for (int i = 0; i < n; i++) {
            scores[i] = pixelPositions.get(i).getConfidence();
        }
        //候选框存活标记
        boolean[] alive = new boolean[n];
        Arrays.fill(alive, true);
        int aliveCount = n;
        while (aliveCount > 0) {
            //分数在持续衰减, 原排序已经失效, 每轮必须重新选出当前最高分框
            int maxIndex = -1;
            float maxScore = -1f;
            for (int i = 0; i < n; i++) {
                if (alive[i] && scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxIndex = i;
                }
            }
            //剩余最高分都低于阈值, 其余框只会更低, 直接结束
            if (maxScore < scoreThresh) {
                break;
            }
            Box maxBox = pixelPositions.get(maxIndex);
            //把衰减后的分数写回检测框
            maxBox.setConfidence(maxScore);
            result.add(maxBox);
            alive[maxIndex] = false;
            aliveCount--;
            //按与最高分框的重叠程度衰减其余候选框
            for (int i = 0; i < n; i++) {
                if (!alive[i]) {
                    continue;
                }
                float iou = getIOU(maxBox, pixelPositions.get(i));
                float decay;
                if (method == GAUSSIAN) {
                    //s_i = s_i * exp(-IoU^2 / sigma)
                    decay = (float) Math.exp(-(iou * iou) / sigma);
                } else if (iou >= iouThreshold) {
                    //s_i = s_i * (1 - IoU)
                    decay = 1f - iou;
                } else {
                    //IoU低于阈值, 不惩罚
                    decay = 1f;
                }
                scores[i] *= decay;
            }
        }
        return result;
    }
}
//（注：内容由AI生成）
