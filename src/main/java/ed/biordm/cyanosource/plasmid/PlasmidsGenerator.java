/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.cyanosource.plasmid;

import static ed.biordm.cyanosource.plasmid.CyanoTemplate.createTemplatePlasmid;
import ed.biordm.sbol.toolkit.transform.FeaturesReader;
import ed.biordm.sbol.toolkit.transform.GenBankConverter;
import ed.biordm.sbol.toolkit.transform.TemplateTransformer;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

/**
 *
 * @author tzielins
 */
public class PlasmidsGenerator {
    
    
    public final String CYANO_PREF = "http://bio.ed.ac.uk/a_mccormick/cyano_source/";
    public int DEF_BATCH = 300;
    
    protected TemplateTransformer transformer = new TemplateTransformer();    
    
    public static void main(String[] args) throws SBOLValidationException, SBOLConversionException, IOException, URISyntaxException, ed.biordm.sbol.toolkit.transform.SBOLConversionException {
        
        Path outDir = Paths.get("E:/Temp/cyanosource_"+LocalDate.now());
        Path flanks = Paths.get("E:/Temp/flank-list_20200821.xlsx");
        
        String version = "1.0";
        String name = "cyano_"+version;
        
        PlasmidsGenerator instance = new PlasmidsGenerator();
        
        instance.generate(name, version, flanks, outDir);
    }
    
    protected void generate(String name, String version, Path flankFile, Path outDir) throws IOException, SBOLValidationException, SBOLConversionException, ed.biordm.sbol.toolkit.transform.SBOLConversionException {
    
        System.out.println("Generating ....");
        List<SBOLDocument> docs = generateFromFile(flankFile, version);
        
        System.out.println("Saving sbols ....");
        Files.createDirectories(outDir);
        
        for (int i = 0; i< docs.size(); i++) {
            Path file = outDir.resolve(name+"_"+i+".sbol");
            saveSbol(docs.get(i), file);            
        }

        System.out.println("Saving genbanks ....");
        
        outDir = outDir.resolve("genbank");
        Files.createDirectories(outDir);
        
        for (SBOLDocument doc: docs) {
            saveFlattened(doc, outDir);
        }

    }
    
    protected List<SBOLDocument> generateFromFile(Path flankFile, String version) throws IOException, SBOLValidationException {
        return generateFromFile(flankFile, version, DEF_BATCH);
    }
    
    protected List<SBOLDocument> generateFromFile(Path flankFile, String version, int batchSize) throws IOException, SBOLValidationException {
        
        Map<String, String> leftFlanks = readSequences(flankFile, 0);
        Map<String, String> rightFlanks = readSequences(flankFile, 1);   
        
        checkCompletness(leftFlanks, rightFlanks);
        
        List<List<String>> batches = splitKeys(leftFlanks.keySet(), batchSize);
        AtomicInteger done = new AtomicInteger(0);
        
        return batches.parallelStream()
                .map( genes -> {
        
                    try {
                        SBOLDocument doc = generatePlasmids(genes, version, leftFlanks, rightFlanks);

                        System.out.println("Generated part "+done.incrementAndGet()+"/"+batches.size());
                        return doc;
                    } catch (SBOLValidationException e) {
                        throw new IllegalStateException(e.getMessage(),e);
                    }
                })
                .collect(Collectors.toList());
    }
    
    protected void checkCompletness(Map<String, String> leftFlanks, Map<String, String> rightFlanks) {
        
        Set<String> missing = new HashSet<>(leftFlanks.keySet());
        missing.removeAll(rightFlanks.keySet());
        
        if (!missing.isEmpty()) {
            System.out.println("Missing right flank for: "+
                    missing.stream().collect(Collectors.joining(", ")));
        }
        
        missing = new HashSet<>(rightFlanks.keySet());
        missing.removeAll(leftFlanks.keySet());
        
        if (!missing.isEmpty()) {
            System.out.println("Missing left flank for: "+
                    missing.stream().collect(Collectors.joining(", ")));
        }    
    }
    
    protected SBOLDocument generatePlasmids(List<String> genes, String version, Map<String, String> leftFlanks, Map<String, String> rightFlanks) throws SBOLValidationException {
        
        
        SBOLDocument doc = cyanoDocument();
        
        ComponentDefinition template = createTemplatePlasmid(doc, version);
        
        for (String gene: genes) {
            
            String lFlankSeq = leftFlanks.get(gene);
            String rFlankSeq = rightFlanks.get(gene);
            if (lFlankSeq == null || rFlankSeq == null) {
                continue;
            }
            addGenne1stGenerationPlasmids(template, gene, lFlankSeq, rFlankSeq, doc, version);            
            
        }
        
        return doc;
    }
    
    
    protected void saveFlattened(SBOLDocument doc, Path dir) throws IOException, ed.biordm.sbol.toolkit.transform.SBOLConversionException {
        
        for (ComponentDefinition comp : doc.getComponentDefinitions()) {
            if (comp.getDisplayId().endsWith("flatten")) {
                Path file = dir.resolve(comp.getDisplayId()+".gb");
                try (Writer out = Files.newBufferedWriter(file)) {
                    GenBankConverter.write(comp, out);
                }
            }
        }
    }
    
    protected void saveSbol(SBOLDocument doc, Path file) throws IOException, SBOLConversionException {
        
        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(doc, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                throw new IllegalStateException("Stoping cause of validation error: "+error);
            }            
        }

        SBOLWriter.write(doc, file.toFile());
    }
    
    protected Map<String, String> readSequences(Path file, int sheet) throws IOException {
        
        FeaturesReader reader = new FeaturesReader();
        
        Map<String, String> orgFeatures = reader.readSimpleFeatures(file, 1, sheet);
        
        Map<String, String> features = new HashMap<>(orgFeatures.size());
        
        orgFeatures.forEach((key, value) -> {
            value = value.trim();
            if (!value.isEmpty()) {
                features.put(extractGene(key), value);
            }
        });
        return features;
    }
    
    
    
    protected SBOLDocument cyanoDocument() {
        SBOLDocument doc = new SBOLDocument();

        doc.setDefaultURIprefix(CYANO_PREF);
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        return doc;                
    }

    void addGenne1stGenerationPlasmids(ComponentDefinition template, String gene, String lFlankSeq, String rFlankSeq,
                                SBOLDocument doc, String version) throws SBOLValidationException {
        
        String description = "";
        ComponentDefinition plasmid = transformer.instantiateFromTemplate(template, gene, version,
                description, doc);
        
        //sll0199.createAnnotation(SBH_DESCRIPTION, "Generate a description for each plasmid, for example\n"
        //        + "Recombinant plasmid targetting sll0199");

        transformer.concretizePart(plasmid, "left", gene+"_left", lFlankSeq, doc);

        transformer.concretizePart(plasmid, "right", gene+"_right", rFlankSeq, doc);

        //to make it top level
        plasmid.clearWasDerivedFroms();
        
        ComponentDefinition flattenPlasmid = transformer.flattenSequences2(plasmid, gene+"_flatten", doc);
        
        //to make it top level
        flattenPlasmid.clearWasDerivedFroms();
        
    }

    protected String extractGene(String key) {
        int ix1 = key.indexOf("_");
        int ix2 = key.lastIndexOf("_");
        if (ix1 < 0 || (ix1+1) >= ix2)
            throw new IllegalArgumentException("Wrong format, expected 0xx0_gene_xxx got: "+key);
        
        return key.substring(ix1+1, ix2);
    }

    protected List<List<String>> splitKeys(Collection<String> keySet, int batchSize) {
        
        List<String> keys = new ArrayList<>(keySet);
        Collections.sort(keys);
        
        List<List<String>> batches = new ArrayList<>();
        for (int start = 0; start < keys.size(); start+=batchSize) {
            int end = Math.min(start+batchSize, keys.size());
            batches.add(keys.subList(start, end));
        }
        return batches;
    }
}
