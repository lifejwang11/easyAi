package org.dromara.easyai.rnnJumpNerveEntity;

import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.i.OutBack;
import org.dromara.easyai.naturalLanguage.word.WordEmbedding;

import java.util.List;

public class NerveCenter extends MatrixOperation {//神经中枢
    private final int depth;//该神经中枢的深度
    private final double powerTh;//权重阈值
    private final List<Nerve> nerveList;//该神经中枢控制的对应隐层神经元集合
    private WordEmbedding wordEmbedding;//词嵌入
    private final boolean isFinish;//是否是最后一层

    public int getDepth() {
        return depth;
    }

    public NerveCenter(int depth, List<Nerve> nerveList, double powerTh, boolean isFinish) {
        this.depth = depth;
        this.nerveList = nerveList;
        this.powerTh = powerTh;
        this.isFinish = isFinish;
    }

    public void setWordEmbedding(WordEmbedding wordEmbedding) {
        this.wordEmbedding = wordEmbedding;
    }

    public void backType(long eventId, double parameter, int id, Matrix featureMatrix, OutBack outBack, String myWord) throws Exception {
        if (id > 0 && parameter > powerTh) {//增加新特征继续传播
            String nextWord = wordEmbedding.getWord(id - 1);
            if (myWord == null) {
                myWord = nextWord;
            } else {
                myWord = myWord + nextWord;
            }
            if (isFinish) {//输出结果
                outBack.backWord(myWord, eventId);
            } else {
                Matrix matrix = wordEmbedding.getEmbedding(nextWord, eventId, false).getFeatureMatrix();
                featureMatrix = pushVector(featureMatrix, matrix, true);
                go(eventId, featureMatrix, outBack, myWord);
            }
        } else {//停止继续传播 进行输出
            outBack.backWord(myWord, eventId);
        }


    }

    private void go(long eventId, Matrix featureMatrix, OutBack outBack, String word) throws Exception {
        //神经中枢收到传递命令 将命令传递给本层神经元
        for (Nerve nerve : nerveList) {//将信息发送给目标层隐层神经元
            nerve.sendMyTestMessage(eventId, featureMatrix, outBack, word);
        }
    }

}
