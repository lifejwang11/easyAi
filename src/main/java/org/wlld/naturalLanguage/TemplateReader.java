package org.wlld.naturalLanguage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 模板读取类
 **/
public class TemplateReader {//模板读取类
    private Map<Integer, List<String>> model = new HashMap<>();//训练模板
    private String charsetName;

    /**
     * 读取图片
     *
     * @param url         文字模板的url
     * @param charsetName 文字编码(一般使用UTF-8)
     * @throws Exception 找不到文字抛出异常
     */
    public void read(String url, String charsetName, WordTemple wordTemple) throws Exception {
        this.charsetName = charsetName;
        File file = new File(url);
        InputStream is = new FileInputStream(file);
        int i;
        LinkedList<Byte> span = new LinkedList<>();
        int hang = 0;
        int again = 0;
        int upNub = 0;
        boolean isSymbol = false;//是否遇到分隔符
        while ((i = is.read()) > -1) {
            if (i == IOConst.TYPE_Symbol) {//遇到分隔符号
                isSymbol = true;
            } else {
                if (i == IOConst.STOP_END || i == IOConst.STOP_NEXT) {
                    isSymbol = false;
                    again = again << 1 | 1;
                    if (again == 1) {//第一次进入
                        List<String> lr = model.get(upNub);
                        //addEnd(span);
                        if (lr != null) {
                            lr.add(LinkToString(span));
                        } else {
                            List<String> lis = new ArrayList<>();
                            lis.add(LinkToString(span));
                            model.put(upNub, lis);
                        }
                        upNub = 0;
                        hang++;
                    }
                    again = 0;
                } else {
                    if (isSymbol) {
                        int type = i;
                        if (type >= 48 && type <= 57) {
                            type = type - 48;
                            if (upNub == 0) {
                                upNub = type;
                            } else {
                                upNub = upNub * 10 + type;
                            }
                        }
                    } else {
                        span.add((byte) i);
                    }
                }
            }
        }
        word(wordTemple);
    }

    public void word(WordTemple wordTemple) throws Exception {
        //将模版注入分词器进行分词
        Tokenizer tokenizer = new Tokenizer(wordTemple);
        tokenizer.start(model);
    }

    public String LinkToString(LinkedList<Byte> mod) throws UnsupportedEncodingException {
        int b = mod.size();
        byte[] be = new byte[b];
        for (int i = 0; i < b; i++) {
            be[i] = mod.poll();
        }
        return new String(be, charsetName);
    }
}
