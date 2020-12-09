#gunzip -c hp-mondo-00000-of-00037.PUBMED_SUB_31.tsv.gz | sed 's/@DISEASE\$/_DISEASE_/g' | sed 's/@PHENOTYPE\$/_PHENOTYPE_/g' | gzip > hp-mondo-00000-of-00037.PUBMED_SUB_31.updated.tsv.gz

#gunzip -c ~/Dropbox/a-m/Corpora/TRANSLATOR/pr-pr-PUBMED_SUB_31.tsv.gz | sed 's/@PROTEIN\$/_PROTEIN_/g' > ~/Dropbox/a-m/Corpora/TRANSLATOR/pr-pr-PUBMED_SUB_31.tsv.gz.underscore.tsv

#gunzip -c ~/Dropbox/a-m/Corpora/TRANSLATOR/chebi-mondo-PUBMED_SUB_31.tsv.gz | sed 's/@CHEMICAL\$/_CHEMICAL_/g' | sed 's/@DISEASE\$/_DISEASE_/g' > ~/Dropbox/a-m/Corpora/TRANSLATOR/chebi-mondo-PUBMED_SUB_31.tsv.gz.underscore.tsv

#gunzip -c ~/Dropbox/a-m/Corpora/TRANSLATOR/chebi-hp-PUBMED_SUB_31.tsv.gz | sed 's/@CHEMICAL\$/_CHEMICAL_/g' | sed 's/@PHENOTYPE\$/_PHENOTYPE_/g' > ~/Dropbox/a-m/Corpora/TRANSLATOR/chebi-hp-PUBMED_SUB_31.tsv.gz.underscore.tsv

gunzip -c ~/Dropbox/a-m/Corpora/TRANSLATOR/chebi-pr-sentences.PUBMED_SUB_31.tsv.gz | sed 's/@CHEMICAL\$/_CHEMICAL_/g' | sed 's/@GENE\$/_GENE_/g' > ~/Dropbox/a-m/Corpora/TRANSLATOR/chebi-pr-sentences.PUBMED_SUB_31.tsv.gz.underscore.tsv
