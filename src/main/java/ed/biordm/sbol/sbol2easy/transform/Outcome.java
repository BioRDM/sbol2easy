/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about the processing outcome as series of identifiers for 
 * which on operation succeeded or failed.
 * @author tzielins
 */
public class Outcome {
    
    public List<String> successful = new ArrayList<>();
    public List<String> missingId = new ArrayList<>();
    public List<String> missingMeta = new ArrayList<>();
    public List<String> failed = new ArrayList<>();
    public List<String> ignored = new ArrayList<>();
    
}
