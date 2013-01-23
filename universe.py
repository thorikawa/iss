#!/usr/bin/env python

import sys
from math import *

##########
# Config #
##########
RENDER_LIBRARY = 0
OUTPUT_FILE = "issout.txt"

#########
# Class #
#########
# ISS class
class ISS:
  def __init__(self):
    self.hoge = 0

  def getInitialOrientation(self, beta):
    return 0
  
  def getStateAtMinute(self, minute):
    next = getNextMaxState()
    return flat(zip(next.getRotations(), 10*[0]))

# Learner class
class Learner:
  def __init__(self, beta, yaw):
    self.beta = beta
    self.yaw = yaw

  def learn(self, initialState):
    self.initialState = initialState
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
    for action in self.getLegalActions():
      nextState = action(currentState)

      debug(nextState)
      if not self.canCyclic(nextState, currentMinute+1):
        continue

      score = evaluate(library(nextState, currentMinute+1, self.beta, self.yaw))
      #debug(nextState)
      debug(" ==> " + str(score))
      if maxState is None or maxScore < score:
        maxScore = score
        maxState = nextState
    return maxState

  def canCyclic(self, state, minute):
    initials = self.initialState.getRotations()
    rotations = state.getRotations()
    for i in range(2):
      if minDegree(rotations[i], initials[i]) > (92-minute)*4.5:
        return False
    for i in range(2,10):
      if minDegree(rotations[i], initials[i]) > (92-minute)*8.7:
        return False
    return True

  def getSARJSingleActions(self):
    def a1(r, v):
      return (r, 0)
    def a2(r, v):
      r += 4.5
      if r>=360.0:
        r -= 360
      return (r, 0)
    def a3(r, v):
      r -= 4.5
      if r<0:
        r += 360
      return (r, 0)
    return [a1,a2,a3]
#return [a1,a2]
#return [a2, a3]
#return [a1]

  def getBGASingleActions(self):
    def a1(r, v):
      return (r, 0)
    def a2(r, v):
      r += 8.7
      if r>=360.0:
        r -= 360
      return (r, 0)
    def a3(r, v):
      r -= 8.7
      if r<0:
        r += 360
      return (r, 0)
    return [a1,a2,a3]
#return [a1, a2]
#return [a2, a3]
#return [a1]

  def getLegalActions(self):
    # return next action function for state using Generator
    bgaActions = self.getBGASingleActions()
    sarjActions = self.getSARJSingleActions()
    base = len(bgaActions)
    
    # this is setting when the number of action functions is less than
    # the number of functions we actually need 
    mapping = {0:0, 1:1, 2:2, 3:3, 4:2, 5:3, 6:4, 7:5, 8:4, 9:5}
    for i in xrange(base**6):
      j = i
      res = []
      for rank in range(6):
        index = j % base
        j /= base
        if rank < 2:
          action = sarjActions[index]
        else:
          action = bgaActions[index]
        res.append(action)
      def return_function(state):
        newRotations = []
        newVelocities = []
        rotations = state.getRotations()
        velocities = state.getVelocities()

        for k in range(10):
          (newRotation, newVelocity) = res[mapping[k]](rotations[k], velocities[k])
          newRotations.append(newRotation)
          newVelocities.append(newVelocity)

        return State(newRotations);
      yield return_function

# State class
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
    return str(self.rotations)

###########
# Utility #
###########
# return minimum degree between r1 and r2
# 0 <= r1 < 360
# 0 <= r2 < 360
def minDegree(r1, r2):
  d = abs(r1-r2)
  if d>180:
    return 360-d
  else:
    return d

# return the score of 696 input values
def evaluate(input):
  cosines = input[0:8]
  stringShadow = input[8:664]
  longeronShadow = input[664:696]

  #debug(stringShadow);

  totalPower = 0.0
  for i in xrange(8):
    power = 0.0
    for j in xrange(82):
      index = i*82 + j
      shadowFactor = max(0.0, (1.0-5.0*stringShadow[index]))
      power += 1371.3*cosines[i]*0.1*2.56*shadowFactor
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
  print RENDER_LIBRARY
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
  sys.stderr.write(str(msg)+"\n")

def proceed(state):

  print 1
  ret = flat(zip(state.getRotations(), state.getVelocities()))

  #debug out
  fw.write(str(ret)+"\n")
  fw.flush()

  for v in ret:
    print v
  flush()

def flat(l):
  return [item for sublist in l for item in sublist]

########
# Main #
########
if __name__=='__main__':

  fw = open(OUTPUT_FILE, 'a')
           
  beta = input()
  
  fw.write(str(beta)+"\n")

  #iss = ISS()
  #yew = iss.getInitialOrientation(beta)
  #print yew

  learner = Learner(beta, 0)
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
  #initialState = State(10*[0])

  initialState1 = State([0, 0, 0, 180, 0, 180, 0, 180, 0, 180])
  initialState2 = State([0, 0, 180, 0, 180, 0, 180, 0, 180, 0])
  if beta > 0:
    initialState = initialState1
  else:
    initialState = initialState2
  learner.learn(initialState)

  fw.close()
