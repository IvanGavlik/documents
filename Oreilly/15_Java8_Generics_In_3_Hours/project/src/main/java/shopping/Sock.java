package shopping;

public class Sock implements Colored, Sized {
  private int size;
  private String color;

  public Sock(int size, String color) {
    this.size = size;
    this.color = color;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  @Override
  public String toString() {
    return "Sock{" +
        "size=" + size +
        ", color='" + color + '\'' +
        '}';
  }
}
