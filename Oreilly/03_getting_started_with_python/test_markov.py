import unittest

import markov
#from markov import Markov

class TestMarkov(unittest.TestCase):
    def test_predict(self):
        m = markov.Markov('ab')
        res = m.predict('a')
        self.assertEqual(res, 'b')

    def test_predict2(self):
        m = markov.Markov('abc', size=2)
        res = m.predict('ab')
        self.assertEqual(res, 'c')

    def test_get_table(self):
        res = markov.get_table('abacab')
        self.assertEqual(res,
            {'a': {'b': 2, 'c': 1},
             'b': {'a': 1},
             'c': {'a': 1}})

    def test_get_table2(self):
        res = markov.get_table('abacab', size=2)
        self.assertEqual(res,
            {'ab': {'a': 1},
             'ba': {'c': 1},
             'ac': {'a': 1},
             'ca': {'b': 1}})
        

if __name__ == '__main__':
    # I am executing this file
    unittest.main()
else:
    print('loading')
    
