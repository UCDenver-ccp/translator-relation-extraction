#!/usr/bin/perl

# PURPOSE: during February relay, predict the relation for BioASQ questions; predict which ones we CAN'T answer and shouldn't bother trying to annotate for their entities?

# TEST data: testRelationPredictor.input.txt, testRelationPredictor.gold.txt

# Expected input: BioASQ spreadsheet with columns added during the February relay.
# Crucial assumption about the input: the question is in the first field.
# Less-crucial assumption about the input: it's the Summary sheet of that file.

use strict 'vars';

# set to 1 for debugging output, or to 0 to suppress it
my $DEBUG = 0;
#my $DEBUG = 1;


# store patterns here in case you want to look at them separately
my %patterns = ();


while (my $line = <>) {

  $DEBUG && print $line;
  my @elements = split("\t", $line);
  my $question; # text of the question

  # A little hack to let you pre-specify which field has the sentence that you want to look at...
  if (1) {
    $question = $elements[1];

    1 && print "INPUT: <$question>\n";
  } 

  if (0 == canWeHandleThisQuestion($question)) { 1 && print "Skipping...\n"; next; }
  # preprocessing
  $question = lc($question); 

# DIRECTIONAL RELATIONS
# E.g. regulates(drug, gene) or regulates(gene, gene)
my $DIRECTIONAL_RELATION = 1;

# NON-DIRECTIONAL RELATIONS
# E.g. located_in(gene, location) or has_symptom(disease, symptom)

if ($DIRECTIONAL_RELATION) {
  # separate patterns get at upregulation, downregulation, and non-directional interactions

  extractDirectionalRelation();

  #if ($line =~ /$elements[2] (up\-regulates|upregulates|amplifies|raises|stimulates|trans\-activates|transactivates|catalyzes|catalyses|re\-activates|reactivates|transduces|enhances|promotes|increases|evokes|enhances|stabilizes|augments|facilitates|augments|triggers|potentiates|elevates|raises|stimulates|activates|initiates|promotes) $elements[5]/) {
 
  # if this works as I think it does, it might fix the actual source of the hyphen-related problem
  $elements[2] = quotemeta($elements[2]);
  $elements[5] = quotemeta($elements[5]);
  # removed hyphens due to reserved character problem
  if ($line =~ /($elements[2]|$elements[5]) (upregulates|upregulated
                              |amplifies|amplified
                              |raises|raised
                              |stimulates|stimulated
                              |transactivates|transactivated
                              |catalyzes|catalyses|catalyzed|catalysed
      |reactivates|reactivated
      |transduces|transduced
      |enhances|enhanced
      |promotes|promoted
      |increases|increased
      |evokes|evoked
      |stabilizes|stabilized
      |augments|augmented
      |facilitates|facilitated
      |augments|augmented
      |triggers|triggered
      |potentiates|potentiated
      |elevates|elevated
      |raises|raised
      |increases|increased
      |stimulates|stimulated
      |activates|activated
      |initiates|initiated
      |promotes|promoted) ($elements[5]|$elements[2])/i) {

    #print "UPREGULATE\n";
    print "UPREGULATE\t$line";
  }
  if ($line =~ /$elements[2] (downregulates|downregulated
                              |inhibits|inhibited
                              |suppresses|suppressed
                              |decreases|decreased
                              |reduces|reduced
                              |lowers|lowered
                              |represses|repressed
                              |depresses|depressed
                              |blocks|blocked
                              |inactivates|inactivated
                              |antagonizes|antagonized
                              |deactivates|deactivated
                              |antagonizes|antagonized|antagonises|antagonised) $elements[5]/i) {
    #print "DOWNREGULATE\n";
    print "DOWNREGULATE\t$line";
  }

  if ($line =~ /$elements[2] (regulates|regulated|regulating
    |binds|bound|binding
    |targets|targeted|targeting
	|phosphorylates|phosphorylated|phosphorylating
	|acetylates|acetylated|acetylating
	|demethylates|demethylated|demethylating
        |hydrolyses|hydrolyzes
    	|methylates|methylated
        |ubiquitinates|ubiquitinated
        |polyubiquitinates|polyubiquitinated
        |dephosphorylates|dephosphorylated
	|deacetylates|deacetylated
  	|induces|induced
  	|influences|influenced
        |(interacts with)|(interacted with)) $elements[5]/i) {
    #print "REGULATE\n";
    print "REGULATE\t$line";

  }

  if ($line =~ /[Ii]nteraction of ($elements[2]|$elements[5]) with ($elements[2]|$elements[5])/) { 
	#print "REGULATE\n"; }
	print "REGULATE\t$line"; }

  if ($line =~ /[Ii]nteraction between ($elements[2]|$elements[5]) and ($elements[2]|$elements[5])/) { 
	#print "REGULATE\n"; }
	print "REGULATE\t$line"; }
  if ($line =~ /[Ii]nteraction of ($elements[2]|$elements[5]) and ($elements[2]|$elements[5])/) { 

	#print "REGULATE\n"; }
	print "REGULATE\t$line"; }

  if ($line =~ /[Uu]pregulation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 

	#print "UPREGULATE\n"; }
	print "UPREGULATE\t$line"; }

  if ($line =~ /[Aa]ctivation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 

	#print "UPREGULATE\n"; }
	print "UPREGULATE\t$line"; }
  if ($line =~ /[Ss]timulation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 

	#print "UPREGULATE\n"; }
	print "UPREGULATE\t$line"; }
  if ($line =~ /[Ee]nhancement of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
	#print "UPREGULATE\n"; }
	print "UPREGULATE\t$line"; }
  if ($line =~ /[Pp]romotion of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
	#print "UPREGULATE\n"; }
	print "UPREGULATE\t$line"; }
  if ($line =~ /[Ee]levation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 

	#print "UPREGULATE\n"; }
	print "UPREGULATE\t$line"; }
  
  if ($line =~ /[Dd]ownpregulation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
	#print "DOWNREGULATE\n"; }
	print "DOWNREGULATE\t$line"; }
  if ($line =~ /[Ii]nhibition of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 

	#print "DOWNREGULATE\n"; }
	print "DOWNREGULATE\t$line"; }
  if ($line =~ /[Ss]uppression of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
	#print "DOWNREGULATE\n"; 
	print "DOWNREGULATE\t$line"; 
  }
  if ($line =~ /[Dd]eactivation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
	#print "DOWNREGULATE\n"; 
	print "DOWNREGULATE\t$line"; 
  }
  if ($line =~ /[Rr]epression of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
	#print "DOWNREGULATE\n"; 
        print "DOWNREGULATE\t$line"; 
  }
  
  if ($line =~ /\b[Rr]egulation of ($elements[2]|$elements[5]) by ($elements[2]|$elements[5])/) { 
  	#print "REGULATE\n"; 
  	print "REGULATE\t$line"; 

  }
  if ($line =~ /\b[Bb]inding of ($elements[2]|$elements[5]) (by|with|to|and) ($elements[2]|$elements[5])/) { 
	#print "REGULATE\n"; }
	print "REGULATE\t$line"; 
  }

  } # CLOSE IF-DRUG-GENE-INTERACTIONS

  ### DISEASE-SYMPTOM RELATIONS
  ### We are currently tagging these with MONDO and with the HPO, so the "definition" of "symptom" is REALLY broad...

  # adverbs
  my $adverbs_positive = "often|frequently";
  my $adverbs = "sometimes|occasionally|occasional";
  my $adverbs_negative = "rarely|infrequently";

  extractNonDirectionalRelation($line, $adverbs_positive, $adverbs, $adverbs_negative, @elements);
    

  # A with B
  #if ($line =~ /($elements[2] with $elements[5])/gi) {
  if ($line =~ /(($elements[2]|$elements[5]) with ($elements[2]|$elements[5]))/gi) {
    
    $DEBUG && print "DISEASE-SYMPTOM\t$line\n";
    $patterns{$1}++;
  }
  if ($line =~ /($elements[5] with $elements[2])/gi) {
    $DEBUG && print "DISEASE-SYMPTOM\t$line\n";
    $patterns{$1}++;
  }

  # A diagnosed by B
  if ($line =~ /(($elements[$22]|$elements[5]) diagnosed by ($elements[2]|$elements[5]))/gi) { 

    $DEBUG && print "DISEASE-SYMPTOM\t$line\n";
    $patterns{$1}++;
  }  
  #if ($line =~ /($elements[2] diagnosed by $elements[5])/gi) { 

    #$DEBUG && print "DISEASE-SYMPTOM\t$line\n";
    #$patterns{$1}++;
  #}  

  if ($line =~ /(($elements[2]|$elements[5]) (with|in) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  #if ($line =~ /($elements[5] (with|in) $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  #if ($line =~ /($elements[2] (with|in) $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  #if ($line =~ /($elements[5] (with|in) $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

  if ($line =~ /($elements[5] (due|secondary|subsequent) to $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /($elements[2] (due|secondary|subsequent) to $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /($elements[2] (due|secondary|subsequent) to $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /($elements[5] (due|secondary|subsequent) to $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

   if ($line =~ /($elements[2] characterized by $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++}
   if ($line =~ /($elements[5] characterized by $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

   # to occur
   if ($line =~ /($elements[2] (occurs|occurring|occurred|occur) (with|in) $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
   if ($line =~ /($elements[5] (occurs|occurring|occurred|occur) (with|in) $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

   # characteristic
   if ($line =~ /($elements[2] (is)? characteristic of $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
   if ($line =~ /($elements[5] (is)? characteristic of $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;i}

   # syndrome
   if ($line =~ /($elements[2] (is|are) (an|a) ((rare|unusual) )? syndrome (of|(characterized by)) $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
   if ($line =~ /($elements[5] (is|are) (an|a) ((rare|unusual) )? syndrome (of|(characterized by)) $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /($elements[2] characteristic of $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /($elements[5] characteristic of $elements[2])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  # if ($line =~ /($elements[2] (with|in) $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  # if ($line =~ /($elements[2] (with|in) $elements[5])/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

 # correlation
 if ($line =~ /(($elements[2]|$elements[5]) (correlates|correlated|correlating|correlate) with ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++; }

 # cause
  if ($line =~ /(($elements[2]|$elements[5]) (causes|caused|causing|cause) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

 # suggest
  if ($line =~ /(($elements[2]|$elements[5]) (suggesting|suggests|suggested|suggest) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  
 # comorbidity
  if ($line =~ /(($elements[2]|$elements[5]) comorbid with ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /(comorbidity of ($elements[2]|$elements[5]) (with|and) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  
 # manifestations
 if ($line =~ /(($elements[2]|$elements[5]) (manifesting|manifested|manifests|manifest) (as|by) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}


 # sign of
  if ($line =~ /(($elements[2]|$elements[5]) (as|is)( a) sign of ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
 
 # ANNOTATE "PHENOTYPE WITH DISEASE" AND SUBSEQUENT PATTERNS
  #if ($line =~ /(($elements[2]|$elements[5]) (causes|caused|causing|cause) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

# associations
  if ($line =~ /(($elements[2]|$elements[5]) associated with ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /(($elements[2]|$elements[5]) in association with ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /(association (of|between) ($elements[2]|$elements[5]) (and|with|or) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /(association (of|between) ($elements[2]|$elements[5]) (and|with|or) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}
  if ($line =~ /(($elements[2]|$elements[5])-(associated|induced) ($elements[2]|$elements[5]))/gi) { $DEBUG && print "DISEASE-SYMPTOM\t$line\n"; $patterns{$1}++;}

# add biomarker
# add something about observed in/with/after/befre
} # CLOSE LOOP THROUGH FILE

if (1) {
  my $count_of_matches = 0;
  my @patterns = sort {$patterns{$b} <=> $patterns{$a}} keys(%patterns);
  foreach my $pattern (@patterns) {
    $count_of_matches += $patterns{$pattern};
    print "PATTERN: $patterns{$pattern}\t$pattern\n";
  }

  print "Total matches: $count_of_matches\n";
}

# is this a "why" question?  if so, we can't give a *good* answer. (Hardly anyone can! Fascinating research topic...)
# returns 1 if it *is* a why-question, 0 if it isn't
sub isWhyQuestion{
  my $question = $_[0];
  1 && print "In isWhyQuestion: <$question>\n";
  if ($question =~ /\bwhy\b/i) { return 1; }
  if (($question =~ /.*\bwhat\b/i) && ($question =~ /.*reason.*/)) { return 1; }
  # if you got here, it isn't a why-question
  return 0;   
} # close subroutine isWhyQuestion()

# runs some basic questions to see if we think we can answer a question, or not.
# if at any point the subroutine thinks we can't, it returns 0.
# if it doesn't see any reasons why not, it returns 1.
sub canWeHandleThisQuestion {
  my $question = $_[0];
  1 && print "in canWeHandle: <$question>\n";
  # if it's longer than n words, don't bother with it...
  my @wordsInQuestion = split(" ", $question);
  my $lengthInWords = @wordsInQuestion;
  1 && print "Length in words: $lengthInWords\n";i

  if (10 <= $lengthInWords) { return 0; } # MAGIC NUMBER

  if (1 == isWhyQuestion($question)) { return 0; } 
  return(1);
 
} # close subroutine definition canWeHandleThisQuestion()

sub extractDirectionalRelation {

  return 1;
}

sub extractNonDirectionalRelation {

  my ($line, $adverbs_positive, $adverbs, $adverbs_negative, @elements) = @_;

  # A is a symptom of B
  #if ($line =~ /($elements[2] is a symptom of $elements[5])/gi) {
  if ($line =~ /($elements[2] is (($adverbs|$adverbs_positive|adverbs_negative) )?a symptom of $elements[5])/gi) {
    $DEBUG && print "DISEASE-SYMPTOM\t$line\n";
    $patterns{$1}++; # $1 is the text that matched whatever's inside the parentheses in the preceding regex
  }
  #if ($line =~ /($elements[5] is a symptom of $elements[2])/gi) {
  #if ($line =~ /($elements[5] is (($adverbs_positive|$adverbs_negative) )?a symptom of $elements[2])/gi) {
  if ($line =~ /(($elements[5]|$elements[2]) is (($adverbs_positive|$adverbs_negative) )?a symptom of ($elements[2]|$elements[5]))/gi) {
    $DEBUG && print "DISEASE-SYMPTOM\t$line\n";
    $patterns{$1}++;
  } 

} # close subrouting extractNonDirectionalRelation()

