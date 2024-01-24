/*
    author: alphi
    createDate: 2023/6/12
*/

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ExampleUnitTest {
    @Test
    public void test() {
        List<String> objects = new ArrayList<String>() {{
            add("11");
            add("22");
            add("333");
        }};
        for (int i = 0; i < objects.size();) {
            String remove = objects.remove(i);
            System.out.println(remove);
        }
    }
}
