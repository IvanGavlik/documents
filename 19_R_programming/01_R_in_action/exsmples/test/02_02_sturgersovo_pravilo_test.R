# TODO https://www.geeksforgeeks.org/unit-testing-in-r-programming/

source('02_02_sturgersovo_pravilo.R')

expect <- function(input1, input2) {
  return (input1 == input2)
}

result <- sturges_rule(22)
expect(5, result)


result <- sturges_rule_interval(64, 21, sturges_rule(22))
expect(9, result)