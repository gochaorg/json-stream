package xyz.cofe.json.stream.token;

public class Digit {
    public static int digit(char c, int base) {
        return switch (c) {
            case '0' -> 0;
            case '1' -> base >= 2 ? 1 : -1;
            case '2' -> base >= 3 ? 2 : -1;
            case '3' -> base >= 4 ? 3 : -1;
            case '4' -> base >= 5 ? 4 : -1;
            case '5' -> base >= 6 ? 5 : -1;
            case '6' -> base >= 7 ? 6 : -1;
            case '7' -> base >= 8 ? 7 : -1;
            case '8' -> base >= 9 ? 8 : -1;
            case '9' -> base >= 10 ? 9 : -1;
            case 'a', 'A' -> base >= 11 ? 10 : -1;
            case 'b', 'B' -> base >= 12 ? 11 : -1;
            case 'c', 'C' -> base >= 13 ? 12 : -1;
            case 'd', 'D' -> base >= 14 ? 13 : -1;
            case 'e', 'E' -> base >= 15 ? 14 : -1;
            case 'f', 'F' -> base >= 16 ? 15 : -1;
            default -> -1;
        };
    }
}
