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

#randomly permute the samples (rows)
permutation = np.random.permutation(np_x.shape[0]);
np_x=np_x[permutation,:]
np_y=np_y[permutation,:]

#split into training and test set
pct_train = 0.75
n_train=int(np.floor(pct_train * np_x.shape[0]))
x_train=np_x[0:n_train,:]
x_test=np_x[n_train:,:]
y_train=np_y[0:n_train,:]
y_test=np_y[n_train:,:]

# create tensor variables for data, we do not need gradient w.r.t. to them
t_x_test=torch.tensor(x_test,requires_grad=False,device=device);
t_y_test=torch.tensor(y_test,requires_grad=False,device=device);


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
learning_rate = 0.0001;
optimizer = torch.optim.SGD([W,b],lr=learning_rate)
#optimizer = torch.optim.Adam([W,b],lr=learning_rate)

batch_size=20;
n_epochs = 10000;
for i in range(n_epochs):

    #permute the training set for each epoch
    #doing this on CPU. Alterntively, might be done on GPU, on the tensor version of x_train, y_train
    permutation = np.random.permutation(x_train.shape[0]);
    x_train=x_train[permutation,:]
    y_train=y_train[permutation,:]

    accuracy = 0.0;

    #go through the training set in batches of size batch_size
    for j in range(0,n_train,batch_size):
        t_x_train = torch.tensor(x_train[j:j+batch_size,:],requires_grad=False,device=device);
        t_y_train = torch.tensor(y_train[j:j+batch_size,:],requires_grad=False,device=device);

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

        
        batch_risk=risk.item();

        true_class = np.argmax(t_y_train.detach().cpu().numpy(),axis=1)
        pred_class = np.argmax(activations.detach().cpu().numpy(),axis=1)
        accuracy += np.count_nonzero(true_class == pred_class);
        
    # after all the batches in this epoch are done, we calculate test set risk and accuracy
    # we don't need gradients, so we turn them off
        
    with (torch.no_grad()):
        test_linear_predictions = torch.matmul(t_x_test,W)+b
        test_activations = 1.0 / (1.0 + torch.exp(-test_linear_predictions));
        test_sum_activations = torch.sum(test_activations,dim=1,keepdim=True)
        
        #calculate loss

        test_normalized_activations = torch.div(test_activations, test_sum_activations);
        test_risk = -1.0 * torch.mean(torch.sum(torch.multiply(t_y_test,torch.log(test_normalized_activations)),dim=1 ) );

        test_true_class = np.argmax(t_y_test.detach().cpu().numpy(),axis=1)
        test_pred_class = np.argmax(test_activations.detach().cpu().numpy(),axis=1)
        test_accuracy = np.count_nonzero(test_true_class == test_pred_class)/test_pred_class.shape[0];
        test_error = 1.0 - test_accuracy;
        
    accuracy = accuracy / float(x_train.shape[0])
    print(i,batch_risk,test_risk.item(),accuracy,test_accuracy)



