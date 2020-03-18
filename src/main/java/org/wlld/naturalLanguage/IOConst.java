package org.wlld.naturalLanguage;

public class IOConst {
    public static final byte TYPE_Symbol = 0x23;//#号键
    public static final byte STOP_END = 10;
    public static final byte STOP_NEXT = 13;
    public static final byte WIN = 1;//windows系统
    public static final byte NOT_WIN = 2;//非Windows系统
    public static final byte CORE_Number = 6;//核心数

    private static byte sys;

    public static byte getSys() {
        return sys;
    }

    static{
        if(System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS")){
            sys = WIN;
        }else{
            sys = NOT_WIN;
        }
    }
}
