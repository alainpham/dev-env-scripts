package demo;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import restsvc.model.Beer;

/**
 * @author laurent
 */
@Component
public class BeerRepository {

   private static Map<String, Beer> beers = new HashMap<>();

   static {
      beers.put("Rodenbach", new Beer("Rodenbach", "Belgium", "Brown ale", new BigDecimal(4.2), "available"));
      beers.put("Westmalle Triple", new Beer("Westmalle Triple", "Belgium", "Trappist",  new BigDecimal(3.8f), "available"));
      beers.put("Weissbier", new Beer("Weissbier", "Germany", "Wheat", new BigDecimal(4.1), "out_of_stock"));
      beers.put("Orval", new Beer("Orval", "Belgium", "Trappist", new BigDecimal(4.3), "available"));
      beers.put("Rochefort", new Beer("Rochefort", "Belgium", "Trappist", new BigDecimal(4.1), "out_of_stock"));
      beers.put("Floreffe", new Beer("Floreffe", "Belgium", "Abbey", new BigDecimal(3.4), "out_of_stock"));
      beers.put("Maredsous", new Beer("Maredsous", "Belgium", "Abbey", new BigDecimal(3.9), "available"));
      beers.put("Pilsener", new Beer("Pilsener", "Germany", "Pale beer", new BigDecimal(3.2), "available"));
      beers.put("Bock", new Beer("Bock", "Germany", "Dark beer", new BigDecimal(3.7), "available"));
   }

   public static List<Beer> getBeers() {
      return new ArrayList<>(beers.values());
   }

   public static Beer findByName(String name) {
      if (beers.containsKey(name)) {
         return beers.get(name);
      }
      return null;
   }

   public static List<Beer> findByStatus(String status) {
      List<Beer> results = beers.values().stream()
            .filter(beer -> beer.getStatus().equals(status))
            .collect(Collectors.toList());

      return results;
   }
}
