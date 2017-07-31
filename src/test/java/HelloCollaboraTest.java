import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mperuzzi on 31/07/17.
 */
public class HelloCollaboraTest {

    private HelloCollabora hello;

    @Before
    public void init() {
        hello = new HelloCollabora();
    }

    @Test
    public void printHelloCollabora() throws Exception {
        assertEquals(hello.printHelloCollabora(3), "Hello 3 times from Collabora");
    }

}