/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class GenBank2SO {
    
    
    final Map<String,URI> features2terms;
    final Map<String,String> terms2features;
    
    public GenBank2SO() {
        this(readDefaultConversion());
    }
    public GenBank2SO(Map<String,String> features2terms) {
        this(features2terms, invert(features2terms));
    }

    public GenBank2SO(Map<String,String> features2terms, Map<String,String> terms2features) {
        
        
        this.features2terms = terms2URIs(features2terms);
        this.terms2features = new HashMap<>();
        terms2features.forEach( (term, value) -> {
            this.terms2features.put(term.toUpperCase(), value);
        });
        
    }
    
    public URI featureToTerm(String feature) {
        URI term = this.features2terms.get(feature.toLowerCase());
        if (term != null) return term;
        return SequenceOntology.SEQUENCE_FEATURE;
    }
    
    public String termToFeature(URI term) {
        return termToFeature(term.toString());
    }
    
    public String termToFeature(String term) {
        if (!term.startsWith("so") && !term.startsWith("SO")) {
            term = extractTerm(term);
        }
        term = term.toUpperCase();
        String feature = this.terms2features.get(term);
        if (feature != null) return feature;
        return "misc_feature";
    }
    
    protected Map<String,URI> terms2URIs(Map<String,String> features2terms) {
        SequenceOntology so = new SequenceOntology();
        HashMap<String, URI> map = new HashMap<>();
        features2terms.forEach((key, value) -> {
        
            map.put(key.toLowerCase(), so.getURIbyId(value));
        });        
        
        return map;
    }
    
    /*
    protected static Map<String,String> lowerKeys(Map<String,String> terms) {
        Map<String,String> out = new HashMap<>();
        terms.forEach( (key, value) -> {
            out.put(key.toLowerCase(), value);
        });
        return out;
    }*/
    
    protected static Map<String,String> invert(Map<String,String> terms) {
        
        Map<String,String> out = new HashMap<>();
        terms.forEach( (key, value) -> {
            out.putIfAbsent(value, key);
        });
        return out;
    }
    
    protected static LinkedHashMap<String,String> readDefaultConversion() {
        
        URL file = GenBank2SO.class.getResource("genbank2SO.csv");
        
        if (file == null ) {
            throw new IllegalStateException("Missing default ontology file");
        }
        
        try (Stream<String> lines = Files.lines(Paths.get(file.toURI()))) {
            
            LinkedHashMap<String,String> map = new LinkedHashMap<>();

            lines.skip(1)
                 .map(s -> s.split(","))
                 .filter( tokens -> tokens.length >= 1)
                 .filter( tokens -> !tokens[0].trim().isEmpty())
                 .forEach( tokens -> {
                     map.put(tokens[0], tokens[1]);
                 });
            
            return map;
            
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Cannot read default genbank mapping: "+e.getMessage(),e);
        }
    }

    protected String extractTerm(String term) {
        term = term.toUpperCase();
        int ix = term.lastIndexOf("SO:");
        if (ix < 0) {
            throw new IllegalArgumentException("Not a Sequence Ontology term: "+term);
        }
        return term.substring(ix);
    }
}
