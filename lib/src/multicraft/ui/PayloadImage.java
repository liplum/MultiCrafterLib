package multicraft.ui;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

import mindustry.core.*;
import mindustry.type.*;
import mindustry.ui.*;

public class PayloadImage extends Stack {
    
    public PayloadImage(TextureRegion region) {
        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));
    }

    public PayloadImage(TextureRegion region, float amount) {
        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        if (amount != 0) {
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ?
                        UI.formatAmount((long) amount) :
                        Mathf.round(amount) + "").fontScale(0.9f)
                    .style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public PayloadImage(PayloadStack stack) {
        this(stack.item.uiIcon, stack.amount);
    }
}
