package net.datacrow.console.views.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.swing.SwingUtilities;

import net.datacrow.console.views.View;
import net.datacrow.core.objects.DcObject;
import net.datacrow.core.resources.DcResources;
import net.datacrow.util.DataTask;

import org.apache.log4j.Logger;

public class FillerTask extends DataTask {
    
    private static Logger logger = Logger.getLogger(FillerTask.class.getName());
    
    private DcObject[] objects;
    private View view;
    
    public FillerTask(View view, DcObject[] objects) {
        super();
        setName("Filler-Task [Object count: " + objects.length + "]");
        
        this.view = view;
        this.objects = objects;
    }
    
    @Override
    public void run() {
       view.setActionsAllowed(false);
       if (view.getType() == View._TYPE_SEARCH) {
           try {
               SwingUtilities.invokeAndWait(new ViewCleaner(view));
           } catch (Exception e) {
               logger.error("Could not clear the view [" + view + "]", e);
           }
       }

       view.deselect();
       view.updateProgressBar(0);
       view.initProgressBar(objects.length);

       final int batchSize = view.getOptimalItemAdditionBatchSize();
       final int batches = (int) Math.ceil((double) objects.length / batchSize);
       final LinkedHashMap<Integer, Collection<DcObject>> batchMap = new LinkedHashMap<Integer, Collection<DcObject>>();
       
       for (int batch = 0; batch < batches; batch++) {
           int start = batch * batchSize;
           int end = start + batchSize;
           
           Collection<DcObject> dcos = new ArrayList<DcObject>();
           for (; start < end && start < objects.length; start++)
               dcos.add(objects[start]);
           
           batchMap.put(batch, dcos);
       }

       for (final int batch : batchMap.keySet()) {
          if (!keepOnRunning()) break;
           
          final Collection<DcObject> c = batchMap.get(batch);

          try {
              SwingUtilities.invokeLater(new Filler(view, c, batch, batchSize));
          } catch (Exception e) {
              logger.error("Error while updating the view", e);
          }
          
          try {
              sleep(10);
          } catch (Exception e) {
              logger.error(e, e);
          }
       }
       
       batchMap.clear();
       stopRunning();
       try {
           SwingUtilities.invokeLater(new FinishingTask(view, objects.length));
       } catch (Exception e) {
           logger.error(e, e);
       }
        
       view = null;
       objects = null;
    }

    private static class FinishingTask implements Runnable {
        private View view;
        private int count;

        public FinishingTask(View view, int count) {
            super();
            this.count = count;
            this.view = view;
        }

        public void run() {
            if (view.getType() == View._TYPE_SEARCH)
                view.setStatus(DcResources.getText("msgSearchResult", String.valueOf(count)));
            else 
                view.setStatus(DcResources.getText("msgAddedXItemsToView", String.valueOf(count)));
            
            view.setActionsAllowed(true);
             
            view.revalidate();
            view.afterUpdate();
            view.setDefaultSelection();
            view = null;
        }
    }
    
    private static class ViewCleaner implements Runnable {
        private View view;
        
        public ViewCleaner(View view) {
            super();
            this.view = view;
        }
        
        public void run() {
            view.clear();
            view = null;
        }
    }
    
    private static class Filler implements Runnable {
        
        private View view;
        private Collection<DcObject> c;
        
        private int batch;
        private int batchSize;
        
        public Filler(View view, Collection<DcObject> c, int batch, int batchSize) {
            super();
            this.view = view;
            this.c = c;
            this.batch = batch;
            this.batchSize = batchSize;
        }
        
        public void run() {
            int pos = 1;
            for (final DcObject dco : c) {
                //if (!keepOnRunning()) break;
                view.add(dco, false);
                view.setStatus(DcResources.getText("msgAddingXToView", dco.toString()));
                view.updateProgressBar((batch * batchSize) + pos++);
            }
            
            c.clear();
            c = null;
            view = null;
        }
    }    
    
}
