# The script that extracts data and gives you counts of annotations needs to have a single file as its input. So, when you want the counts for a relation as a whole, i.e. across all files for that relation, you need to concatenate those files together.

#cat drug-gene/*.tsv > concatenated-annotations/concatenated.drug-gene.tsv
#cat gene-gene/*.tsv > concatenated-annotations/concatenated.gene-gene.tsv
#cat drug-disease/*.tsv > concatenated-annotations/concatenated.drug-disease.tsv
#cat gene-disease/*.tsv > concatenated-annotations/concatenated.gene-disease.tsv
#cat symptom-disease/*.tsv > concatenated-annotations/concatenated.symptom-disease.tsv
#cat gene-location/*.tsv > concatenated-annotations/concatenated.gene-location.tsv

#cat drug-disease/rule-based/*.tsv > concatenated-annotations/concatenated.drug-disease.rule-based.tsv
#cat drug-disease/cooccurrence/*.tsv > concatenated-annotations/concatenated.drug-disease.cooccurrence.tsv
#cat drug-gene/cooccurrence/*.tsv > concatenated-annotations/concatenated.drug-gene.cooccurrence.tsv
#cat drug-gene/rule-based/*.tsv > concatenated-annotations/concatenated.drug-gene.rule-based.tsv

#cat gene-disease/cooccurrence/*.tsv > concatenated-annotations/concatenated.gene-disease.cooccurrence.tsv
cat gene-disease/rule-based/*.tsv > concatenated-annotations/concatenated.gene-disease.rule-based.tsv

