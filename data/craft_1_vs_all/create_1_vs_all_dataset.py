import argparse

def main():
    parser = argparse.ArgumentParser(description="Process command-line arguments")
    parser.add_argument("input_file", type=str, help="First argument")
    parser.add_argument("predicate", type=str, help="Second argument")
    # Add more arguments as needed

    args = parser.parse_args()

    input_file = args.input_file
    predicate = args.predicate
    output_file = f"bert_input.{predicate}.tsv"

    with open(input_file, 'r') as infile, open(output_file, 'w') as outfile:
	    for line in infile:
	        columns = line.strip().split('\t')
	        if len(columns) >= 3:
	            if columns[2] == predicate:
	                columns[2] = "label1"
	            else:
	                columns[2] = "false"
	        outfile.write('\t'.join(columns) + '\n')


if __name__ == "__main__":
    main()