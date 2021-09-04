package taxables;

import java.util.ArrayList;
import java.util.List;

public class TaxPreparer {
  public static void prepareTaxes(Taxable t) {}
  // List<?> is shorthand for List<? extends Object>
  // ? extends "covariance"
  public static void prepareBulkTaxes(List<? extends Taxable> lt) {
    for (Taxable t : lt) { // Taxable t = lt.get(index);
      prepareTaxes(t);
    }
    // ALL Prohibited
//    lt.add(new Corporation());
//    lt.add(new Individual());
  }

  // ? super "contravariance"
  public static void getClients(List<? super Individual> li) {
//    Individual i = li.get(0);
    li.add(new Individual());
  }

  public static void main(String[] args) {
    List<Taxable> clients = new ArrayList<>();
    clients.add(new Corporation());
    clients.add(new Corporation());
    clients.add(new Individual());
    getClients(clients);

    List<Individual> joesClients = new ArrayList<>();
    getClients(joesClients);
//    joesClients.add(new Corporation());
    joesClients.add(new Individual());
    joesClients.add(new Individual());

    prepareBulkTaxes(clients);
    prepareBulkTaxes(joesClients);
  }
}
