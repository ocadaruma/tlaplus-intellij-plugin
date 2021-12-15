package pcal;

import com.mayreh.intellij.plugin.tlaplus.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlusCalTranslatorTest {

    @Test
    public void translate() {
        String input = TestUtils.resourceToString("pluscal/translate/test.tla");
        String expected = TestUtils.resourceToString("pluscal/translate/test_expected.tla");
        String actual = PlusCalTranslator.translate(input).getTranslated();
        assertEquals(expected, actual);
    }
}
