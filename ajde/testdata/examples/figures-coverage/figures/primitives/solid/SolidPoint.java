
package figures.primitives.solid;

import java.util.Collection;
import java.lang.String;
import figures.primitives.planar.*;

public class SolidPoint extends Point {
    private int z;

    public SolidPoint(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public int getZ() { return z; }

    public void setZ(int z) { this.z = z; }

    public void incrXY(int dx, int dy) {
        super.incrXY(dx, dy);
        setZ(getZ() + dx + dy);
    }
}
