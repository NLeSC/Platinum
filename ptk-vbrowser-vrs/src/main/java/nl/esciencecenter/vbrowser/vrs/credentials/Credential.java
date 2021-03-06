package nl.esciencecenter.vbrowser.vrs.credentials;

public interface Credential {

    public String getCredentialType();

    public String getUserPrincipal();

    public String getGroupPrincipal();

    public boolean isValid();

}
