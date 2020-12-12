#!/usr/bin/perl

# Purpose: normalize the various labels in the TRANSLATOR annotations to Y/N (positive/negative).

# Usage: cat the input file and pipe it through this script.

# catch typos, haha
use strict 'vars';

# test data: 

# set to 1 for debugging output, or to 0 to suppress same
my $DEBUG = 1;
#my $DEBUG = 0;

while (my $line = <>) { 

  # PASS THROUGH UNCHANGED: Y and N
  if ($line =~ /null\t[0-9]+\tY/o) {
    $DEBUG && print "UNCHANGED-Y ";
    print $line; next;
  } 
  if ($line =~ /null\t[0-9]+\tN/o) {
    $DEBUG && print "UNCHANGED-N ";
    print $line; next;
  }

  # LABELS THAT NEED TO BE NORMALIZED TO Y or N
  # "anaphora" -> Y
  if ($line =~ s/(null\t[0-9]+\t)anaphora/$1Y/o) {
  $DEBUG && print "ANAPHORA ";  
  print $line; next;
  }
  # "entity" -> N
  if ($line =~ s/(null\t[0-9]+\t)entity/$1N/o) {
    $DEBUG && print "ENTITY ";  
    print $line; next;
  }
  # "speculation" -> N
  if ($line =~ s/(null\t[0-9]+\t)speculation/$1N/o) {
    $DEBUG && print "SPECULATION ";
    print $line; next;
  }
  # "process" -> N
  if ($line =~ s/(null\t[0-9]+\t)process/$1N/o) {
    $DEBUG && print "PROCESS ";
    print $line; next;
  }
  # "other.relation" -> N
  if ($line =~ s/(null\t[0-9]+\t)other.relation/$1N/o) {
    $DEBUG && print "OTHER.RELATION ";
    print $line; next;
  }

  # REGULATION RELATIONS  
  # "I(increase)" -> Y

  # "D(ecrease)" -> Y

  # "U(nspecified directionality)" -> Y
  if ($line =~ s/(null\t[0-9]+\t)U/$1N/o) {
    #print $line; 
    next;
  }

  # any other annotations will get you here--question-mark, segmentation, etc.
  next;
} # close while-loop through input
