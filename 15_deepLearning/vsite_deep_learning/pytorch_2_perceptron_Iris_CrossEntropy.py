#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import pandas as pd;
from scipy.stats import zscore
import torch as torch;
import numpy as np

np.random.seed(42)
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

#read in the dataset, convert to numpy
dfData = pd.read_csv('./iris.csv')

np_x=dfData[list(dfData.columns)[0:-1]].apply(zscore).to_numpy();
np_y = pd.get_dummies(dfData['variety']).to_numpy();

n_classes = 3;
n_features = np_x.shape[1];

x_train=np_x
y_train=np_y

#define pytorch linear binary classifier

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

# create tensor variables for data, we do not need gradient w.r.t. to them
t_x_train=torch.tensor(x_train,requires_grad=False,device=device);
t_y_train=torch.tensor(y_train,requires_grad=False,device=device);


#define starting value of weights W for gradient descent
init_std_dev = 0.01;
initialW=init_std_dev*np.random.randn(n_features,n_classes)

# create a PyTorch tensor variable for W and b. 
# we will be optimizing over W, b, finding their best value using gradient descent, so we need gradient enabled
W = torch.tensor(initialW,requires_grad=True,device=device);
b = torch.zeros((1,n_classes),requires_grad=True,device=device);


# this optimizer will do gradient descent for us
learning_rate = 0.001;
optimizer = torch.optim.SGD([W,b],lr=learning_rate)
#optimizer = torch.optim.Adam([W,b],lr=learning_rate)

n_epochs = 10000;
for i in range(n_epochs):
    # clear previous gradient calculations
    optimizer.zero_grad();
    
    # calculate model predictions
    linear_predictions = torch.matmul(t_x_train,W)+b
    activations = 1.0 / (1.0 + torch.exp(-linear_predictions));
    
    #normalize activations to form probability distribution over classes
    sum_activations = torch.sum(activations,dim=1,keepdim=True)
    normalized_activations = torch.div(activations, sum_activations);    
    
    #calculate crossentropy loss
    risk = -1.0 * torch.mean(torch.sum(torch.multiply(t_y_train,torch.log(normalized_activations)),dim=1 ) );

    #calculate gradients of risk w.r.t. W,b and propagate them back
    risk.backward();

    # use the gradient to change W, b
    optimizer.step();

    # calculate accuracy (on the training set!)
    true_class = np.argmax(t_y_train.detach().cpu().numpy(),axis=1)
    pred_class = np.argmax(activations.detach().cpu().numpy(),axis=1)
    accuracy = np.count_nonzero(true_class == pred_class)/pred_class.shape[0];
    error = 1.0 - accuracy;
    
    print(i,risk.item(),accuracy)
