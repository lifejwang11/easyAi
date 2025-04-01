package org.dromara.easyai.transFormer;

import org.dromara.easyai.config.TfConfig;
import org.dromara.easyai.matrixTools.Matrix;
import org.dromara.easyai.matrixTools.MatrixList;
import org.dromara.easyai.matrixTools.MatrixOperation;

import java.util.*;

/**
 * @author lidapeng
 * @time 2025/3/29 09:11
 * @des transFormer 词向量
 */
public class TransWordVector {
    private final List<String> wordList = new ArrayList<>();//词离散id
    private final List<Matrix> wordVectorList = new ArrayList<>();//词向量
    private final WordIds wordIds = new WordIds();
    private final String splitWord;
    private final int featureDimension;//词向量维度
    private final Random random = new Random();
    private final String startWord;
    private final String endWord;
    private final MatrixOperation matrixOperation = new MatrixOperation();
    private final float studyRate;

    public TransWordVector(TfConfig tfConfig) throws Exception {
        this.splitWord = tfConfig.getSplitWord();
        this.studyRate = tfConfig.getStudyPoint();
        this.featureDimension = tfConfig.getFeatureDimension();
        startWord = tfConfig.getStartWord();
        endWord = tfConfig.getEndWord();
        wordList.add(startWord);
        wordList.add(endWord);
        initWordVector();
        initWordVector();
    }

    public void backEncoderError(Matrix error) throws Exception {
        List<Integer> ids = wordIds.getEncoder();
        int size = ids.size();
        if (size == error.getX()) {
            updateWordVector(ids, error);
            wordIds.getEncoder().clear();
        } else {
            throw new Exception("编码器误差返回长度不一致");
        }
    }

    private void updateWordVector(List<Integer> ids, Matrix error) throws Exception {
        int size = ids.size();
       // matrixOperation.mathMul(error, studyRate);
        for (int i = 0; i < size; i++) {
            int index = ids.get(i);
            Matrix wordError = error.getRow(i);
            Matrix wordVector = wordVectorList.get(index);
            wordVector = matrixOperation.add(wordVector, wordError);
            wordVectorList.set(index, wordVector);
        }
    }

    public void backDecoderError(Matrix errorMatrix, Matrix allFeature) throws Exception {
        Matrix error = matrixOperation.add(errorMatrix, allFeature);
        List<Integer> ids = wordIds.getDecoder();
        int size = ids.size();
        if (size == error.getX()) {
            updateWordVector(ids, error);
            wordIds.getDecoder().clear();
        } else {
            throw new Exception("解码器误差返回长度不一致");
        }
    }

    public int getWordID(String word) {//获取离散id
        int id = -1;
        int size = wordList.size();
        for (int i = 0; i < size; i++) {
            if (wordList.get(i).equals(word)) {
                id = i + 1;
                break;
            }
        }
        return id;
    }

    public List<Integer> getE(String word) {//获取期望
        List<Integer> result = new ArrayList<>();
        if (splitWord == null) {
            for (int i = 0; i < word.length(); i++) {
                result.add(getWordID(word.substring(i, i + 1)));
            }
        } else {
            String[] words = word.split(splitWord);
            for (String s : words) {
                result.add(getWordID(s));
            }
        }
        result.add(2);
        return result;
    }

    private Matrix getVector(String word, boolean decoder, boolean study) {
        int size = wordList.size();
        Matrix feature = null;
        List<Integer> ids = null;
        if (decoder && study) {
            ids = wordIds.getDecoder();
        } else if (!decoder && study) {
            ids = wordIds.getEncoder();
        }
        for (int i = 0; i < size; i++) {
            if (wordList.get(i).equals(word)) {
                if (ids != null) {
                    ids.add(i);
                }
                feature = wordVectorList.get(i);
                break;
            }
        }
        if (feature == null) {
            feature = new Matrix(1, featureDimension);
        }
        return feature;
    }

    public Matrix getWordVector(String word, boolean decoder, boolean study) throws Exception {//获取词向量
        MatrixList matrixList = null;
        if (decoder) {
            if (study) {
                wordIds.getDecoder().add(0);
            }
            matrixList = new MatrixList(wordVectorList.get(0), true, 128);
        }
        if (word != null && !word.isEmpty()) {
            if (splitWord == null) {
                int size = word.length();
                for (int i = 0; i < size; i++) {
                    Matrix feature = getVector(word.substring(i, i + 1), decoder, study);
                    if (matrixList == null) {
                        matrixList = new MatrixList(feature, true, 128);
                    } else {
                        matrixList.add(feature);
                    }
                }
            } else {
                String[] myWord = word.split(splitWord);
                for (String s : myWord) {
                    Matrix feature = getVector(s, decoder, study);
                    if (matrixList == null) {
                        matrixList = new MatrixList(feature, true, 128);
                    } else {
                        matrixList.add(feature);
                    }
                }
            }
        }
        return matrixList.getMatrix();
    }

    private void initWordVector() throws Exception {
        Matrix matrix = new Matrix(1, featureDimension);
        for (int j = 0; j < featureDimension; j++) {
            matrix.setNub(0, j, random.nextFloat());
        }
        wordVectorList.add(matrix);
    }

    private void insertWord(String word) throws Exception {
        if (!word.equals(startWord) && !word.equals(endWord)) {
            boolean here = false;
            for (String myWord : wordList) {
                if (myWord.equals(word)) {
                    here = true;
                    break;
                }
            }
            if (!here) {
                wordList.add(word);
                initWordVector();
            }
        } else {
            throw new Exception("任何字词不可以与结束符或开始符重叠");
        }
    }

    public int init(List<String> sentenceList) throws Exception {
        for (String sentence : sentenceList) {
            if (sentence != null && !sentence.isEmpty()) {
                if (splitWord == null) {
                    for (int i = 0; i < sentence.length(); i++) {
                        insertWord(sentence.substring(i, i + 1));
                    }
                } else {
                    String[] myWord = sentence.split(splitWord);
                    for (String s : myWord) {
                        insertWord(s);
                    }
                }
            }
        }
        return wordList.size();
    }

}
