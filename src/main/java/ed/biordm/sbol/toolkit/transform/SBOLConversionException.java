/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

/**
 *
 * @author jhay
 */
public class SBOLConversionException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception instance with the given message and objects causing the problem.
     * @param message
     * @param objects
     */
    SBOLConversionException(String message) {
            super(message);
    }

    /**
     * Creates a new exception instance with the given cause but no specific objects for the problem.
     * 
     * @param cause
     */	
    SBOLConversionException(Throwable cause) {
            super(cause);
    }
}
