'''
This is the Markov module. This is a docstring
for the module.

>>> m = Markov('ab')
>>> m.predict('a')
'b'

'''
import random


class Markov:
    def __init__(self, txt, size=1):
        #import pdb;pdb.set_trace()
        #self.table = get_table(txt)
        self.tables = []
        for i in range(size):
            self.tables.append(get_table(txt, size=i+1))

    def predict(self, txt):
        #options = self.table.get(txt, {})
        table = self.tables[len(txt)-1]
        options = table.get(txt, {})
        if not options:
            raise KeyError(f'{txt} not found')
            # raise KeyError('{} not found'.format(txt))
        possibles = []  # list literal
        for key in options:
            count = options[key]
            for i in range(count):
                possibles.append(key)
        return random.choice(possibles)


def get_table(txt, size=1):
    """This is a function docstring. This
    function returns a transition table for txt.

    >>> get_table('ab')
    {'a': {'b': 1}}
    """
    results = {}
    for i in range(len(txt)):
        chars = txt[i:i+size]
        try:
            out = txt[i+size]
        except IndexError:
            break
        char_dict = results.get(chars, {})
        char_dict.setdefault(out, 0)
        char_dict[out] += 1
        results[chars] = char_dict
    return results

        
    
    
