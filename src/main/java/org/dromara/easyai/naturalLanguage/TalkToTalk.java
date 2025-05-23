package org.dromara.easyai.naturalLanguage;

import org.dromara.easyai.config.TfConfig;
import org.dromara.easyai.entity.TalkBody;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixOperation;
import org.dromara.easyai.naturalLanguage.word.WordBack;
import org.dromara.easyai.naturalLanguage.word.WordEmbedding;
import org.dromara.easyai.transFormer.TransFormerManager;
import org.dromara.easyai.transFormer.TransWordVector;
import org.dromara.easyai.transFormer.model.TransFormerModel;
import org.dromara.easyai.transFormer.nerve.SensoryNerve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TalkToTalk {
    private final TfConfig tfConfig;
    private final int maxLength;
    private final int times;
    private final TransFormerManager transFormerManager = new TransFormerManager();
    private final boolean splitAnswer;//回答是否带隔断符
    private final String splitWord;//隔断符

    public TalkToTalk(TfConfig tfConfig) throws Exception {
        splitWord = tfConfig.getSplitWord();
        splitAnswer = splitWord != null && !splitWord.isEmpty();
        this.tfConfig = tfConfig;
        maxLength = tfConfig.getMaxLength();
        this.times = tfConfig.getTimes();
        if (times <= 0) {
            throw new Exception("参数times必须大于0");
        }
    }

    private void init(List<TalkBody> talkBodies) throws Exception {
        List<String> sentenceList = new ArrayList<>();
        for (TalkBody talkBody : talkBodies) {
            sentenceList.add(talkBody.getQuestion());
            sentenceList.add(talkBody.getAnswer());
        }
        transFormerManager.init(tfConfig, sentenceList);
    }


    public String getAnswer(String question, long eventID) throws Exception {
        SensoryNerve sensoryNerve = transFormerManager.getSensoryNerve();
        TransWordVector transWordVector = transFormerManager.getTransWordVector();
        int end = transWordVector.getEndID();
        WordBack wordBack = new WordBack();
        int id;
        StringBuilder answer = new StringBuilder();
        int index = 0;
        do {
            String myAnswer = null;
            if (answer.length() > 0) {
                myAnswer = answer.toString();
            }
            sensoryNerve.postSentence(eventID, question, myAnswer, false, wordBack);
            id = wordBack.getId();
            if (id != end) {//没有结束
                String word = transWordVector.getWordByID(id);
                if (splitAnswer) {
                    answer.append(splitWord).append(word);
                } else {
                    answer.append(word);
                }
            }
            index++;
        } while (id != end && index < maxLength);
        String result = answer.toString();
        return result.replace(tfConfig.startWord, "");
    }

    public void insertModel(TransFormerModel transFormerModel) throws Exception {
        transFormerManager.insertModel(transFormerModel, tfConfig);
    }


    public TransFormerModel study(List<TalkBody> talkBodies) throws Exception {
        init(talkBodies);
        SensoryNerve sensoryNerve = transFormerManager.getSensoryNerve();
        int size = talkBodies.size();
        for (int k = 0; k < times; k++) {
            int index = 0;
            for (TalkBody talkBody : talkBodies) {
                index++;
                String question = talkBody.getQuestion();
                String answer = talkBody.getAnswer();
                System.out.println("问题:" + question + ", 回答:" + answer + ",训练语句下标:" + index + ",总数量:" + size + ",当前次数：" + k + ",总次数:" + times);
                sensoryNerve.postSentence(1, question, answer, true, null);
            }
        }
        return transFormerManager.getModel();
    }
}
