package eu.javaspecialists.courses.datastructures.ch5_hashing;
//                             ###       ###        ####
public record PhoneNumber(int area, int prefix, int extension) {
  @Override
  public int hashCode() {
    return area * 1_000_0000 + prefix * 1_0000 + extension;
  }
}
