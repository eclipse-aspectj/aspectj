
class Pos {

  int x = 0;
  int y = 0;

  int getX() { 
    return(x);
  }

  int getY() {
    return(y);
  }
  
  void move(int newX, int newY) {
    x=newX;
    y=newY;
  }
}
