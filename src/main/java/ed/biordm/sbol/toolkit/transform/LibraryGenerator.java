/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import ed.biordm.sbol.toolkit.meta.ExcelMetaReader;
import ed.biordm.sbol.toolkit.meta.MetaFormat;
import ed.biordm.sbol.toolkit.meta.MetaHelper;
import ed.biordm.sbol.toolkit.meta.MetaRecord;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_DESCRIPTION;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_NOTES;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.saveValidSbol;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLConversionException;
/**
 *
 * @author tzielins
 */
public class LibraryGenerator {
 
    final ExcelMetaReader metaReader = new ExcelMetaReader();
    final ComponentUtil util = new ComponentUtil();
    final MetaHelper metaHelper = new MetaHelper();
    final TemplateTransformer transformer = new TemplateTransformer(); 
    final ComponentAnnotator annotator = new ComponentAnnotator();
    
    public static final int DEF_BATCH = 100;
    public boolean DEBUG = true;
    
    public Outcome generateFromFiles(String fileName, String defVersion, Path templateFile, Path metaFile, 
            Path outDir, boolean stopOnMissingMeta) throws IOException {
        
        return generateFromFiles(fileName, defVersion, templateFile, metaFile, outDir, stopOnMissingMeta, 0);
    }
    
    public Outcome generateFromFiles(String fileName, String defVersion, Path templateFile, Path metaFile, 
            Path outDir, boolean stopOnMissingMeta, int batchSize) throws IOException {
        
        MetaFormat metaFormat = metaReader.readMetaFormat(metaFile);
        validateMetaFormat(metaFormat);
        
        List<MetaRecord> metaData = metaReader.readMeta(metaFile, metaFormat);
        metaData = metaHelper.calculateIdFromKey(metaData);
        
        Outcome status = checkMissingMeta(metaData, metaFormat);
        validateCompletness(status, stopOnMissingMeta);
        
        if (batchSize < 1) batchSize = metaData.size();        
        List<List<MetaRecord>> batches = splitMeta(metaData, batchSize);
        
        // needs synchornization
        AtomicInteger counter = new AtomicInteger(0);
        List<String> completed = Collections.synchronizedList(new ArrayList<>());
        
        batches.parallelStream().forEach( batch -> {

            int nr = counter.incrementAndGet();
            Outcome outcome = new Outcome();            
            
            long sT = System.currentTimeMillis();
            if (DEBUG) {
                System.out.println("Generating "+nr+"/"+batches.size()+"...");
            }
            
            SBOLDocument doc = generateOneBatchFromFile(templateFile, batch, defVersion, outcome);
            saveBatch(doc, fileName, nr, outDir);
            completed.addAll(outcome.successful);
            
            long dur = (System.currentTimeMillis()-sT)/1000;
            if (DEBUG) {
                System.out.println("Generated "+nr+"/"+batches.size()+" in "+dur+"s");                
            }
        });
        
        status.successful.addAll(completed);
        return status;
    }
    
    //must be synchronized caouse of validation
    synchronized void saveBatch(SBOLDocument doc, String fileName, int batchNr, Path outDir) {
        
        try {
            Path file = outDir.resolve(fileName+"."+batchNr+".xml");
            saveValidSbol(doc, file);
        } catch (IOException|SBOLConversionException e) {
            throw new IllegalStateException(e);
        }
    }

    SBOLDocument generateOneBatchFromFile(Path templateFile, List<MetaRecord> meta, String defVersion, Outcome outcome) {

        SBOLDocument doc = newFromTemplate(templateFile);
        ComponentDefinition template = util.extractRootComponent(doc);
        List<String> generated = generateFromTemplate(template,  meta, defVersion, doc);
        outcome.successful = generated;
        return doc;
    }
    
    public List<String> generateFromTemplate(ComponentDefinition template, List<MetaRecord> metas, String defVersion, SBOLDocument doc) {
        
        List<String> generated = new ArrayList<>();
        
        for (MetaRecord meta: metas) {
            ComponentDefinition newComp = instantiateComponent(template, meta, defVersion, doc);
            describeComponent(newComp, meta);
            generated.add(newComp.getDisplayId());
        }
        return generated;
    }
    
    
    SBOLDocument newFromTemplate(Path templateFile) {
        try {
            SBOLDocument template = SBOLReader.read(templateFile.toFile());
            template.setDefaultURIprefix(CommonAnnotations.BIORDM_PREF);
            template.setComplete(true);
            template.setCreateDefaults(true);
            return template;
        } catch (SBOLValidationException |SBOLConversionException| IOException e ) {
            throw new IllegalStateException(e);
        }
    }

    void validateMetaFormat(MetaFormat metaFormat) {
        if (metaFormat.displayId.isEmpty())
            throw new IllegalArgumentException("DisplayId must be present in the meta description table");
    }    

    Outcome checkMissingMeta(List<MetaRecord> metaData, MetaFormat metaFormat) {

        long emptyIds = metaData.stream()
                .map( meta -> meta.displayId)
                .filter( disp -> disp.isEmpty() || disp.get().isBlank())
                .count();
        
        if (emptyIds > 0) throw new IllegalArgumentException("Meta descriptin table has records with missing DisplayId");
        
        
        List<String> missingMeta = metaHelper.missingMetaIds(metaData, metaFormat);
                
        Outcome status = new Outcome();
        status.missingMeta = missingMeta;
        
        return status;
    }
    
    void validateCompletness(Outcome status, boolean stopOnMissingMeta) {
        
        if (stopOnMissingMeta && !status.missingMeta.isEmpty()) {
            throw new IllegalArgumentException("Missing metadata fileds for records: "+metaHelper.shortList(status.missingMeta));
        }
    }    

    List<List<MetaRecord>> splitMeta(Collection<MetaRecord> items, int batchSize) {
        
        List<MetaRecord> sorted = items.parallelStream()
                .sorted(Comparator.comparing(m -> m.displayId.get()))
                .collect(Collectors.toList());
                    
        
        List<List<MetaRecord>> batches = new ArrayList<>();
        for (int start = 0; start < sorted.size(); start+=batchSize) {
            int end = Math.min(start+batchSize, sorted.size());
            batches.add(sorted.subList(start, end));
        }
        return batches;
    }    

    ComponentDefinition instantiateComponent(ComponentDefinition template, MetaRecord meta, String defVersion, SBOLDocument doc)  {
        
        
        String displayId = meta.displayId.get();
        String version = meta.version.orElse(defVersion);
        
        try {
            ComponentDefinition instance = transformer.instantiateFromTemplate(template, displayId, version, "", doc);    
            //otherwise they are not root
            instance.clearWasDerivedFroms();
        
            for (String genCompId : meta.extras.keySet()) {
                String newSeq = meta.extras.getOrDefault(genCompId, "");
                if (newSeq.isBlank()) continue; //ignoring creation of components with empty sequences
                
                if (instance.getComponent(genCompId) == null) 
                    throw new IllegalArgumentException("No component instance: "+genCompId+" in the template design");
                
                String newName = displayId+"_"+genCompId; //that should be unique
                
                transformer.concretizePart(instance, genCompId, newName, newSeq, doc);
            }
            
        
            return instance;
        } catch (SBOLValidationException e) {
            throw new IllegalStateException("Could not instantiate component "+displayId+": "+e.getMessage(),e);
        }
    }

    void describeComponent(ComponentDefinition component, MetaRecord meta) {

        instantiesExistingDescriptions(component, meta);
        
        annotator.annotateComponent(component, meta, false);
        
    }
    
    void instantiesExistingDescriptions(ComponentDefinition component, MetaRecord meta) {

        String displayId = component.getDisplayId();
        String key = meta.key.orElse("");
        
        annotator.setName(component, meta.name, displayId, key);
        String name = component.getName() != null ? component.getName() : "";
        
        annotator.addSummary(component, Optional.ofNullable(component.getDescription()), true, displayId, key, name);
        
        Optional<String> tmp = Optional.ofNullable(util.getAnnotationValue(component, SBH_DESCRIPTION));
        annotator.addDescription(component, tmp, true, displayId, key, name);
        
        tmp = Optional.ofNullable(util.getAnnotationValue(component, SBH_NOTES));
        annotator.addNotes(component, tmp, true, displayId, key, name);
    }    

}
