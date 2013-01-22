#!/usr/bin/env python

import sys

class ISS:
  def __init__(self):
    self.hoge = 0

  def getInitialOrientation(self, beta):
    return 0
  
  def getStateAtMinute(self, minute):
    next = getNextMaxState()
    return flat(zip(next.getRotations(), 10*[0]))

class Learner:
  def __init__(self, beta, yaw):
    self.beta = beta
    self.yaw = yaw

  def learn(initialState):
    state = initialState
    for i in range(92):
      if i==0:
        pass
      else:
        state = getNextMaxState(state)
      proceed(state)

  # return the score of 696 input values
  def evaluate(input):
    cos = input[0:8]
    stringShadow = input[8:664];
    longeronShadow = input[664:696];
    
    pass
  
  # library method call
  # @return 
  # The cosine of the angle between the blanket normal and the vector to the sun for each SAW (8 values).
  # The shadow fraction of each string (8 SAWs x 2 blankets x 41 strings = 656 values).
  # The shadow fraction of each longeron (8 SAWs x 4 longerons = 32 values).
  def library(state, minute):
    alpha = 360.0*minute/92.0
    beta = self.beta
    yaw = self.yaw
    print 2
    print 0
    print alpha
    print beta
    print yaw
    for rot in state.getRotations():
      print rot
    flush()
    ret = []
    for i in xrange(696):
      tmp = int(raw_input())
      ret.append(tmp)
    return ret

  def getNextMaxState(state):
    maxState = None
    maxScore = 0
    for action in getLegalActions():
      nextState = action(state)
      score = evaluate(nextState)
    if maxState is None or maxScore < score:
      maxScore = score
      maxState = nextState
    return maxState

class State:
  def __init__(self):
    self.rotations = 10*[0]
  
  def __init__(self, rotations):
    self.rotations = rotations

  def getRotations():
    return self.rotations

  def setRotations(rotations):
    self.rotations = rotations

  def getVelocities():
    return [0]*10

  def __str__(self):
    str(self.rotations)

def flush():
  sys.stdout.flush()

def proceed(state):
  print 1
  ret = flat(zip(state.getRotations(), state.getVelocities()))
  for v in ret:
    print v
  flush()

def getSingleActions():
  def a1(r, v):
    return (r, 0)
  def a2(r, v):
    return (r+8.7, 0)
  def a3(r, v):
    return (r-8.7, 0)
  return [a1, a2, a3]

def getLegalActions():
  # return next action function for state using Generator
  actions = getSingleActions()
  base = len(actions)
  for i in xrange(base<<10):
    j = i
    res = []
    for rank in range(10):
      index = j % base
      res.append(actions[index])
    yield res

def flat(l):
  return [item for sublist in l for item in sublist]

if __name__=='__main__':
  beta = input()
  iss = ISS()
  yew = iss.getInitialOrientation(beta)
  print yew
  flush()
  for i in range(92):
    print 1
    ret = iss.getStateAtMinute(i)
    for v in ret:
      print v
    flush()
