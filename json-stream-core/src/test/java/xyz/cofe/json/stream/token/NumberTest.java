package xyz.cofe.json.stream.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumberTest {
    @Test
    public void rawFloat(){
        assertTrue( new NumberParser.RawFloat("1.1e+1").toDouble() == 11.0 );
        assertTrue( new NumberParser.RawFloat("1.1E+1").toDouble() == 11.0 );
        assertTrue( new NumberParser.RawFloat("1.1e-1").toDouble() == 0.11 );
        assertTrue( new NumberParser.RawFloat("1.1E-1").toDouble() == 0.11 );
        assertTrue( new NumberParser.RawFloat("1.1e1").toDouble() == 11.0 );
        assertTrue( new NumberParser.RawFloat("1.1E1").toDouble() == 11.0 );
        assertTrue( new NumberParser.RawFloat("1.e+1").toDouble() == 10.0 );
        assertTrue( new NumberParser.RawFloat("1.E+1").toDouble() == 10.0 );
        assertTrue( new NumberParser.RawFloat("1.e-1").toDouble() == 0.10 );
        assertTrue( new NumberParser.RawFloat("1.E-1").toDouble() == 0.10 );
        assertTrue( new NumberParser.RawFloat("1.e1").toDouble() == 10.0 );
        assertTrue( new NumberParser.RawFloat("1.E1").toDouble() == 10.0 );
        assertTrue( new NumberParser.RawFloat("1.").toDouble() == 1.0 );
        assertTrue( new NumberParser.RawFloat(".1e+1").toDouble() == 1.0 );
        assertTrue( new NumberParser.RawFloat(".1e-1").toDouble() == 0.01 );
        assertTrue( new NumberParser.RawFloat(".1E+1").toDouble() == 1.0 );
        assertTrue( new NumberParser.RawFloat(".1E-1").toDouble() == 0.01 );
        assertTrue( new NumberParser.RawFloat(".1e1").toDouble() == 1.0 );
        assertTrue( new NumberParser.RawFloat(".1E1").toDouble() == 1.0 );
        assertTrue( new NumberParser.RawFloat(".1").toDouble() == 0.1 );
    }
}
