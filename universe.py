#!/usr/bin/env python

import sys
from math import *

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

  def learn(self, initialState):
    state = initialState
    for i in range(92):
      if i==0:
        pass
      else:
        state = self.getNextMaxState(state, i-1)
      proceed(state)

  def getNextMaxState(self, currentState, currentMinute):
    maxState = None
    maxScore = 0
    debug('====' + str(currentMinute) + '====')
    for action in getLegalActions():
      nextState = action(currentState)
      score = evaluate(library(nextState, currentMinute+1, self.beta, self.yaw))
      debug(str(score))
    if maxState is None or maxScore < score:
      maxScore = score
      maxState = nextState
    return maxState

class State:
  def __init__(self):
    self.rotations = 10*[0]
  
  def __init__(self, rotations):
    self.rotations = rotations

  def getRotations(self):
    return self.rotations

  def setRotations(self, rotations):
    self.rotations = rotations

  def getVelocities(self):
    return [0]*10

  def __str__(self):
    str(self.rotations)

# return the score of 696 input values
def evaluate(input):
  cosines = input[0:8]
  stringShadow = input[8:664]
  longeronShadow = input[664:696]
  totalPower = 0.0
  for i in xrange(8):
    power = 0.0
    for j in xrange(82):
      index = i*82 + j
      power += 1371.3*cosines[i]*0.1*2.56*stringShadow[index]
    totalPower += power
  # TODO take consider SAW's minimum average power
  # TODO take consider longeron's shadow factor
  # TODO take consider maxmum BGA rotation
  return totalPower;

# library method call
# @return
# The cosine of the angle between the blanket normal and the vector to the sun for each SAW (8 values).
# The shadow fraction of each string (8 SAWs x 2 blankets x 41 strings = 656 values).
# The shadow fraction of each longeron (8 SAWs x 4 longerons = 32 values).
def library(state, minute, beta, yaw):
  alpha = 360.0*minute/92.0
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
    tmp = float(raw_input())
    ret.append(tmp)
  return ret

def flush():
  sys.stdout.flush()

def debug(msg):
  sys.stderr.write(msg)

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
    def return_function(state):
      newRotations = []
      newVelocities = []
      rotations = state.getRotations()
      velocities = state.getVelocities()
      for j in range(10):
        (newRotation, newVelocity) = res[j](rotations[j], velocities[j])
        newRotations.append(newRotation)
        newVelocities.append(newVelocity)
      return State(newRotations);
    yield return_function

def flat(l):
  return [item for sublist in l for item in sublist]

if __name__=='__main__':
  beta = input()

  #iss = ISS()
  #yew = iss.getInitialOrientation(beta)
  #print yew

  learner = Learner(0, 0)
  print 0

  flush()
  '''
  for i in range(92):
    print 1
    ret = iss.getStateAtMinute(i)
    for v in ret:
      print v
    flush()
  '''
  initialState = State(10*[0])
  learner.learn(initialState)
