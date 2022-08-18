#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd;
from scipy.stats import zscore
import torch as torch;

import numpy as np

import torchvision.datasets as datasets


np.random.seed(42)
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')


#read in the dataset, convert to numpy

n_classes = 10;
n_features = 28*28;

full_train_dataset = datasets.MNIST(root='./data', train=True, download=True, transform=None)
full_test_dataset = datasets.MNIST(root='./data', train=False, download=True, transform=None)

x_train = full_train_dataset.data.numpy().reshape(-1,n_features).astype(dtype=np.float)/255.0;
x_test = full_test_dataset.data.numpy().reshape(-1,n_features).astype(dtype=np.float)/255.0;

y_train_cat = full_train_dataset.targets.numpy()
y_test_cat = full_test_dataset.targets.numpy()

dd_y=pd.DataFrame(data=y_train_cat)
y_train=pd.get_dummies(dd_y,columns = list(dd_y.columns)).to_numpy().astype(dtype=np.float);

dd_y=pd.DataFrame(data=y_test_cat)
y_test=pd.get_dummies(dd_y,columns = list(dd_y.columns)).to_numpy().astype(dtype=np.float);


# permute the dataset
permutation = np.random.permutation(x_train.shape[0]);
x_train=x_train[permutation,:]
y_train=y_train[permutation,:]

# select smaller subset (out of 50000)
subset_size = 50000;
x_train=x_train[:subset_size,:]
y_train=y_train[:subset_size,:]


n_train=x_train.shape[0];

# create tensor variables for data, we do not need gradient w.r.t. to them
t_x_test=torch.tensor(x_test,requires_grad=False,device=device);
t_y_test=torch.tensor(y_test,requires_grad=False,device=device);


# number of activations (neurons) in the hidden layer
n_hidden = 100;


# create weights for layer 1 (W1, b1) and layer 2 (W2, b2)
initialW1=0.01*np.random.randn(n_features,n_hidden)
initialW2=0.01*np.random.randn(n_hidden,n_classes)

W1 = torch.tensor(initialW1,requires_grad=True,device=device);
b1 = torch.zeros((1,n_hidden),requires_grad=True,device=device);

W2 = torch.tensor(initialW2,requires_grad=True,device=device);
b2 = torch.zeros((1,n_classes),requires_grad=True,device=device);


# this optimizer will do gradient descent for us
# experiment with learning rate and optimizer type
learning_rate = 0.0001;
# note that we have to add all weights&biases, for both layers, to the optimizer
optimizer = torch.optim.SGD([W1,b1,W2,b2],lr=learning_rate)
#optimizer = torch.optim.Adam([W1,b1,W2,b2],lr=learning_rate)

# experiment with batch size (small batch size needs small learning rate)
batch_size=32;
n_epochs = 30;

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

        # clear previous gradient calculations
        optimizer.zero_grad();
        
        # calculate model predictions
        first_layer = torch.matmul(t_x_train,W1)+b1
        activations_first_layer = 2.0 / (1.0 + torch.exp(-first_layer)) -1.0;

        # done with first layer, it will serve as input to the second layer
        second_layer = torch.matmul(activations_first_layer,W2)+b2
        activations_second_layer = 1.0 / (1.0 + torch.exp(-second_layer));
        # done with second layer

        # but we need to normalize it (get softmax) for cross-entropy loss/risk
        sum_activations = torch.sum(activations_second_layer,dim=1,keepdim=True)
        normalized_activations = torch.div(activations_second_layer, sum_activations);
        risk = -1.0 * torch.mean(torch.sum(torch.multiply(t_y_train,torch.log(normalized_activations)),dim=1 ) );

        # calculate gradients
        risk.backward();
        
        # take the gradient step
        optimizer.step();
        

        batch_risk=risk.item();

        true_class = np.argmax(t_y_train.detach().cpu().numpy(),axis=1)
        pred_class = np.argmax(normalized_activations.detach().cpu().numpy(),axis=1)
        accuracy += np.count_nonzero(true_class == pred_class);
        
    # after all the batches in this epoch are done, we calculate test set risk and accuracy
    # we don't need gradients, so we turn them off
        
    with (torch.no_grad()):
        first_layer = torch.matmul(t_x_test,W1)+b1
        activations_first_layer = 2.0 / (1.0 + torch.exp(-first_layer)) -1.0;
        second_layer = torch.matmul(activations_first_layer,W2)+b2
        activations_second_layer = 1.0 / (1.0 + torch.exp(-second_layer));
        sum_activations = torch.sum(activations_second_layer,dim=1,keepdim=True)
        test_normalized_activations = torch.div(activations_second_layer, sum_activations);
        
        #calculate loss

        test_risk = -1.0 * torch.mean(torch.sum(torch.multiply(t_y_test,torch.log(test_normalized_activations)),dim=1 ) );

        test_true_class = np.argmax(t_y_test.detach().cpu().numpy(),axis=1)
        test_pred_class = np.argmax(test_normalized_activations.detach().cpu().numpy(),axis=1)
        test_accuracy = np.count_nonzero(test_true_class == test_pred_class)/test_pred_class.shape[0];
        test_error = 1.0 - test_accuracy;
        
    accuracy = accuracy / float(x_train.shape[0])
    print(i,batch_risk,test_risk.item(),accuracy,test_accuracy)



