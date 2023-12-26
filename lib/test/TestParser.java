import mindustry.Vars;
import mindustry.core.ContentLoader;
import mindustry.type.Item;
import org.junit.jupiter.api.Test;

public class TestParser {
    @SuppressWarnings("unused")
    @Test
    public void input(){
        Vars.content = new ContentLoader();
        final Item item = new Item("test");
    }
}
