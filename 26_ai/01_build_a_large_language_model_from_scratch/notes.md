# Build a Large Language Model (From Scratch)

## TODO

* study the code page 18 - 21
* additional 
    https://github.com/zeyadusf/LLMs-from-Scratch
    https://github.com/rasbt/LLMs-from-scratch
    https://www.youtube.com/playlist?list=PLPTV0NXA_ZSgsLAr8YCgCwhPIJNNtexWu

## Preface

Started with developing a model to detect the mood of a song based on its lyrics

## Understanding large language models

Large language models (LLMS) = deep neural network

Can process and generate text in ways that appear coherent and contextually relevant not that they possess human-like consciousness or comprehension

Deep learning advancements
* vast quantities of text data
* large scale training
* transformer architecture 

### What is an LLM ? 

LLM = deep neural networks trained on massive amounts of text data
"large" refers to the size of parameters (hundreds of billions parameters) and dataset

params are adjustable weights in the network that are optimized during training to predict the next word in a sequence

Deep learning does not require manual feature extraction

### Applications of LLMs
* translation
* generation/create of text (articles, novels, computer code) 
* sentiment analysis
* test summarizaiton
* power chatboots and virtual assistans (ChatGPT)
* effective knowledge retrieval
* answering questions of text

In short automation task that involves parsing and generating text

### Stages of building LLMs

Custom build LLMs can outperform general purpose LLMs

Process
* pretraining
    * train it on a large raw text (raw - without any labeling info)
    * for llm do develop broad understanding of language
    * result base/fundation model
* fine-tuning
    * train on smaller labeled data specific to task/domains
    * types
        * instruction fine tuning
        * classification fine tunning
    * instruction fine tuning labeled dataset consist of instruction and answer Example (query to translate a text with the corretly translated text)
    * classification fine tuning labeled data sets with class labels
    TODO: diff between tuning (what each data looks likes and has)
    
### Into the transformer architecture

Has encoder and decoder. Both consist of many layers connected by self-attention mechanism. Allows the model to weight the importance of different words or tokens in a sequence relative to each other. This enables to capture long range dependencies and contextual relationships within the data
Encoder processes the input ecnodes into numerical representations 
Decoder from endoed values generate output text

Variations
* BERT
* GPT

BERT (encoder segment) specialize in word prediction. Good for test classification including sentiment prediction and document categorizaiton. 

GPT (decoder segment) designed for generative tasks (generating text, translation, text summarization ). Good for zero-shot task (generalize to completely unseen tasks without prior examples). Good for few-shot task (minimal number of examples the user provides as input)

### GPT architecture

GPT models are pretrained on a relatively simple next-word prediction task 

Its architecture is just decoder part (no encoder). 

Type is autoregressive model. Previous output as input for future predictions
TODO what are othee types 

Larger than original transformer model GPT-3 96 transformer layers and 175 billion params

Form of self-supervised learning (self labeling / create label on the fly) so it is possible ot use massive unlabeled text datasets

### Building a LLM

Stage 1
* fundamental data processing 
    * data preparation and sampling
    * attention mechanism

Stage 2
* code and pretraing GPT-like LLM
    * training loop
    * model evaluation
    * load pretrained weights

Stage 3
* fine tune
    * classifier
    * personal assistant
    
## Working with text data    
