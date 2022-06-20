package net.liplum.multicraft;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.Element;
import mindustry.graphics.Drawf;

public class Arrow extends Element {
    public int direction = 0;
    public Color outline = new Color();

    @Override
    public void draw() {
        super.draw();
        float bodyWidth = width * 0.618f;
        float bodyHeight = height * 0.618f;
        float trWidth = width * 0.382f;
        float trHeight = height * 0.382f;
        Draw.color(color);
        Point2 dir = Geometry.d4((direction + 3) % 4);
        float dx = dir.x;
        float dy = dir.y * -1;
        Fill.crect(
            x + (width / 2f - bodyWidth / 2f) * dx,
            y + (height / 2f - bodyHeight / 2f) * dy,
            bodyWidth, bodyHeight);
        Drawf.tri(
            x + width / 2f + trWidth / 2f,
            y + height / 2f + trHeight / 2f,
            trWidth, trHeight, direction * 90f);
        Draw.reset();
    }

    public void right() {
        direction = 0;
    }

    public void top() {
        direction = 1;
    }

    public void left() {
        direction = 2;
    }

    public void bottom() {
        direction = 3;
    }
}
