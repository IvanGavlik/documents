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

In pretraining stage LLMs preocess text one word at a time
Input text
1. split4 The [0.1 0.0 0.1] into individual word and subword tokens
2. encode into vector representations

### Word embeddings

Convering data (text, video, audio) into vector format (to have mathematical operations to train neural network) is called embedding

Embedding mapping from discrete objects (words/images..) into points in continous vector space

Embeddings
* word
* sentences, paragraphs -> retrieval-augmented generation
* documents

Word embeddings
* diff algorithms
* popular Word2Vec approach
    * words that appear in similar context have similar meanings
    * predict the context of a word given the target and vice versa
    * higher dimensionality captures more relationships but cost is computational efficiency
    
LLMs can produce their own embedding that are part of the input layer ant then updated during training - optimized for specific task

Steps
* split text into words
* words into tokens
* tokens into embedding vectors

### Tokenizing text

Token - individual  word, special character, punctuation character
* Use split fn to split a text on whitespace characters
* keep text capitalized
* split text also on commas, periods, punctuation characters (question marks, quotation marks..) 
* remove whitespace charaters (deepens on the app - space needed in code text)

### Tokens into token IDs

Create Vocabulary 
* token (uniqe word/special character) -  integer representation 
* sorted alphaberically
* it containes only words form input text

Convert new text into token IDs
* Input: I am Ivan I come to Output 10 12 45 10 345
* also have method to do vice versa (encode and decode) 

### Adding special context tokens

Goal handle unknown words
Special tokens include markers for unknown words, document boundaries
<|unk|> - for any word that is not part of the vocabulary 
<|endoftext|> - between unrelated texts

Additional special tokens
* [BOS] beginning of sequence (start of a text)
* [EOS] end of sequence (end of a text)
* [PAD] paddding (shorter text are extended)

In GPT models we dont use special context tokens but pair encoding tokenization 
to solve problem of unknown words

### Byte pair encoding

BPE algorithm search for library complicated to implement
* <|endoftext|> large token ID (ChatGPT-3 has vocabulary size of 50,257) 
* does not use <|unk|> for unknown word
* breaks down words into smaller subword or even individual charactes so unknown word is represented as sequence of subword tokens by iteratively merging frequent charactes into subwords

### Data sampling with a sliding window

Input-target pairs
Note LLMs are pretrained by predicting the next word in a text

Text sample: LLMs learn to predict one word at a time
Input block for LLMs learn
LLM has to predict next word

Steps
* tokenize text using BPE tokenizer
* x - input tokens y - targets (inputs shifted by 1) 
* do this for whole text and return inputs and targets as PyTorch tensors 
* we have input tensor(input) and target (what has tp be predicted)

max-length (content)
* how much tokens into input vector 
* assigned to input tensor
* common to train with size at least 256

stride
* number of positions the inputs shift across bathes, emulating a sliding window approach

batch-size
* small batch sizes require less memory but lead to more noisy model

### Creating token embeddings

Token embedding is bridge (computers works with numbers so re represent model as vectors with n dimension (each dimensions represents once concept)

The -> 4
cat -> 15
eats -> 8
fish -> 21

Model ids -> 4, 15, 9, 21

Each token hast its embedding table (n dimensions)
ID TOKEN EMBEDDING
------------------
4  The [0.1 0.0 0.1]
15 cat [0.8 0.2 0.6]

Final output (tensor) for attention layers
[
  [0.1, 0.0, 0.1],   ← "The"
  [1.0, 0.9, 0.1],   ← "cat"
  [0.8, 0.2, 0.6],   ← "eats"
  [0.9, 1.0, 0.3]    ← "fish"
]
(sequence_length, embedding_dim) = (4, 3)

Convert token IDs into embedding vectors and initialize its weight with random values (starting point for learning process)

Use vocab_size and output_dim to instantiate an embedding layer

vocab_size
* one row for each of the possible token in the vocabulary
* if you have 50,000 vocabulary you have 50,000 rows 

output_dim
* number of colums (embedding dimensions)
* how much colums to have 

#### How there embedding learned 

Initially:
* Random numbers

During training:
* Model makes prediction
* Loss is computed
* Gradients flow backward
* Embedding vectors shift

After training:
* Similar words cluster together
* Useful relationships emerge

No rules. Just optimization.

### Encoding word positions

Order is agnostics
"cat eats fish"
"fish eats cat"
- look identical to attention if we dont add positions


Dont have notion of position or order

* absolute positional embeddings: specific position in a sequence (at which position) used by GPT (optimized during tranning process)

* relative positional embeddings: distance between tokens (how far apart) model can generalize better to sequences of varying lengths    

* rotary positional embeddings (RoPE)
   TODO rotary positional embeddings (RoPE)
   TODO

#### Absolute positional embeddings

Each position gets its own vector
E =  [
  [0.1, 0.0, 0.1],   ← "The"
  [1.0, 0.9, 0.1],   ← "cat"
  [0.8, 0.2, 0.6],   ← "eats"
  [0.9, 1.0, 0.3]    ← "fish"
] embeddings

Positions embeddings 
P = [
  p_0,
  p_1,
  p_2,
  p_3
]

Results: Combine them they are added
X = E + P 

Pros / Cons
* simple (same mechanisam no change to attention math easy to implement)
* hard limit on sequence length (if position embedding table has length 512) the the size is fixed - you can not encode longer sequences without changing the model* absolute positional embeddings: specific position in a sequence (at which position) used by GPT (optimized during tranning process)

* don+t generalize well to longer texts 
this is apsolute position (subject on the start of the sentence) so you encode identity not relationship
* model learns Token x at position 5 not Token X before token Y

Input embedding pipeline
input text -> tokenized text -> token ids -> token embeddings -> positional embeddings -> input embeddings -> GPT like decoder (only transformer)

#### Relative positional embeddings (modern approach)

Positions are encoded during attention, not added to embeddings: 
Instead of: "The token is at position 5" We say: "This token is 3 steps away form me"
    * language care more about distance direction ordering 

when computing attention we modify it to include relative positions
score: Q x K - Q + Q * R(position_difference)

score(i, j) = similarity(query_i, key_j) + position_bias(i - j)

Language is fundamentally relative:
* subject before verb
* adjective before noun
* closing parenthesis after opening one

Pros/Cons
* generalize for longer sequences/context (we depend on distance, we encode patterns not coordinates=
* more complex because calculated at attention level attention_score(i, j) += bias(i - j) so position afffect evety attention pair not just token creation increases code complexity memory use: you need distance matrices 
* harder to optimize not good for autoregresive LLMs long running converstaions

## Coding attention mechanisms

TODO 
* Rotary positional embeddings (RoPE) from chat gpt
* chat gpt to explain transformer architecture why we only use decoder what is then encoder user (should we use both) what is big picture
    * Starting quesion LLMs are good because we have 
        * vast quantities of text data
        * large scale training
        * transformer architecture 
       explain me transformer architecture
* how does Data sampling with a sliding window come into picture also how comes into picture with attention mechanisms     
    
*  RAG vs. fine-tuning: Choosing the right method for your LLM
When setting up a language model for specific tasks, two key approaches often come into play: retrieval augmented generation (RAG) and fine-tuning. Each method brings distinct advantages, and choosing the right one is crucial for the model’s performance in targeted environments.
from https://www.superannotate.com/blog/rag-vs-fine-tuning?fbclid=IwY2xjawO1_spleHRuA2FlbQIxMABicmlkETBnS2JNVXhjNzdseExXVHlsc3J0YwZhcHBfaWQQMjIyMDM5MTc4ODIwMDg5MgABHo_qauoxHaKrJugWh9J4gZWezRV_vMWaKXJ05Tbt4Tn5NOkYaQ_e8W1cW29N_aem_yPrLRUSsj49wBC2uBHPwpQ
