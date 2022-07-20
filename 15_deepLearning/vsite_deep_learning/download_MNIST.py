#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import torchvision.datasets as datasets
full_train_dataset = datasets.MNIST(root='./data', train=True, download=True, transform=None)
full_test_dataset = datasets.MNIST(root='./data', train=False, download=True, transform=None)

