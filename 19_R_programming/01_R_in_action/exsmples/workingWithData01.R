manager <- c(1, 2, 3, 4, 5)
date <- c("10/24/08", "10/28/08", "10/1/08", "10/12/08", "5/1/09")
country <- c("US", "US", "UK", "UK", "UK")
gender <- c("M", "F", "F", "M", "F")
age <- c(32, 45, 25, 39, 99)
q1 <- c(5, 3, 3, 3, 2)
q2 <- c(4, 5, 5, 3, 2)
q3 <- c(5, 2, 5, 4, 1)
q4 <- c(5, 5, 5, NA, 2)
q5 <- c(5, 5, 2, NA, 1)
leadership <- data.frame(manager, date, country, gender, age,
                         q1, q2, q3, q4, q5, stringsAsFactors=FALSE)

#Creating new variables
x1 <- c(1, 1, 1)
x2 <- c(2, 2, 2)
mydata <- data.frame(x1, x2)

sumx <- mydata$x1 + mydata$x2
maan <- (mydata$x1 + mydata$x2) / 2

mydata$sumx <- mydata$x1 + mydata$x2
mydata$maan <- (mydata$x1 + mydata$x2) / 2

mydata2 <- transform(mydata, sum = x1 + x2, meanx = (x1 + x2)/2)
mydata2 <- transform(mydata, sum = x1 + x2, meanx = (x1 + x2)/23)

# Creating new variables bas on condition - recode
newLeadership <- within(leadership, {
  agecat <- NA
  agecat[age > 75] <- "Elder"
  agecat[age >= 55 & age <= 75] <- "Middle"
  agecat[age < 55] <- "Young"
  agecat <- factor(agecat)
})

# rename variable
names(newLeadership)[2] <- "testDate"
newLeadership

#missing values
y <- c(1, 2, 3, NA)
is.na(y)


newData <- na.omit(leadership) #delete rows with null
newData

# To merge two data frames (datasets) horizontally, you use the merge() function.
dA <- data.frame(a1 = c(1, 2, 3, 4), b1 = c(1, 2, 3, 4))
dB <- data.frame(a1 = c(1, 3, 3), b1 = c(1, 2, 3))
total <- merge(dA, dB, by="a1")
total

# adding rows to a data frame
dC <- data.frame(a1 = c(5), b1 = c(5))
total2 <- rbind(dC, dA)
total2

#selecting variables 
selectvars <- c("q1", "q2", "q3")
newData2 <- leadership[selectvars]
newData2

#excluding (droping) variables
newData3 <- leadership[!(names(leadership) %in% c("q3", "q4"))]
#sve osim q3 i q4
newData3

#selecting observations
leadership[1:3,]
leadership[leadership$gender=="M" & leadership$age > 30]

newData4 <- subset(leadership, 
                   age >= 25,
                   select = c(q1, q2))
newData4

# sql for manulupation of data frames
install.packages("sqldf")
library(sqldf)
newdf <- sqldf("select * from mtcars where carb=1 order by mpg",
               row.names=TRUE)