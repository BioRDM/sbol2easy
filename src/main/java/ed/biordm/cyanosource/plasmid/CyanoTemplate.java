/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.cyanosource.plasmid;

import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;


import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/*
 * Cyano source plasmid template
 * 
 * @author tzielins
 * 
 */

public class CyanoTemplate {
    
    public static final String CYANO_PREF = "http://bio.ed.ac.uk/a_mccormick/cyano_source/";
    
    public static void main(String[] args) throws SBOLValidationException, SBOLConversionException, IOException, URISyntaxException, ed.biordm.sbol.toolkit.transform.SBOLConversionException {
        Path tempDir = Paths.get("E:/Temp");
        
        Path outDir = tempDir.resolve("cyanosource_"+LocalDate.now());
        Files.createDirectories(outDir);
        
        String version = "1.0";
        Path templateFile = outDir.resolve("cyano_template.xml");
        
        
        SBOLDocument templateDoc = cyanoDocument();
        ComponentDefinition template = createTemplatePlasmid(templateDoc, version);
        validateSbol(templateDoc);
        SBOLWriter.write(templateDoc, templateFile.toFile());        
    }

    public static SBOLDocument cyanoDocument() {
        SBOLDocument doc = new SBOLDocument();

        doc.setDefaultURIprefix(CYANO_PREF);
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        return doc;                
    }    

    public static void validateSbol(SBOLDocument doc) {
        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(doc, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println("E\t"+error);
            }            
        }        
        if (SBOLValidate.getNumErrors() > 0) {
            throw new IllegalStateException("Stoping cause of validation error: "+SBOLValidate.getErrors().get(0));
        }                
    }
    
    public static ComponentDefinition createTemplatePlasmid(SBOLDocument doc, String version) throws SBOLValidationException {
        
        ComponentDefinition insert = createCassete(doc, version);

        ComponentDefinition ampROrg = createBackbone(doc, version);

        ComponentDefinition leftFlank = createLeftFlank(doc, version);

        ComponentDefinition rightFlank = createRightFlank(doc, version);

        ComponentDefinition plasmid = assembleTemplatePlasmid(doc, version, ampROrg, leftFlank, insert, rightFlank);  
        return plasmid;
    }    

    static ComponentDefinition createCassete(SBOLDocument doc, String version) throws SBOLValidationException {
        
        String name = "insert";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.setName("insert");
        //region.setDescription("transgene insert with codA and Km");
        region.createAnnotation(SBH_DESCRIPTION,"transgene insert with codA and Km");
        
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // engineered insert
        region.addRole(SO("SO:0000915"));
        
        String seqStr ="ATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCgtaa";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        // where the left flank ends, so the possitions are 1 based (numbers come from snapgene)
        final int SS = 2657;
        SequenceAnnotation an = region.createSequenceAnnotation("SapI_ATG_over", "SapI_ATG_over", 2658-SS, 2660-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("SapI-ATG overhang");
    
        an = region.createSequenceAnnotation("SapI_BspQI", "SapI_BspQI", 2662-SS, 2668-SS);
        an.addRole(SO("SO:0001687"));
        an.setName("SapI/BspQI");
        
        ComponentDefinition barcode = doc.createComponentDefinition("barcode", version, ComponentDefinition.DNA_REGION);
        barcode.addRole(SO("SO:0000730"));

        Component barcodeI = region.createComponent("barcode_inst", AccessType.PUBLIC, barcode.getIdentity());
        
        an = region.createSequenceAnnotation("barcode", "barcode", 2680-SS, 2697-SS);
        an.setComponent(barcodeI.getIdentity());
        //an = region.createSequenceAnnotation("barcode", "barcode", 2680-SS, 2697-SS);
        //an.addRole(SO("SO:0000730"));

        an = region.createSequenceAnnotation("BpiI_TGCC_over", "BpiI_TGCC_over", 2698-SS, 2701-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("BpiI-TGCC overhang");

        an = region.createSequenceAnnotation("BpiI", "BpiI", 2704-SS, 2709-SS);
        an.addRole(SO("SO:0001687"));
        

        an = region.createSequenceAnnotation("AarI_TGCC_over", "AarI_TGCC_over", 2710-SS, 2713-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("AarI-TGCC overhang");

        an = region.createSequenceAnnotation("AarI", "AarI", 2718-SS, 2724-SS);
        an.addRole(SO("SO:0001687"));
        
        
        /* removed in Anja4.dna version 
        an = region.createSequenceAnnotation("attB_CC", "attB_CC", 2725-SS, 2777-SS);
        //recombination_signal_sequence
        an.addRole(SO("SO:0001532"));
        an.setName("attB CC");
        */
        
        an = region.createSequenceAnnotation("J23101MH_prom", "J23101MH_prom", 2725-SS, 2813-SS);
        an.addRole(SequenceOntology.PROMOTER); 
        an.setName("J23101MH prom");
        an.createAnnotation(SBH_DESCRIPTION,"pC0.031 https://doi.org/10.1104/pp.18.01401");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "pC0.031  https://doi.org/10.1104/pp.18.01401");
        
        
        ComponentDefinition codA = doc.createComponentDefinition("codA", version, ComponentDefinition.DNA_REGION);
        codA.addRole(SequenceOntology.CDS);
        codA.createAnnotation(SBH_DESCRIPTION, "Codon optimised (V153A, F317C)  ndoi:10.1111/tpj.12675");
        //codA.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "Codon optimised (V153A, F317C)  doi:10.1111/tpj.12675");
        
        seqStr = "ATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGA";
        seq = doc.createSequence("codA_seq", version, seqStr, Sequence.IUPAC_DNA);
        codA.addSequence(seq);


        Component codAI = region.createComponent("codA_inst", AccessType.PUBLIC, codA.getIdentity());
        
        an = region.createSequenceAnnotation("codA", "codA", 2814-SS, 4097-SS);
        an.setComponent(codAI.getIdentity());
        //an.addRole(SequenceOntology.CDS);
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product","gbconv"), 
        //        "Codon optimised (V153A, F317C)  \ndoi:10.1111/tpj.12675");
        
        an = region.createSequenceAnnotation("rrnBT1_T7_term", "rrnBT1_T7_term", 4102-SS, 4216-SS);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("rrnBT1/T7 term");
        an.createAnnotation(SBH_DESCRIPTION, 
                "BBa_B0015 double terminator (B0010-B0012) pC0.082 https://doi.org/10.1104/pp.18.01401");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "BBa_B0015 double terminator (B0010-B0012)  pC0.082  https://doi.org/10.1104/pp.18.01401");
        
        an = region.createSequenceAnnotation("KanR_term", "KanR_term", 4221-SS, 4504-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("KanR term");
        
        an = region.createSequenceAnnotation("KanR", "KanR", 4505-SS, 5320-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.CDS); 
        an.createAnnotation(SBH_DESCRIPTION,
                "Gene: aph(3')-Ia aminoglycoside phosphotransferase confers resistance to kanamycin "
                        + "in bacteria or G418 (Geneticin(R)) in eukaryotes");
        
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "confers resistance to kanamycin in bacteria or G418 (Geneticin(R)) in eukaryotes");
        an.createAnnotation(GB_GENE, 
                "aph(3')-Ia");
        an.createAnnotation(GB_PRODUCT, 
                "aminoglycoside phosphotransferase");

        an = region.createSequenceAnnotation("KanR_prom", "KanR_prom", 5321-SS, 5430-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.PROMOTER);
        an.setName("KanR prom");
        
        /* removed in Anja4
        an = region.createSequenceAnnotation("attB_TT", "attB_TT", 5484-SS, 5536-SS);
        //recombination_signal_sequence
        an.addRole(SO("SO:0001532"));
        an.setName("attB TT");
        an.createAnnotation(SBH_DESCRIPTION, "https://doi.org/10.1002/bit.26854");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "https://doi.org/10.1002/bit.26854");
        */
        
        an = region.createSequenceAnnotation("AarI_2", "AarI_2", 5431-SS, 5437-SS);
        an.setName("AarI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("AarI_GGGA_over", "AarI_GGGA_over", 5442-SS, 5445-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("AarI-GGGA overhang");

        an = region.createSequenceAnnotation("BpiI_2", "BpiI_2", 5446-SS, 5451-SS);
        an.setName("BpiI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("BpiI_GGGA_over", "BpiI_GGGA_over", 5454-SS, 5457-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("BpiI-GGGA overhang");

        an = region.createSequenceAnnotation("SapI_BspQI_2", "SapI_BspQI_2", 5458-SS, 5464-SS);
        an.setName("SapI/BspQI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("SapI_TAA_over", "SapI_TAA_over", 5466-SS, 5468-SS);
        //an.addRole(SO("SO:0001695"));
        an.addRole(SO("SO:0001933"));
        an.setName("SapI-TAA overhang");
        
        
        return region;
    }

    static ComponentDefinition createBackbone(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACGAAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggag";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);
        
        
        SequenceAnnotation an = region.createSequenceAnnotation("BsmBI", "BsmBI", 31, 31);
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("BsmBI_2", "BsmBI_2", 73, 73);
        an.setName("BsmBI");
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 176, 280);
        an.addRole(SequenceOntology.PROMOTER);
        an.createAnnotation(GB_GENE, "bla");
        
        an = region.createSequenceAnnotation("AmpR", "AmpR", 281, 1141);
        an.addRole(SequenceOntology.CDS);    
        an.createAnnotation(SBH_DESCRIPTION, 
                "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        //an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        an.createAnnotation(GB_GENE,"bla");
        an.createAnnotation(GB_PRODUCT,"beta-lactamase");        
        
        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        //originD.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note","gbconv"), 
        //        "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");   
        originD.createAnnotation(SBH_DESCRIPTION, "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");
        
        seqStr = "TTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAA"; 
        seq = doc.createSequence("ori_seq", version, seqStr, Sequence.IUPAC_DNA);
        originD.addSequence(seq);
        
        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        
        an = region.createSequenceAnnotation("ori", "ori", 1312, 1900);
        an.setComponent(origin.getIdentity());
        return region;
    }

    /*
    static ComponentDefinition createEndGap(SBOLDocument doc, String version) throws SBOLValidationException, URISyntaxException {

        String name = "end_of_backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.setName("backbone end");
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACG";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        int SS = 6058;
        SequenceAnnotation an = region.createSequenceAnnotation("BsmBI", "BsmBI", 6089-SS, 6089-SS);
        an.addRole(SO("SO:0001687"));
        
        an = region.createSequenceAnnotation("BsmBI_2", "BsmBI_2", 6131-SS, 6131-SS);
        an.setName("BsmBI");
        an.addRole(SO("SO:0001687"));        
        
        return region;
    }*/

    static ComponentDefinition createLeftFlank(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "left_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // recombination feature
        //region.addRole(SO("SO:0000298"));
        //site_specific_recombination_target_region‘
        region.addRole(SO("SO:0000342"));
        return region;
    }

    static ComponentDefinition createRightFlank(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "right_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //region.addRole(SequenceOntology.ENGINEERED_REGION);
        // recombination feature
        //region.addRole(SO("SO:0000298"));
        //region.addRole(OrientationType.REVERSECOMPLEMENT);
        //site_specific_recombination_target_region‘
        region.addRole(SO("SO:0000342"));
        return region;
    }

    static ComponentDefinition assembleTemplatePlasmid(SBOLDocument doc, String version, 
            ComponentDefinition backbone, 
            ComponentDefinition leftFlank, 
            ComponentDefinition insert, 
            ComponentDefinition rightFlank) throws SBOLValidationException {

        return assebmleTemplatePlasmid(doc, version, backbone.getIdentity(), leftFlank.getIdentity(), insert.getIdentity(), 
                rightFlank.getIdentity());
        
    }

    static ComponentDefinition assebmleTemplatePlasmid(SBOLDocument doc, String version, 
            URI backbone, 
            URI leftFlank, 
            URI insert, 
            URI rightFlank) throws SBOLValidationException {

        String name = "cyano_source_template";
        ComponentDefinition plasmid = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        plasmid.addType(SequenceOntology.CIRCULAR);
        //engineered plasmid
        plasmid.addRole(SO("SO:0000637"));
        
        plasmid.createAnnotation(CREATOR,"Alistair McCormick");
        plasmid.createAnnotation(CREATOR,"Anja Nenninger");
        
        String description = "Recombinant plasmid targeting {gene}";
        plasmid.setDescription(description);
        
        String fullDescription = "Recombinant plasmid targeting {gene}\n"+
        "Target organism: Synechocystis sp. PCC 6803\n"+
        "Assembly method: MoClo\n"+
        "CyanoSource record: <a href=\"https://cyanosource.ac.uk/plasmid/{linkId}\">"+
                "https://cyanosource.ac.uk/plasmid/{linkId}</a>";
        
        plasmid.createAnnotation(SBH_DESCRIPTION,fullDescription);
            
        Component aC = plasmid.createComponent("backbone", AccessType.PUBLIC, backbone);
        Component lC = plasmid.createComponent("left", AccessType.PUBLIC, leftFlank);
        Component iC = plasmid.createComponent("insert", AccessType.PUBLIC, insert);
        Component rC = plasmid.createComponent("right", AccessType.PUBLIC, rightFlank);
        //Component gC = plasmid.createComponent("gap", AccessType.PUBLIC, endGap);

        plasmid.createSequenceConstraint("cs1", RestrictionType.PRECEDES, aC.getIdentity(), lC.getIdentity());
        plasmid.createSequenceConstraint("cs2", RestrictionType.PRECEDES, lC.getIdentity(), iC.getIdentity());
        plasmid.createSequenceConstraint("cs3", RestrictionType.PRECEDES, iC.getIdentity(), rC.getIdentity());
        //plasmid.createSequenceConstraint("cs4", RestrictionType.PRECEDES, rC.getIdentity(), gC.getIdentity());
                
        return plasmid;
    }
    

    
  

}
