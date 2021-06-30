/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import ed.biordm.sbol.toolkit.transform.CommonAnnotations;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.GB_GENE;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.GB_PRODUCT;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_DESCRIPTION;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SO;
import ed.biordm.sbol.toolkit.transform.ComponentAnnotator;
import ed.biordm.sbol.toolkit.transform.ComponentFlattener;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.emptyDocument;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.saveValidSbol;
import ed.biordm.sbol.toolkit.transform.LibraryGenerator;
import java.io.IOException;
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
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author tzielins
 */
public class PaperRecipes {
    
    static String version = "1.0";
    
    static Path outDir;
    static Path inputs;
    
    public static void main(String[] args) {

        Path tempDir = Paths.get("E:/Temp");        
        outDir = tempDir.resolve("sbol2easy_"+LocalDate.now());
        
        inputs = Paths.get("examples");
        assert(Files.isDirectory(inputs));

        
        try {
            Files.createDirectories(outDir);
            PaperRecipes recipes = new PaperRecipes();
            
            
            // creating template
            Path templateFile = recipes.createTemplateDesignFile(outDir,"template.xml");
            
            //generating library
            Path libraryDef = inputToOut("library_def.xlsx");                        
            LibraryGenerator generator = new LibraryGenerator();
            generator.generateFromFiles("library", version, templateFile, libraryDef, outDir, true);
            
            Path libraryFile = outDir.resolve("library.1.xml");
            assert(Files.isRegularFile(libraryFile));
            
            //flatten
            Path flatFile = recipes.flatten(libraryFile);
            assert(Files.isRegularFile(flatFile));
            
            //annotate flatten
            Path annotDef = inputToOut("flat_annotation.xlsx");            
            Path annotatedFile = recipes.annotate(flatFile, annotDef);
            assert(Files.isRegularFile(annotatedFile));
            
            
        } catch (IOException | SBOLConversionException | SBOLValidationException e) {
            System.out.println("Failed: "+e.getMessage());
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        
        
    }
    

    
    static Path inputToOut(String name) throws IOException {
        Path inputFile = inputs.resolve(name);
        assert(Files.isRegularFile(inputFile));            
        Path outFile = outDir.resolve(inputFile.getFileName());
        Files.copy(inputFile, outFile); 
        return outFile;
    }
    
    Path annotate(Path sourceFile, Path metaFile) throws SBOLValidationException, IOException, SBOLConversionException {
        SBOLDocument in = SBOLReader.read(sourceFile.toFile());

        ComponentAnnotator annotator = new ComponentAnnotator();
        annotator.annotate(in, metaFile, false, true, true);
        Path annotatedFile = outDir.resolve("annotated.xml");
        saveValidSbol(in, annotatedFile);
        return annotatedFile;
    }
    
    Path flatten(Path sourceFile) throws SBOLValidationException, IOException, SBOLConversionException {
        SBOLDocument in = SBOLReader.read(sourceFile.toFile());
        SBOLDocument out = emptyDocument();
        ComponentFlattener flattener = new ComponentFlattener();
        flattener.flattenDesigns(in, " flat", out, false);
        Path flatFile = outDir.resolve("flattened.xml");
        saveValidSbol(out, flatFile);        
        return flatFile;
    }

    Path createTemplateDesignFile(Path outDir, String name) throws SBOLValidationException, IOException, SBOLConversionException {
        Path templateFile = outDir.resolve(name);
        SBOLDocument doc = emptyDocument();
        ComponentDefinition template = assemblePlasmidTemplate(doc, version);
        saveValidSbol(doc, templateFile);;
        return templateFile;        
    }
    
    ComponentDefinition assemblePlasmidTemplate(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "plasmid_template";
        ComponentDefinition plasmid = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        plasmid.addType(SequenceOntology.CIRCULAR);
        //engineered plasmid
        plasmid.addRole(SO("SO:0000637"));
        
        String description = "Recombinant plasmid targeting {key}";
        plasmid.setDescription(description);
        
        String fullDescription = "Recombinant plasmid targeting {key}\n"+
        "Target organism: Synechocystis sp. PCC 6803\n"+
        "Assembly method: MoClo\n";
        
        plasmid.createAnnotation(SBH_DESCRIPTION,fullDescription);
            
        Component back = plasmid.createComponent("backbone", AccessType.PUBLIC,createBackbone(doc, version).getPersistentIdentity());
        Component left = plasmid.createComponent("left", AccessType.PUBLIC, createLeftFlank(doc, version).getPersistentIdentity());
        Component barcode = plasmid.createComponent("barcode", AccessType.PUBLIC, createBarcode(doc, version).getPersistentIdentity());
        Component insert = plasmid.createComponent("insert", AccessType.PUBLIC, createInsert(doc,version).getPersistentIdentity());
        Component right = plasmid.createComponent("right", AccessType.PUBLIC, createRightFlank(doc, version).getPersistentIdentity());

        plasmid.createSequenceConstraint("cs1", RestrictionType.PRECEDES, back.getIdentity(), left.getIdentity());
        plasmid.createSequenceConstraint("cs2", RestrictionType.PRECEDES, left.getIdentity(), barcode.getIdentity());
        plasmid.createSequenceConstraint("cs3", RestrictionType.PRECEDES, barcode.getIdentity(), insert.getIdentity());
        plasmid.createSequenceConstraint("cs4", RestrictionType.PRECEDES, insert.getIdentity(), right.getIdentity());
                
        return plasmid;
    }
    
    
    
    ComponentDefinition createBackbone(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        
        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACGAAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggag";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);
        
        SequenceAnnotation an = region.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 176, 280);
        an.addRole(SequenceOntology.PROMOTER);
        an.createAnnotation(GB_GENE, "bla");
        
        an = region.createSequenceAnnotation("AmpR", "AmpR", 281, 1141);
        an.addRole(SequenceOntology.CDS);    
        an.createAnnotation(SBH_DESCRIPTION, 
                "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        an.createAnnotation(GB_GENE,"bla");
        an.createAnnotation(GB_PRODUCT,"beta-lactamase");        
        
        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        originD.createAnnotation(SBH_DESCRIPTION, "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");
        
        seqStr = "TTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAA"; 
        seq = doc.createSequence("ori_seq", version, seqStr, Sequence.IUPAC_DNA);
        originD.addSequence(seq);
        
        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        
        an = region.createSequenceAnnotation("ori", "ori", 1312, 1900);
        an.setComponent(origin.getIdentity());
        return region;
    }

    ComponentDefinition createBarcode(SBOLDocument doc, String version) throws SBOLValidationException {
        ComponentDefinition barcode = doc.createComponentDefinition("barcode", version, ComponentDefinition.DNA_REGION);
        barcode.addRole(SO("SO:0000730"));
        
        //String seqStr ="NNNNNNNNNNNNNNNNNN";
        //Sequence seq = doc.createSequence(barcode+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        //region.addSequence(seq);
        return barcode;        
    }    
    
    ComponentDefinition createInsert(SBOLDocument doc, String version) throws SBOLValidationException {
        
        String name = "insert";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.setName("insert");
        region.setDescription("transgene insert with codA and Km");
        //region.createAnnotation(SBH_DESCRIPTION,"transgene insert with codA and Km");        
        region.addRole(SO("SO:0000915"));
        
        String seqStr ="ATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCgtaa";
        Sequence seq = doc.createSequence(name+"_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        // where the left flank ends, so the possitions are 1 based (numbers come from snapgene)
        final int SS = 2657;
        

        SequenceAnnotation an = region.createSequenceAnnotation("J23101MH_prom", "J23101MH_prom", 2725-SS, 2813-SS);
        an.addRole(SequenceOntology.PROMOTER); 
        an.setName("J23101MH prom");
        
        ComponentDefinition codA = doc.createComponentDefinition("codA", version, ComponentDefinition.DNA_REGION);
        codA.addRole(SequenceOntology.CDS);
        codA.createAnnotation(SBH_DESCRIPTION, "Codon optimised (V153A, F317C)  ndoi:10.1111/tpj.12675");
        
        seqStr = "ATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGA";
        seq = doc.createSequence("codA_seq", version, seqStr, Sequence.IUPAC_DNA);
        codA.addSequence(seq);

        Component codAI = region.createComponent("codA_inst", AccessType.PUBLIC, codA.getIdentity());
        
        an = region.createSequenceAnnotation("codA", "codA", 2814-SS, 4097-SS);
        an.setComponent(codAI.getIdentity());
        
        an = region.createSequenceAnnotation("rrnBT1_T7_term", "rrnBT1_T7_term", 4102-SS, 4216-SS);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("rrnBT1/T7 term");
        an.createAnnotation(SBH_DESCRIPTION, 
                "BBa_B0015 double terminator (B0010-B0012) pC0.082 https://doi.org/10.1104/pp.18.01401");
        
        an = region.createSequenceAnnotation("KanR_term", "KanR_term", 4221-SS, 4504-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.TERMINATOR);
        an.setName("KanR term");
        
        an = region.createSequenceAnnotation("KanR", "KanR", 4505-SS, 5320-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.CDS); 
        an.createAnnotation(SBH_DESCRIPTION,
                "Gene: aph(3')-Ia aminoglycoside phosphotransferase confers resistance to kanamycin "
                        + "in bacteria or G418 (Geneticin(R)) in eukaryotes");
        
        an.createAnnotation(GB_GENE, 
                "aph(3')-Ia");
        an.createAnnotation(GB_PRODUCT, 
                "aminoglycoside phosphotransferase");

        an = region.createSequenceAnnotation("KanR_prom", "KanR_prom", 5321-SS, 5430-SS, OrientationType.REVERSECOMPLEMENT);
        an.addRole(SequenceOntology.PROMOTER);
        an.setName("KanR prom");
        
        return region;
    }
    
    ComponentDefinition createLeftFlank(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "left_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //site_specific_recombination_target_region‘
        region.addRole(SO("SO:0000342"));
        return region;
    }

    ComponentDefinition createRightFlank(SBOLDocument doc, String version) throws SBOLValidationException {

        String name = "right_flank";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        //site_specific_recombination_target_region‘
        region.addRole(SO("SO:0000342"));
        return region;
    }
    
    
}
