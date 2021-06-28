/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author tzielins
 */
public class MetaFormat {
    
    public Optional<Integer> displayId = Optional.empty();
    public Optional<Integer> version = Optional.empty();
    public Optional<Integer> name = Optional.empty();
    public Optional<Integer> summary = Optional.empty();    
    public Optional<Integer> description = Optional.empty();
    public Optional<Integer> notes = Optional.empty();
    public Optional<Integer> variable = Optional.empty();
    public Optional<Integer> attachment = Optional.empty();
    public List<Integer> authors = new ArrayList<>();
    
    public Map<String,Integer> extras = new HashMap<>();
    
    int cols = 0;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.displayId);
        hash = 47 * hash + Objects.hashCode(this.version);        
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.summary);
        hash = 47 * hash + Objects.hashCode(this.description);
        hash = 47 * hash + Objects.hashCode(this.notes);
        hash = 47 * hash + Objects.hashCode(this.variable);
        hash = 47 * hash + Objects.hashCode(this.attachment);
        hash = 47 * hash + Objects.hashCode(this.authors);
        hash = 47 * hash + Objects.hashCode(this.extras);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetaFormat other = (MetaFormat) obj;
        if (this.cols != other.cols) {
            return false;
        }
        if (!Objects.equals(this.displayId, other.displayId)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.summary, other.summary)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.variable, other.variable)) {
            return false;
        }
        if (!Objects.equals(this.attachment, other.attachment)) {
            return false;
        }
        if (!Objects.equals(this.authors, other.authors)) {
            return false;
        }
        if (!Objects.equals(this.extras, other.extras)) {
            return false;
        }
        return true;
    }
    
    
    
}
