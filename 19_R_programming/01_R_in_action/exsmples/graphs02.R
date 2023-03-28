# A bar plot displays the distribution (frequency) of a categorical variable through verti-
#cal or horizontal bars.
data2 <- c(42, 14, 28)
fac <- c('None', 'Some', 'Marked')
barplot(height = data2, names.arg = fac, horiz = TRUE)

hist(mtcars$mpg, breaks = 12, col='red')
rug(jitter(mtcars$mpg))
lines(density(mtcars$mpg), col="blue", lwd=2)


x <- mtcars$mpg
h<-hist(x,
        breaks=12,
        col="red",
        xlab="Miles Per Gallon",
        main="Histogram with normal curve and box")
xfit<-seq(min(x), max(x), length=40)
yfit<-dnorm(xfit, mean=mean(x), sd=sd(x))
yfit <- yfit*diff(h$mids[1:2])*length(x)
lines(xfit, yfit, col="blue", lwd=2)
box()

myvars <- c("mpg", "hp", "wt")
head(mtcars[myvars])

summary(mtcars[myvars])


library(Hmisc)
describe(mtcars[myvars])