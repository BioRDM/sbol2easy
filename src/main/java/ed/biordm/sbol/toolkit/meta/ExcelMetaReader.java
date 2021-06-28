/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.meta;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author tzielins
 */
public class ExcelMetaReader {
    
    // Excel column header strings - worksheet headers must match these to work
    public static final String DISP_ID_HEADER = "display_id";
    public static final String NAME_HEADER = "name";
    public static final String VARIABLE_HEADER = "variable";    
    public static final String AUTHOR_HEADER = "author";    
    public static final String DESC_HEADER = "description";
    public static final String NOTES_HEADER = "notes";    
    public static final String ATTACH_FILE_HEADER = "attachment_filename";
    
    public MetaLocations readMetaDefiniton(Path file) throws IOException {
        
        return readMetaDefiniton(file, 0);
    }
    
    public MetaLocations readMetaDefiniton(Path file, int sheet) throws IOException {
        
        ExcelReader excel = new ExcelReader();
        List<String> header = excel.readStringRow(file, sheet, 0);
        MetaLocations def = parseHeader(header);
        return def;
    }

    public List<MetaRecord> readMeta(Path file) throws IOException {
        
        MetaLocations locations = readMetaDefiniton(file);
        return readMeta(file, locations, 0, 1);
    }
    
    
    public List<MetaRecord> readMeta(Path file, MetaLocations locations) throws IOException {
        
        return readMeta(file, locations, 0, 1);
    }
    
    public List<MetaRecord> readMeta(Path file, MetaLocations locations, int sheet, int skip) throws IOException {
        
        ExcelReader excel = new ExcelReader();
        List<List<String>> rows = excel.readStringRows(file, 0, 1, locations.cols);
        
        return rows.stream()
                    .filter( row -> isListNotEmpty(row))
                    .map(row -> parseMeta(row, locations))
                    .collect(Collectors.toList());
    }
    
    MetaRecord parseMeta(List<String> row, MetaLocations locations) {
        
        MetaRecord meta = new MetaRecord();
        
        meta.displayId = locationToValue(locations.displayId, row);
        meta.name = locationToValue(locations.name, row);
        meta.variable = locationToValue(locations.variable, row);
        meta.description = locationToValue(locations.description, row);
        meta.notes = locationToValue(locations.notes, row);
        meta.attachment = locationToValue(locations.attachment, row);
        
        meta.authors = locations.authors.stream()
                            .map(ix -> row.get(ix))
                            .collect(Collectors.toList());
        
        locations.extras.forEach((label, ix) -> meta.extras.put(label, row.get(ix)));
        
        return meta;
    }
    
    Optional<String> locationToValue(Optional<Integer> loc, List<String> vals) {
        return loc.map( ix -> vals.get(ix));
    }

    MetaLocations parseHeader(List<String> header) {
        
        MetaLocations meta = new MetaLocations();
        
        header = trim(header);
        for (int i = 0; i< header.size(); i++) {
            String label = header.get(i);
            if (label.isBlank()) continue;
            
            switch(label) {
                case DISP_ID_HEADER: meta.displayId = Optional.of(i); break;
                case NAME_HEADER: meta.name = Optional.of(i); break;
                case VARIABLE_HEADER: meta.variable = Optional.of(i); break;
                case DESC_HEADER: meta.description = Optional.of(i); break;
                case NOTES_HEADER: meta.notes = Optional.of(i); break;
                case ATTACH_FILE_HEADER: meta.attachment = Optional.of(i); break;
                case AUTHOR_HEADER: meta.authors.add(i); break;
                default: meta.extras.put(label, i);
            }
        }
        
        meta.cols = header.size();
        
        return meta;
    }
    
    List<String> trim(List<String> list) {
        int last = list.size()-1;
        for (;last >= 0; last--) {
            if (list.get(last) != null && !list.get(last).isBlank()) break;
        }
        if (last >= 0) return list.subList(0, last+1);
        return List.of();
    }
    
    boolean isListNotEmpty(List<String> list) {
        
        for (String item: list) {
            if (item != null && !item.isBlank()) return true;
        }
        return false;
    }
    
    
}
