#!/usr/bin/perl

# Purpose: normalize the various labels in the TRANSLATOR annotations to Y/N (positive/negative).

# Usage: cat the input file and pipe it through this script.

# catch typos, haha
use strict 'vars';

# test data: 
# turn on debugging when running the test data, because that's how the gold outputs are stored

# set to 1 for debugging output, or to 0 to suppress same
my $DEBUG = 1;
#my $DEBUG = 0;

# comment says it all, right? ;-)
my $INCLUDE_ONES_MIKE_WOULD_NOT_APPROVE_OF_BUT_SCHANK_WOULD = 1;

while (my $line = <>) { 

  # PASS THROUGH UNCHANGED: Y and N
  if ($line =~ /null\t[0-9]+\tY\t/o) {
    $DEBUG && print "UNCHANGED-Y ";
    print $line; next;
  } 
  if ($line =~ /null\t[0-9]+\tN\t/o) {
    $DEBUG && print "UNCHANGED-N ";
    print $line; next;
  }

  # LABELS THAT NEED TO BE NORMALIZED TO Y or N
  # "anaphora" -> Y
  if ($line =~ s/(null\t[0-9]+\t)anaphora\t/$1Y\t/o) {
  $DEBUG && print "ANAPHORA ";  
  print $line; next;
  }

  if ($INCLUDE_ONES_MIKE_WOULD_NOT_APPROVE_OF_BUT_SCHANK_WOULD) {
    if ($line =~ s/(null\t[0-9]+\t)M\t/$1Y\t/o) {
      $DEBUG && print "SCHANK WOULD APPROVE ";
      print $line; next;
    }
  } else {
    if ($line =~ s/(null\t[0-9]+\t)M\t/$1N\t/o) {
      $DEBUG && print "MIKE WOULD NOT APPROVE "; 
      print $line; next;
    }
  }

  # "entity" -> N
  if ($line =~ s/(null\t[0-9]+\t)entity\t/$1N\t/o) {
    $DEBUG && print "ENTITY ";  
    print $line; next;
  }
  # "speculation" -> N
  if ($line =~ s/(null\t[0-9]+\t)speculation\t/$1N\t/o) {
    $DEBUG && print "SPECULATION ";
    print $line; next;
  }
  # "process" -> N
  if ($line =~ s/(null\t[0-9]+\t)process\t/$1N\t/o) {
    $DEBUG && print "PROCESS ";
    print $line; next;
  }
  # "other.relation" -> N
  if ($line =~ s/(null\t[0-9]+\t)other.relation\t/$1N\t/o) {
    $DEBUG && print "OTHER.RELATION ";
    print $line; next;
  }

  # REGULATION RELATIONS  
  # "I(increase)" -> Y

  # "D(ecrease)" -> Y

  # "U(nspecified directionality)" -> Y
  if ($line =~ s/(null\t[0-9]+\t)U\t/$1N\t/o) {
    #print $line; 
    next;
  }

  # any other annotations will get you here--question-mark, segmentation, etc.
  next;
} # close while-loop through input
