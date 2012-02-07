import sys

# E ::= T ((+|-) E)*
# T ::= S | S (*|/|^|%) T | "(" E ")"
# S ::= [0-9][0-9]*

class Parser:
  def __init__(self):
    self.text_ = '' 
    self.len_ = 0
    self.i_ = -1
    self.tok_ = ''
    self.vars_ = {}

  def skipSpace(self):
    while self.i_ < self.len_ and self.text_[self.i_].isspace():
      self.i_ += 1

  def nextToken(self):
    self.i_ += 1
    self.skipSpace()

    if self.i_ >= self.len_:
      self.tok_ = '$'
      return

    self.tok_ = self.text_[self.i_]

    if self.tok_.isalpha():
      self.tok_ = ''
      while self.i_ < self.len_ and self.text_[self.i_].isalnum():
        self.tok_ += self.text_[self.i_]
        self.i_ += 1
      self.i_ -= 1
    elif self.tok_.isdigit():
      self.tok_ = ''
      while self.i_ < self.len_ and self.text_[self.i_].isdigit():
        self.tok_ += self.text_[self.i_]
        self.i_ += 1
      self.i_ -= 1

  def error(self, msg):
    raise Exception(msg + ' at ' + str(self.i_))

  def expect(self, tok):
    if self.tok_ != tok:
      self.error('Expecting {0}, found {1}'.format(tok, self.tok_))
    self.nextToken()

  def parse(self, text):
    self.text_ = text
    self.len_ = len(self.text_)
    self.i_ = -1

    self.nextToken()

    if self.tok_ == 'let':
      self.nextToken()
      x = self.parseAssignment()
      while self.tok_ == ',':
        self.nextToken()
        x = self.parseAssignment()
    elif self.tok_ == '$':
      x = ''
    else:
      x = self.parseExpression()
    self.expect('$')
    return x

  def parseAssignment(self):
    name = self.tok_
    self.nextToken()
    self.expect('=')
    x = self.parseExpression()
    self.vars_[name] = x
    return str(self.vars_)

  def parseExpression(self):
    x = self.parseTerm()
    if self.tok_ == '+':
      self.nextToken()
      x += self.parseExpression()
    elif self.tok_ == '-':
      self.nextToken()
      x -= self.parseExpression()
    elif self.tok_ == '*':
      self.nextToken()
      x *= self.parseExpression()
    elif self.tok_ == '/':
      self.nextToken()
      x /= self.parseExpression()
    elif self.tok_ == '^':
      self.nextToken()
      x = x ** self.parseExpression()
    elif self.tok_ == '%':
      self.nextToken()
      x = x % self.parseExpression()
    return x

  def parseTerm(self):
    if self.tok_.isdigit() or self.tok_.isalnum():
      x = self.parseSymbol()
      if self.tok_ == "*":
        self.nextToken()
        x *= self.parseTerm()
      elif self.tok_ == '/':
        self.nextToken()
        x /= self.parseTerm()
      elif self.tok_ == '^':
        self.nextToken()
        x = x ** self.parseTerm()
    elif self.tok_ == "(":
      self.nextToken()
      x = self.parseExpression()
      self.expect(")")
    else:
      raise Exception("Unknown token " + self.tok_)
    return x

  def parseSymbol(self):
    if self.tok_.isdigit():
      x = int(self.tok_)
    elif self.tok_.isalnum():
      if self.tok_ not in self.vars_:
        self.error('Undefined variable ' + self.tok_)
      else:
        x = self.vars_[self.tok_]
    else:
      self.error('Expecting number of variable')
    self.nextToken()
    return x

def main():
  p = Parser()
  for line in sys.stdin:
    try:
      print(p.parse(line))
    except Exception as e:
      print(e)

if __name__ == "__main__":
  main()
