import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl;

import java.util.ArrayList;
import java.util.List;

public class TestEsClient {
    public static void main(String[] args) {
        ObjectInfoInnerHandlerImpl objectInfoInnerHandler = new ObjectInfoInnerHandlerImpl();
        List<String> pkeys = new ArrayList<>();
        pkeys.add("123457");
        pkeys.add("223458");
        System.out.println(objectInfoInnerHandler.searchByPkeys(pkeys));
    }
}
