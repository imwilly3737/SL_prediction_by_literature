package object;

public class CosmicMutantGene {
	public String Gene_name;
	public String Accession_Number;
	public String Gene_CDS_length;
	public String HGNC_ID;
	public String Sample_name;
	public String ID_sample;
	public String ID_tumour;
	public String Primary_site;
	public String Site_subtype_1;
	public String Site_subtype_2;
	public String Site_subtype_3;
	public String Primary_histology;
	public String Histology_subtype_1;
	public String Histology_subtype_2;
	public String Histology_subtype_3;
	public String Genome_wide_screen;
	public String Mutation_ID;
	public String Mutation_CDS;
	public String Mutation_AA;
	public String Mutation_Description;
	public String Mutation_zygosity;
	public String LOH;
	public String GRCh;
	public String Mutation_genome_position;
	public String Mutation_strand;
	public String SNP;
	public String FATHMM_prediction;
	public String FATHMM_score;
	public String Mutation_somatic_status;
	public String Pubmed_PMID;
	public String ID_STUDY;
	public String Sample_source;
	public String Tumour_origin;
	public String Age;
	public CosmicMutantGene(){}
	public CosmicMutantGene(String line,String sep){
		String[] eTab = line.split(sep);
		try{
				Gene_name = eTab[0];
				Accession_Number = eTab[1];
				Gene_CDS_length = eTab[2];
				HGNC_ID = eTab[3];
				Sample_name = eTab[4];
				ID_sample = eTab[5];
				ID_tumour = eTab[6];
				Primary_site = eTab[7];
				Site_subtype_1 = eTab[8];
				Site_subtype_2 = eTab[9];
				Site_subtype_3 = eTab[10];
				Primary_histology = eTab[11];
				Histology_subtype_1 = eTab[12];
				Histology_subtype_2 = eTab[13];
				Histology_subtype_3 = eTab[14];
				Genome_wide_screen = eTab[15];
				Mutation_ID = eTab[16];
				Mutation_CDS = eTab[17];
				Mutation_AA = eTab[18];
				Mutation_Description = eTab[19];
				Mutation_zygosity = eTab[20];
				LOH = eTab[21];
				GRCh = eTab[22];
				Mutation_genome_position = eTab[23];
				Mutation_strand = eTab[24];
				SNP = eTab[25];
				FATHMM_prediction = eTab[26];
				FATHMM_score = eTab[27];
				Mutation_somatic_status = eTab[28];
				Pubmed_PMID = eTab[29];
				ID_STUDY = eTab[30];
				Sample_source = eTab[31];
				Tumour_origin = eTab[32];
				Age = eTab[33];
		}catch(ArrayIndexOutOfBoundsException e){
			;
		}
	}
}
