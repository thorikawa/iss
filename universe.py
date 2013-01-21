#!/usr/bin/env python
import sys

class ISS:
  def __init__(self):
    self.hoge = 0

  def getInitialOrientation(self, beta):
    return 0
  
  def getStateAtMinute(self, minute):
    return 20*[0]


if __name__=='__main__':
  beta = input()
  iss = ISS()
  yew = iss.getInitialOrientation(beta)
  print yew
  sys.stdout.flush()
  for i in range(92):
    print 1
    ret = iss.getStateAtMinute(i)
    for v in ret:
      print v
    sys.stdout.flush()
  