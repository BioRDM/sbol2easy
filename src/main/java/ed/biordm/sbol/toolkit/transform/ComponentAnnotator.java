/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import ed.biordm.sbol.toolkit.meta.ExcelMetaReader;
import ed.biordm.sbol.toolkit.meta.MetaFormat;
import ed.biordm.sbol.toolkit.meta.MetaRecord;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.CREATOR;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_DESCRIPTION;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_NOTES;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 *
 * @author tzielins
 */
public class ComponentAnnotator {
    
    final ExcelMetaReader metaReader = new ExcelMetaReader();
    final ComponentUtil util = new ComponentUtil();
    
    public Outcome annotate(SBOLDocument source, Path metaFile, boolean overwriteDesc, 
            boolean stopOnMissingId, boolean stopOnMissingMeta) throws IOException {
        
        MetaFormat metaFormat = metaReader.readMetaFormat(metaFile);
        validateMetaFormat(metaFormat);
        
        List<MetaRecord> metaData = metaReader.readMeta(metaFile, metaFormat);
        
        Map<String,List<String>> idsWithVersions = util.extractComponentsVersionedDisplayIds(source);
        
        Outcome status = checkMissingMeta(idsWithVersions, metaData, metaFormat);
        validateCompletness(status, stopOnMissingId, stopOnMissingMeta);
        
        return annotate(source, idsWithVersions, metaData, metaFormat, overwriteDesc, status);
        
    }

    void validateMetaFormat(MetaFormat metaFormat) {
        if (metaFormat.displayId.isEmpty())
            throw new IllegalArgumentException("DisplayId must be present in the meta description table");
    }

    Outcome checkMissingMeta(Map<String,List<String>> idsWithVersions, List<MetaRecord> metaData, MetaFormat metaFormat) {
        
        long emptyIds = metaData.stream()
                .map( meta -> meta.displayId)
                .filter( disp -> disp.isEmpty() || disp.get().isBlank())
                .count();
        
        if (emptyIds > 0) throw new IllegalArgumentException("Meta descriptin table has records with missing DisplayId");
        
        
        List<String> missingIds = metaData.stream()
                .filter( meta -> missingIds(meta, idsWithVersions))
                .map( meta -> meta.displayId.get())
                .collect(Collectors.toList());
        
        List<String> missingMeta = metaData.stream()
                .filter( meta -> emptyMeta(meta, metaFormat))
                .map( meta -> meta.displayId.get())
                .collect(Collectors.toList());
                
        Outcome status = new Outcome();
        status.missingId = missingIds;
        status.missingMeta = missingMeta;
        
        return status;
    }
    
    boolean missingIds(MetaRecord meta, Map<String,List<String>> idsWithVersions) {
        List<String> versions = idsWithVersions.get(meta.displayId.get());
        if (versions == null) return true;
        if (meta.version.isEmpty()) return false;
        return !versions.contains(meta.version.get());
    }
    
    boolean emptyMeta(MetaRecord meta, MetaFormat format) {
        
        if (format.displayId.isPresent() && emptyVal(meta.displayId)) return true;
        if (format.version.isPresent() && emptyVal(meta.version)) return true;
        if (format.name.isPresent() && emptyVal(meta.name)) return true;
        if (format.summary.isPresent() && emptyVal(meta.summary)) return true;
        if (format.variable.isPresent() && emptyVal(meta.variable)) return true;
        if (format.description.isPresent() && emptyVal(meta.description)) return true;
        if (format.notes.isPresent() && emptyVal(meta.notes)) return true;
        if (format.attachment.isPresent() && emptyVal(meta.attachment)) return true;
        
        if (format.authors.size() != meta.authors.size()) return true;
        if (format.extras.size() != meta.extras.size()) return true;
        //blank values
        if (    meta.extras.values().stream().anyMatch(s -> s.isBlank()) ) return true;
        return false;
        
    }
    
    boolean emptyVal(Optional<String> val) {
        return (val.isEmpty() || val.get().isBlank());
    }

    void validateCompletness(Outcome status, boolean stopOnMissingId, boolean stopOnMissingMeta) {
        
        if (stopOnMissingId && !status.missingId.isEmpty()) {
            throw new IllegalArgumentException("Missing designs ids in the input document: "+shortList(status.missingId));
        }
        
        if (stopOnMissingMeta && !status.missingMeta.isEmpty()) {
            throw new IllegalArgumentException("Missing metadata fileds for records: "+shortList(status.missingMeta));
        }
    }
    
    String shortList(List<String> vals) {
        String s = vals.stream().limit(5)
                .collect(Collectors.joining(","));
        if (vals.size() > 5) s +="...";
        return s;
    }

    Outcome annotate(SBOLDocument source, Map<String, List<String>> idsWithVersions, List<MetaRecord> metaData, MetaFormat metaFormat, boolean overWrite, Outcome status) {

        for (MetaRecord record : metaData) {
            String displayId = record.displayId.get();
            if (!idsWithVersions.containsKey(displayId)) continue;
            
            String version = (idsWithVersions.get(displayId).get(0));
            if (record.version.isPresent() && !record.version.get().isBlank()) {
                version = record.version.get();
            }
            
            ComponentDefinition component = source.getComponentDefinition(displayId, version);
            if (component == null) continue;
            
            annotateComponent(component, record, metaFormat, overWrite);
            
            status.successful.add(displayId);
        }

        return status;
    }

    void annotateComponent(ComponentDefinition component, MetaRecord meta, MetaFormat metaFormat, boolean overWriteDesc) {
        
        String displayId = component.getDisplayId();
        String variable = meta.variable.orElse("");
        
        setName(component, meta.name, displayId, variable);
        String name = component.getName() != null ? component.getName() : "";
        
        addAuthors(component, meta.authors);
        
        setSummary(component, meta.summary, displayId, variable, name);
        
        addDescription(component, meta.description, overWriteDesc, displayId, variable, name);
        
        addNotes(component, meta.notes, overWriteDesc, displayId, variable, name);
        
    }

    protected String setTemplateVariable(String variable, String value, String template) {
        
        String pattern = "\\{"+variable+"}";
        return template.replaceAll(pattern, value);
    }
    
    void setName(ComponentDefinition component, Optional<String> name, String displayId, String variable) {
        if (name.isEmpty()) return;
        
        String template = name.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("variable", variable, template);
        
        component.setName(template);
    }

    void addAuthors(ComponentDefinition component, List<String> authors) {
        
        for (String author: authors) {
            addAuthor(component, author);
        }
    }
    
    void addAuthor(ComponentDefinition component, String author) {
        if (author.isBlank()) return;
        
        util.addAnnotation(component, CREATOR, author);
    }
    
    void setSummary(ComponentDefinition component, Optional<String> summary, String displayId, String variable, String name) {
        
        if (summary.isEmpty()) return;
        
        String template = summary.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("variable", variable, template);
        template = setTemplateVariable("name", name, template);

        component.setDescription(template);
    }

    void addDescription(ComponentDefinition component, Optional<String> description, boolean overwrite, String displayId, String variable, String name) {
        
        if (description.isEmpty()) return;
        
        String template = description.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("variable", variable, template);
        template = setTemplateVariable("name", name, template);

        if (overwrite) {
            util.setAnnotation(component, SBH_DESCRIPTION, template);
        } else {
            util.appendAnnotation(component, SBH_DESCRIPTION, template);            
        }
    }

    void addNotes(ComponentDefinition component, Optional<String> notes, boolean overwrite, String displayId, String variable, String name) {
        if (notes.isEmpty()) return;
        
        String template = notes.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("variable", variable, template);
        template = setTemplateVariable("name", name, template);

        if (overwrite) {
            util.setAnnotation(component, SBH_NOTES, template);
        } else {
            util.appendAnnotation(component, SBH_NOTES, template);            
        }
    }

    
    
    
    public static class Outcome {
        public List<String> successful = new ArrayList<>();
        public List<String> missingId = new ArrayList<>();
        public List<String> missingMeta = new ArrayList<>();
        
    }
}
