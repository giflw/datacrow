package net.datacrow.core.migration.itemimport;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.datacrow.core.DcRepository;
import net.datacrow.core.DcThread;
import net.datacrow.core.modules.DcModule;
import net.datacrow.core.modules.DcModules;
import net.datacrow.core.objects.DcField;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.Converter;
import net.datacrow.util.Utilities;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlImporter extends ItemImporter {
    
    private static Logger logger = Logger.getLogger(XmlImporter.class.getName());
    
    public XmlImporter(int moduleIdx, int mode) throws Exception {
        super(moduleIdx, "XML", mode);
    }
    
    @Override
    public Collection<String> getSettingKeys() {
        Collection<String> settingKeys = super.getSettingKeys();
        settingKeys.add(DcRepository.Settings.stImportMatchAndMerge);
        return settingKeys;
    }
    
    @Override
    protected void initialize() {}

    @Override
    public DcThread getTask() {
        return new Task(file, getModule(), client);
    }

    @Override
    public String[] getSupportedFileTypes() {
        return new String[] {"xml"};
    }
    
    @Override
    public void cancel() {}

    @Override
    public String getName() {
        return DcResources.getText("lblXImport", "XML");
    }
    
    private class Task extends DcThread {
        
        private File file;
        private IItemImporterClient listener;
        private DcModule module;
        
        public Task(File file, DcModule module, IItemImporterClient listener) {
            super(null, "XML import for " + file);
            this.file = file;
            this.module = module;
            this.listener = listener;
        }
    
        private DcObject parseItem(DcModule module, Element eItem) throws Exception {
            DcObject dco = module.getItem();
            dco.setIDs();
            String value;
            // get the object
            for (DcField field : module.getFields()) {
                
                if ((   field.isUiOnly() && 
                        field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION && 
                        field.getValueType() != DcRepository.ValueTypes._PICTURE) ||  
                        field.getIndex() == DcObject._SYS_EXTERNAL_REFERENCES) 
                    continue;
                
                String fieldName = Converter.getValidXmlTag(field.getSystemName());
                NodeList nlField = eItem.getElementsByTagName(fieldName);
                
                if (nlField == null || nlField.getLength() == 0) continue;
                
                Element eField = (Element) nlField.item(0);
                
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    // retrieve the items by their module name
                    DcModule referenceMod = DcModules.get(field.getReferenceIdx());
                    String referenceName = Converter.getValidXmlTag(referenceMod.getSystemObjectName());
                    NodeList elReferences = eField.getElementsByTagName(referenceName);
                    for (int j = 0; elReferences != null && j < elReferences.getLength(); j++) {
                        // retrieve the values by the display field index (the system display field index)
                        Element eReference = (Element) elReferences.item(j);
                        DcObject reference = referenceMod.getItem();
                        String referenceField = Converter.getValidXmlTag(reference.getField(reference.getSystemDisplayFieldIdx()).getSystemName());
                        NodeList nlRefField = eReference.getElementsByTagName(referenceField);
                        if (nlRefField != null && nlRefField.getLength() > 0) {
                            Node eRefField = nlRefField.item(0);
                            setValue(dco, field.getIndex(), eRefField.getTextContent(), listener);
                        } else {
                            logger.debug("Could not set value for field " + referenceField + ". The field name does not exist in the XML file");
                        }
                    }
                } else if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                    setValue(dco, field.getIndex(), eField.getTextContent(), listener);
                    
                } else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                    setValue(dco, field.getIndex(), eField.getTextContent(), listener);
                } else {
                    value = eField.getTextContent();
                    if (!Utilities.isEmpty(value))
                        setValue(dco, field.getIndex(), value, listener);
                }
            }
            
            return dco;
        }
        
        @SuppressWarnings("resource")
        @Override
        public void run() {
            FileInputStream fis = null;
            try {
            	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                fis = new FileInputStream(file);
                Document document = db.parse(fis);
                
                Element eTop = document.getDocumentElement();
                
                String name = Converter.getValidXmlTag(module.getSystemObjectName());
                NodeList nlItems = eTop.getElementsByTagName(name);
    
                listener.notifyStarted(nlItems != null ? nlItems.getLength() : 0);
                
                Element eItem;
                DcObject dco;
                DcModule cm;
                String childName;
                NodeList nlChildren;
                Element eChild;
                for (int i = 0; !isCanceled() && nlItems != null && i < nlItems.getLength(); i++) {
                    try {
                    	eItem = (Element) nlItems.item(i);
                    	
                    	if (eItem.getParentNode() != eTop) continue;
                    	
                    	dco = parseItem(module, eItem);
                    	cm = module.getChild();
                    	// Child items for module ITEM will be skipped since these are abstract items.
                    	if (cm != null && cm.getIndex() != DcModules._ITEM) {
                    	    childName = Converter.getValidXmlTag(cm.getSystemObjectName());
                            nlChildren = eItem.getElementsByTagName(childName);
                            
                            for (int j = 0; nlChildren != null && j < nlChildren.getLength(); j++) {
                                eChild = (Element) nlChildren.item(j);
                                dco.addChild(parseItem(cm, eChild));
                            }
                    	}
                    	listener.notifyProcessed(dco);
                    } catch (Exception e) {
                        listener.notifyMessage(e.getMessage());
                        logger.error(e, e) ;
                    }
                }
                
                listener.notifyStopped();
                
            } catch (Exception e) {
                logger.error(e, e) ;
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (Exception e) {
                    logger.debug("Failed to close resource", e);
                }
            }
        }
    }
}
