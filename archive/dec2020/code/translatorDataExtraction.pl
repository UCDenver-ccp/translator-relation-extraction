#!/usr/bin/perl

# two uses: 

# 1) Extract label and sentence for training classifier
# 2) Extract label only for counting annotations

use strict 'vars';

my $annotated_sentences = 0;
my %labels = ();

# set to 1 for helpful debugging output, or to 0 to suppress it
my $DEBUG = 0;

# prints a summary of the annotations--counts of each label and total annotations.
# set to 1 to print the summary, or to 0 to suppress it
my $PRINT_SUMMARY = 1;
#my $PRINT_SUMMARY = 0;

# set to 1 to produce the counts in R format, tab-separated; otherwise, set to 0. At the moment, this only makes sense when you have PRINT_SUMMARY set to 1
my $OUTPUT_R = 	0; # not currently useful, 'cause I don't have the file names

# extracts sentences and their labels.
# set to 1 to produce training data, or to 0 to suppress it
my $PRINT_EXEMPLARS = 0;
#my $PRINT_EXEMPLARS = 1;

# assumes you cat the file through a pipe
while (my $line = <>) {
	
  # assuming tab-separated data
  my @fields = split("\t", $line);

  # debugging
  $DEBUG && print "$fields[0]\n";
  $DEBUG && print "$fields[$#fields - 2]\n";

  # last fields are: annotation, sentence, and paragraph. So, we want the antepenultimata and the penultima.
  my $class_label = $fields[$#fields - 2];
  my $sentence = $fields[$#fields -1];
  #print $class_label;

  if (length($class_label) > 0) {
    $annotated_sentences++; # if there is a class label, then the sentence has been annotated
    $labels{$class_label}++;
  }

  if ($class_label eq "Y" || $class_label eq "N") {
	if ($PRINT_EXEMPLARS) {
          print "$class_label\t$sentence\n";
        }
  }

  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # DOESN'T THIS NEED TO GET ORDERED EARLIER??
  # this is for the case where we're converting all interactions to a single class.
  # "I" means an increase, "D" means a decrease, and "U" means that directionality
  # is unspecified.
  # So: if we're doing increases and decreases, this bit of code needs to be commented out.
  if ($class_label eq "U" || $class_label eq "I" || $class_label eq "D") {
    $class_label = "Y";
  }  
} # close while-loop through input

### PRODUCE OUTPUT ####

my $R_header_line = "label\tcount\tfile";

if ($OUTPUT_R) {
	print $R_header_line . "\n";
} 

# prints the counts of different labels
if ($PRINT_SUMMARY) {
  my @labels = sort(keys(%labels));

  foreach my $label (@labels) {
    if ($OUTPUT_R) { #print "$label\t$labels{$label}\t$file\n"; 
    } else { print "$label\t$labels{$label}\n"; }
  }

  # you probably also want to know the sum of positive and negative exemplars.
  # note that this might vary for 3-label relations, e.g. Increase, Decrease, Unspecified for 
  # chemical-gene regulation
  my $total_yes_plus_no = $labels{"Y"} + $labels{"N"};

  print "Y+N:\t" . $total_yes_plus_no . "\n";

  # calculate P/R/F for co-occurrence
  my $precision = $labels{"Y"} / $total_yes_plus_no;
  # actually, R isn't calculatable, since we don't know the number of actual instances, so I think we just call it 100, right? as in IR...
  my $recall = 1.0;
  my $F_measure = (2 * $precision * $recall) / ($precision + $recall);

  print "Co-occurrence: P = $precision, R = $recall, F = $F_measure\n";
}
print "Annotated sentences: $annotated_sentences\n";

# quiz: what's the problem with this foreach loop?
#foreach (my $label (@labels)) {
#  print "$label\t$labels{$label}\n";
#}
