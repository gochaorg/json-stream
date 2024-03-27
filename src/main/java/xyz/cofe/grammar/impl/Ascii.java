package xyz.cofe.grammar.impl;

public class Ascii {
    public final static String ESC = "\u001b[";

    public final static String reset = ESC + "0m";
    public final static String bold = ESC + "1m";

    public final static String italicOn = ESC + "3m";
    public final static String italicOff = ESC + "23m";

    public final static String underlineOn = ESC + "4m";
    public final static String underlineOff = ESC + "24m";

    public static enum Color {
        Black(0,false),
        BlackBright(0,true),
        Red(1,false),
        RedBright(1,true),
        Green(2,false),
        GreenBright(2,true),
        Yellow(3,false),
        YellowBright(3,true),
        Blue(4,false),
        BlueBright(4,true),
        Magenta(5,false),
        MagentaBright(5,true),
        Cyan(6,false),
        CyanBright(6,true),
        White(7,false),
        WhiteBright(7,true),
        Default(9,false),
        ;

        private final int offset;
        private final boolean bright;
        Color(int offset,boolean bright) {
            this.offset = offset;
            this.bright = bright;
        }

        public String foreground(){
            return ESC + (30+offset)+";"+(bright ? "2" : "22") + "m";
        }

        public String background(){
            return ESC + (40+offset)+";"+(bright ? "2" : "22") + "m";
        }
    }
}
