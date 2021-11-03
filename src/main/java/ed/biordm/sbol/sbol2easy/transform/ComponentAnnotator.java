/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import ed.biordm.sbol.sbol2easy.meta.ExcelMetaReader;
import ed.biordm.sbol.sbol2easy.meta.MetaFormat;
import ed.biordm.sbol.sbol2easy.meta.MetaHelper;
import static ed.biordm.sbol.sbol2easy.meta.MetaHelper.setTemplateVariable;
import ed.biordm.sbol.sbol2easy.meta.MetaRecord;
import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.CREATOR;
import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.SBH_DESCRIPTION;
import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.SBH_NOTES;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 * Utility class that can set descriptive properties of component definitions.
 * It can handle, name, summary (sbol description), sbh notes , 
 * sbh mutable description and creators/authors
 * @author tzielins
 */
public class ComponentAnnotator {
    
    final ComponentUtil util = new ComponentUtil();
    final MetaHelper metaHelper = new MetaHelper();
    
    public Outcome annotate(SBOLDocument source, Path metaFile, boolean overwriteDesc, 
            boolean stopOnMissingId, boolean stopOnMissingMeta) throws IOException {
        
        ExcelMetaReader metaReader = new ExcelMetaReader();
        
        MetaFormat metaFormat = metaReader.readMetaFormat(metaFile);
        validateMetaFormat(metaFormat);
        
        List<MetaRecord> metaData = metaReader.readMeta(metaFile, metaFormat);
        metaData = metaHelper.calculateIdFromKey(metaData);
        
        Map<String,List<String>> idsWithVersions = util.extractComponentsVersionedDisplayIds(source);
        
        Outcome status = checkMissingMeta(idsWithVersions, metaData, metaFormat);
        validateCompletness(status, stopOnMissingId, stopOnMissingMeta);
        
        return annotate(source, idsWithVersions, metaData, metaFormat, overwriteDesc, status);
        
    }
    
    /**
     * Adds metadescripton to the component. 
     * It can handle simple templating in the description/notes values.
     * It sets name, summary, description, notes, creators
     * @param component definition to be annotated
     * @param meta attributes to be set (missing fields are ignored)
     * @param overWriteDesc if false the new values are appended to existing notes and description
     */
    public void annotateComponent(ComponentDefinition component, MetaRecord meta, boolean overWriteDesc) {
        
        String displayId = component.getDisplayId();
        String key = meta.key.orElse("");
        
        setName(component, meta.name, displayId, key);
        String name = component.getName() != null ? component.getName() : "";
        
        addAuthors(component, meta.authors);
        
        addSummary(component, meta.summary, overWriteDesc, displayId, key, name);
        
        addDescription(component, meta.description, overWriteDesc, displayId, key, name);
        
        addNotes(component, meta.notes, overWriteDesc, displayId, key, name);
        
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
        
        List<String> missingMeta = metaHelper.missingMetaIds(metaData, metaFormat);
                
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
    


    void validateCompletness(Outcome status, boolean stopOnMissingId, boolean stopOnMissingMeta) {
        
        if (stopOnMissingId && !status.missingId.isEmpty()) {
            throw new IllegalArgumentException("Missing designs ids in the input document: "+metaHelper.shortList(status.missingId));
        }
        
        if (stopOnMissingMeta && !status.missingMeta.isEmpty()) {
            throw new IllegalArgumentException("Missing metadata fileds for records: "+metaHelper.shortList(status.missingMeta));
        }
    }
    

    Outcome annotate(SBOLDocument source, Map<String, List<String>> idsWithVersions, List<MetaRecord> metaData, MetaFormat metaFormat, boolean overWrite, Outcome status) {

        if (source.getDefaultURIprefix() == null || source.getDefaultURIprefix().isBlank()) {
            source.setDefaultURIprefix(CommonAnnotations.BIORDM_PREF);
        }
        
        for (MetaRecord record : metaData) {
            String displayId = record.displayId.get();
            if (!idsWithVersions.containsKey(displayId)) continue;
            
            String version = (idsWithVersions.get(displayId).get(0));
            if (record.version.isPresent() && !record.version.get().isBlank()) {
                version = record.version.get();
            }
            
            ComponentDefinition component = source.getComponentDefinition(displayId, version);
            if (component == null) continue;
            
            annotateComponent(component, record, overWrite);
            
            status.successful.add(displayId);
        }

        return status;
    }



    
    
    void setName(ComponentDefinition component, Optional<String> name, String displayId, String key) {
        if (name.isEmpty()) return;
        
        String template = parseTemplate(name.get(), displayId, key);        
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
    
    void addSummary(ComponentDefinition component, Optional<String> summary, boolean overwrite, String displayId, String key, String name) {
        
        if (summary.isEmpty()) return;
        
        String template = summary.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("key", key, template);
        template = setTemplateVariable("name", name, template);

        if (overwrite) {
            component.setDescription(template);
        } else {
            String old = component.getDescription();
            if (old == null) old = "";
            component.setDescription(old+template);
        }
    }

    public final String getDescription(ComponentDefinition component) {
        return util.getDescription(component);
    }
    
    public String parseTemplate(String template, String displayId, String key, String name) {
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("key", key, template);
        template = setTemplateVariable("name", name, template);
        return template;
    }
    
    public String parseTemplate(String template, String displayId, String key) {
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("key", key, template);
        return template;
    }    
    
    public void addDescription(ComponentDefinition component, Optional<String> description, boolean overwrite, String displayId, String key, String name) {
        
        if (description.isEmpty()) return;
        
        String template = description.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("key", key, template);
        template = setTemplateVariable("name", name, template);

        if (overwrite) {
            util.setAnnotation(component, SBH_DESCRIPTION, template);
        } else {
            util.appendAnnotation(component, SBH_DESCRIPTION, template);            
        }
    }

    public final String getNotes(ComponentDefinition component) {
        return util.getNotes(component);
    }
    
    public void addNotes(ComponentDefinition component, Optional<String> notes, boolean overwrite, String displayId, String key, String name) {
        if (notes.isEmpty()) return;
        
        String template = notes.get();
        template = setTemplateVariable("displayId", displayId, template);
        template = setTemplateVariable("key", key, template);
        template = setTemplateVariable("name", name, template);

        if (overwrite) {
            util.setAnnotation(component, SBH_NOTES, template);
        } else {
            util.appendAnnotation(component, SBH_NOTES, template);            
        }
    }

    
    
    
}
