import java.awt.*;
import demo.*;

public aspect Aspect {
  public Color Orange.getColor() { return Color.orange; }
  public Color Strawberry.color = Color.red;
  public Fruit.new(Color c,String name) {this();}
}
