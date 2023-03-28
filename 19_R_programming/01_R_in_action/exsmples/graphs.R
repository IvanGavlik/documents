dev.next() # graph in new window
attach(mtcars)
plot(wt, mpg)
title("PLot of MPG on Weight")
detach()

dev.next()
attach(mtcars)
plot(wt, mpg)
title("PLot of MPG on Weight 2")
detach()

# Simple example
dev.next()
dose <- c(20, 30, 40, 45, 60)
drugA <- c(16, 20, 27, 40, 60)
drugB <- c(15, 18, 25, 31, 40)
plot(dose, drugA, type="b")
help(pplot) #see type param

# Graphical parameters
dev.next()
plot(dose, drugA, type="b")
# Adding the no.readonly=TRUE option produces a list of current graphical set-tings that can be modified
par(lty=2, pch=17)

# Combinig graphs
attach(mtcars)
opar <- par(no.readonly = TRUE)
par(mfrow=c(2, 2)) # create matrix of rows that are filed in by row
plot(wt, mpg, main="wt vs mpg")
plot(wt, disp, main="wt vs disp")
hist(wt, main="histogram wt")
boxplot(wt, main="boxplot of wt")
par(opar)
detach(mtcars)