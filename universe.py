#!/usr/bin/env python
import sys

class ISS:
  def __init__(self):
    self.hoge = 0

  def getInitialOrientation(self, beta):
    return 0
  
  def getStateAtMinute(self, minute):
    next = getNextMaxState();
    return flat(zip(next.getRotations(), 10*[0]));

class State:
  def __init__(self):
    self.rotations = 10*[0];
  
  def __init__(self, rotations):
    self.rotations = rotations;

  def getRotations():
    return self.rotations;

  def setRotations(rotations):
    self.rotations = rotations;

  def __str__(self):
    str(self.rotations);
    

def getSingleActions():
  def a1(r, v):
    return (r, 0);
  def a2(r, v):
    return (r+8.7, 0);
  def a3(r, v):
    return (r-8.7, 0);
  return [a1, a2, a3];

def getLegalActions():
  # return next action function for state using Generator
  def a1(state):
    return 1;
  return a1;

def evaluate(state):
  return 0;

def getNextMaxState():
  maxState = None
  for action in getLegalActions():
    nextState = action(state);
    score = evaluate(nextState);
  ####TODO
  return maxState

def flat(l):
  return [item for sublist in l for item in sublist];

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
