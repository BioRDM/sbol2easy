/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for operations on MetaRecords.
 * @author tzielins
 */
public class MetaHelper {
    
   
    public static String setTemplateVariable(String key, String value, String template) {
        
        String pattern = "\\{"+key+"}";
        return template.replaceAll(pattern, value);
    }
    
    public List<MetaRecord> calculateIdFromKey(List<MetaRecord> records) {
        for (MetaRecord record: records) {
            String key = record.key.orElse("");
            String displayId = record.displayId.orElse("");
            displayId = setTemplateVariable("key", key, displayId);
            record.displayId = Optional.of(displayId);
        }
        return records;
    }
    
    public List<String> missingMetaIds(List<MetaRecord> metaData, MetaFormat metaFormat) {
        return metaData.stream()
                .filter( meta -> emptyMeta(meta, metaFormat))
                .map( meta -> meta.displayId.get())
                .collect(Collectors.toList());        
    }
    
    public boolean emptyMeta(MetaRecord meta, MetaFormat format) {
        
        if (format.displayId.isPresent() && emptyVal(meta.displayId)) return true;
        if (format.version.isPresent() && emptyVal(meta.version)) return true;
        if (format.name.isPresent() && emptyVal(meta.name)) return true;
        if (format.summary.isPresent() && emptyVal(meta.summary)) return true;
        if (format.key.isPresent() && emptyVal(meta.key)) return true;
        if (format.description.isPresent() && emptyVal(meta.description)) return true;
        if (format.notes.isPresent() && emptyVal(meta.notes)) return true;
        if (format.attachment.isPresent() && emptyVal(meta.attachment)) return true;
        
        if (format.authors.size() != meta.authors.size()) return true;
        if (format.extras.size() != meta.extras.size()) return true;
        //blank values
        if (    meta.extras.values().stream().anyMatch(s -> s.isBlank()) ) return true;
        return false;
        
    }
    
    public boolean emptyVal(Optional<String> val) {
        return (val.isEmpty() || val.get().isBlank());
    }
    
    public String shortList(List<String> vals) {
        String s = vals.stream().limit(5)
                .collect(Collectors.joining(","));
        if (vals.size() > 5) s +="...";
        return s;
    }
    
    
}
