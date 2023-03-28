# Za određivanje optimalnog broja redova i širinu intervala numeričkih skupina

# za broj redova
# k = 1 + 3.3 log N
# n - broj elemenata
sturges_rule <- function(n) {
  n <- (1 + 3.3 * log(n, base = 10))
  return (round(n))
}

# za širinu intervala
# delta x = (xmax - xmin) / k 
sturges_rule_interval <- function(xamx, xmin, k) {
  n <- (xamx - xmin) / k
  return (round(n))
}