package net.datacrow.core.services.plugin;

public class InvalidServiceException extends Exception {

    private static final long serialVersionUID = 7502179535641217954L;

    public InvalidServiceException(String msg) {
        super(msg);
    }
}
