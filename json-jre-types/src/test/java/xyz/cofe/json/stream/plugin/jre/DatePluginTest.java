package xyz.cofe.json.stream.plugin.jre;

import org.junit.jupiter.api.Test;
import xyz.cofe.json.stream.rec.StdMapper;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SimplifiableAssertion")
public class DatePluginTest {
    @Test
    public void format(){
        var date = new java.util.Date();
        var str = DatePlugin.simpleDateFormat.format(date);
        System.out.println(str);
    }

    public record Time1(
        java.util.Date utlDate,
        java.sql.Date sqlDate,
        java.sql.Time sqlTime,
        java.sql.Timestamp sqlTimestamp
    ) {}

    @Test
    public void time1(){
        StdMapper mapper = new StdMapper();
        var dt = new java.util.Date();
        var time1 = new Time1(
            new java.util.Date(dt.getTime()),
            new java.sql.Date(dt.getTime()),
            new java.sql.Time(dt.getTime()),
            new java.sql.Timestamp(dt.getTime())
        );

        var json = mapper.toJson(time1,true);
        System.out.println(json);

        Time1 time2 = mapper.parse(json, Time1.class);
        System.out.println(time2);

        SimpleDateFormat yMd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat Hms = new SimpleDateFormat("HH:mm:ss");

        assertTrue(yMd.format(time1.utlDate).equals(yMd.format(time2.utlDate)));
        assertTrue(Hms.format(time1.utlDate).equals(Hms.format(time2.utlDate)));

        assertTrue(yMd.format(time1.sqlDate).equals(yMd.format(time2.sqlDate)));

        assertTrue(Hms.format(time1.sqlTime).equals(Hms.format(time2.sqlTime)));

        assertTrue(yMd.format(time1.sqlTimestamp).equals(yMd.format(time2.sqlTimestamp)));
        assertTrue(Hms.format(time1.sqlTimestamp).equals(Hms.format(time2.sqlTimestamp)));
    }
}
