/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.scrapbook;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Tomasz Zielinski
 */
public class TrevorMetaReader {
    
    
    Map<String,String[]> descriptions = Map.of();
    
    TrevorMetaReader() {
        
    }
    
    public TrevorMetaReader(Path file, int skip) {
        init(file, skip);
    }
    
    public void init(Path file, int skip) {
        descriptions = parse(file, skip);
    }
    
    public Set<String> ids() {
        return descriptions.keySet();
    }
    
    public String[] meta(String id) {
        String[] meta = descriptions.get(id);
        if (meta == null) throw new IllegalArgumentException("Missing id "+id);
        return meta;
    }
    
    public String summary(String id) {
        String[] meta = meta(id);
        return meta[9];
    }
    
    public String creator(String id) {
        String[] meta = meta(id);
        return meta[14];
    }
    
    public String pi(String id) {
        String[] meta = meta(id);
        return meta[1];
    }
    

    
    
    public String originOfRep(String id) {
        String[] meta = meta(id);
        
        return meta[20];
    }    
    
    public String selection(String id) {
        String[] meta = meta(id);
        
        return meta[21];
    }   
    
    public String backbone(String id) {
        String[] meta = meta(id);
        
        return meta[17];
    }     
    
    public String replicatesIn(String id) {
        String[] meta = meta(id);
        
        return meta[19];
    }     
    
    public String description(String id) {
        String[] meta = meta(id);
        
        String desc = ""+summary(id) +"\n";
        desc += meta[10];
        return desc;
    }    
    
    
    public String notes(String id) {
        String[] meta = meta(id);
        
        String desc = "";
        desc += "Creator: "+creator(id) +"\n";
        desc += "Principal Investigator: "+pi(id) +"\n";
        desc += "BioSafety Level: "+meta[5] +"\n";
        desc += "Backbone: "+backbone(id) +"\n";
        desc += "Origin of replication: "+originOfRep(id) +"\n";
        desc += "Selection Markers: "+selection(id) +"\n";
        return desc;
    }    
    
    public Map<String,String[]> parse(Path file, int skip) {
        
        List<String[]> rows = read(file, skip);
        return rows.stream().collect(Collectors.toMap(t -> t[0].trim(), t -> t));
    }    
    
    List<String[]> read(Path file, int skip) {
        
        try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(file))) {
            
            csvReader.skip(skip);
            return csvReader.readAll();
            
        } catch (IOException| CsvException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
