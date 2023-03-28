rod <- c("M", "Z")
rod <- factor(rod)

f <- data.frame(rod, frekencija = c(8789, 12180))

f <- transform(f, 
               proprcije = round(frekencija/sum(frekencija), digits = 4)
               )

f <- transform(f,
               postotci = round(proprcije * 100, digits = 4)
               )
f

f