"""
  This file contains two classes: NFA and Parser, which together can be used
  to do regular expression matching.
"""
import sys

"""

  A class that simulates NFAs 
  Each State is a list of nodes
  Each Node is a list of edges
  Each Edge is a tuple of character and node index

"""
class NFA:
  def __init__(self, num_nodes, start_node, accepting_nodes):
    self.nodes_ = [[] for i in range(num_nodes)]
    self.start_ = start_node
    self.accepting_ = accepting_nodes
    self.state_ = [start_node]
    self.add_e_reachable(start_node, self.state_)

  def __str__(self):
    return 'start = {0} accepting = {1}\nnfa = {2}'.format(self.start_, self.accepting_, self.nodes_)

  def add_edge(self, *args, **kwargs):
    if len(args) >= 3:
      chars = args[2]
    else:
      chars = kwargs.get('chars', '\0')
    edge = (chars, args[1])
    node = self.nodes_[args[0]]
    if edge not in node:
      node.append(edge)

  # Add all possible e-reachable nodes from this node
  def add_e_reachable(self, node, state):
    for edge in self.nodes_[node]:
      if edge[0] == '\0' and edge[1] not in state:
        state.append(edge[1])
        self.add_e_reachable(edge[1], state)

  def step(self, ch):
    next_state = []

    # Add ch-reachable nodes
    for node in self.state_:
      for edge in self.nodes_[node]:
        if ch in edge[0]:
          if edge[1] not in next_state:
            next_state.append(edge[1])

    # Add all e-reachable nodes
    for node in next_state:
      self.add_e_reachable(node, next_state)

    print('{0} => {1}'.format(self.state_, next_state))
    self.state_ = next_state

  def match(self, string):
    self.state_ = [self.start_]
    self.add_e_reachable(self.start_, self.state_)
    i = 0
    while i < len(string) and len(self.state_) > 0:
      self.step(string[i])
      i += 1
    if i == len(string):
      for node in self.state_:
        if node in self.accepting_:
          return True
    return False

  def concat(self, nfa):
    offset = len(self.nodes_)
    # insert nodes with adjusted index
    new_nodes = []
    for node in nfa.nodes_:
      new_node = []
      for edge in node:
        new_node.append((edge[0], edge[1] + offset))
      new_nodes.append(new_node)
    self.nodes_.extend(new_nodes)
    # insert edges from original accepting to start of second nfa
    for node in self.accepting_:
      self.add_edge(node, offset + nfa.start_)
    # accepting states are now accepting of second nfa
    new_accepting = [offset + node for node in nfa.accepting_]
    self.accepting_ = new_accepting

  # Corresponding to ? operator
  def optional(self):
    for node in self.accepting_:
      self.add_edge(self.start_, node)

  def kleene_star(self):
    # Create two extra nodes and add epsilon transitions
    self.nodes_.extend([[], []])
    first_node = len(self.nodes_) - 2
    last_node = len(self.nodes_) - 1
    self.add_edge(first_node, self.start_)
    self.add_edge(first_node, last_node)
    for node in self.accepting_:
      self.add_edge(node, last_node)
      self.add_edge(node, self.start_)
    self.accepting_ = [last_node]
    self.start_ = first_node

  def union(self, nfas):
    if len(nfas) == 0:
      return

    new_nodes = []
    offset = len(self.nodes_)
    for nfa in nfas:
      nfa.offset = offset
      for node in nfa.nodes_:
        new_nodes.append([(edge[0], edge[1] + offset) for edge in node])
      offset += len(nfa.nodes_)

    print(new_nodes)
    self.nodes_.extend(new_nodes)
    self.nodes_.extend([[], []])
    first_node = len(self.nodes_) - 2
    last_node = len(self.nodes_) - 1

    # Connect new start node to all start nodes
    self.add_edge(first_node, self.start_)
    for nfa in nfas:
      self.add_edge(first_node, nfa.start_ + nfa.offset)
    # Connect all finish nodes to new finish node
    for node in self.accepting_:
      self.add_edge(node, last_node)
    for nfa in nfas:
      for node in nfa.accepting_:
        self.add_edge(node + nfa.offset, last_node)
    self.accepting_ = [last_node]
    self.start_ = first_node

"""
 A parser for regular expressions based on the following grammar:
 R ::= E$
 E ::= A ('|' A)*
 A ::= B (B)*
 B ::= C ('*' | '+' | '?')?
 C ::= '(' E ')' | T
 T ::= id | '[' R (R)* ']'
 R ::= id ('-' id)?

"""
class Parser:
  def __init__(self):
    self.text_ = '' 
    self.len_ = 0
    self.pos_ = -1
    self.tok_ = ''
    self.special_ = '()|*+?[]-$'

  def skip_space(self):
    while self.pos_ < self.len_ and self.text_[self.pos_].isspace():
      self.pos_ += 1

  def next_token(self):
    self.pos_ += 1
    self.skip_space()

    if self.pos_ >= self.len_:
      self.tok_ = '$'
      return

    self.tok_ = self.text_[self.pos_]

  def error(self, msg):
    raise Exception(msg + ' at ' + str(self.pos_))

  def expect(self, tok):
    if self.tok_ != tok:
      self.error('Expecting {0}, found {1}'.format(tok, self.tok_))
    self.next_token()

  def parse(self, text):
    self.text_ = text
    self.len_ = len(self.text_)
    self.pos_ = -1

    self.next_token()
    nfa = self.parse_expression()
    self.expect('$')
    return nfa

  # E = A ('|' A)*
  def parse_expression(self):
    nfa = self.parse_a()
    choices = []
    while self.tok_ == '|':
      self.next_token()
      choices.append(self.parse_a())
    nfa.union(choices)
    return nfa

  # A ::= B (B)*
  def parse_a(self):
    nfa = self.parse_b()
    while self.tok_ not in self.special_ or self.tok_ in '([':
      nfa.concat(self.parse_b())
    return nfa

  # B ::= C ('*' | '+' | '?')?
  def parse_b(self):
    # Starters[C] = id, (, [
    if self.tok_ not in self.special_ or self.tok_ in '([':
      nfa = self.parse_c()
      if self.tok_ == '*':
        self.next_token()
        nfa.kleene_star()
      elif self.tok_ == '+':
        self.next_token()
        self.error('+ operator not implemented yet!')
      elif self.tok_ == '?':
        self.next_token()
        nfa.optional()
    else:
      self.error('Expecting id, ( or [')
    return nfa

  # C ::= '(' E ')' | T
  def parse_c(self):
    if self.tok_ == '(':
      self.next_token()
      nfa = self.parse_expression()
      self.expect(')')
    # Starters[T] = id, '['
    elif self.tok_ not in self.special_ or self.tok_ in '[':
      nfa = self.parse_terminal()
    else:
      self.error('Unknown token ' + self.tok_)
    return nfa

  # T = id | "[" R(R)* "]"
  def parse_terminal(self):
    if self.tok_ == '[':
      chars = ''
      self.next_token()
      while self.tok_ != ']':
        chars += self.parse_range()
      chars = ''.join(set(chars))
      self.expect(']')
    else:
      chars = self.tok_
      self.next_token()
    # Make an nfa that acccepts these chars
    nfa = NFA(2, 0, [1])
    nfa.add_edge(0, 1, chars)
    return nfa

  # R = id ('-' id)?
  def parse_range(self):
    start = self.tok_
    self.next_token()
    if self.tok_ == '-':
      self.next_token()
      end = self.tok_
      ch = start
      start = ''.join([chr(x) for x in range(ord(start), ord(end)+1)])
    return start

def main():
  parser = Parser()
  #nfa = parser.parse('[a-zA-Z][a-zA-Z0-9]*')
  # Floating point numbers
  nfa = parser.parse('(([0-9][0-9]*(.[0-9]*)?) | ([0-9]*.[0-9][0-9]*))')
  #nfa = parser.parse('[cd]*')
  print(nfa)

  for line in sys.stdin:
    print(nfa.match(line.strip()))

if __name__ == "__main__":
  main()

