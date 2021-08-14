# sbol2easy

A library for operations on sbol designs.

The main operations are:
* annotating: adding descriptive metadata to component definitions using attributes from Excel table  
  see `ed.biordm.sbol.sbol2easy.transform.ComponentAnnotator`
* flattening design: converting components tree into one annotated sequence  
  see `ed.biordm.sbol.sbol2easy.transform.ComponentFlattener`
* genbank conversion: converts to sbol to genbank, but supports more features types and handles multiline descrptions  
  see `ed.biordm.sbol.sbol2easy.transform.GenBankConverter`
* generation from table: using sbol template to generate a library of similar designs using details from Excel table  
  see `ed.biordm.sbol.sbol2easy.transform.LibraryGenerator`
* taming: removing SynBioHub specific namespaces from a document so it can be re-uploaded to the server  
  see `ed.biordm.sbol.sbol2easy.transform.SynBioTamer`





