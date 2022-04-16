package org.opensearch.gradle.zipplugin;

public class ZipPublishExtension {

    private String zipGroup = "org.opensearch.plugin";
    public String zipVersion = "1.0.0";
    public String zipArtifact = "pgplugin";
    public String zipFilePath = "test.zip";

    public void setZipversion(String zipVersion){
		this.zipVersion=zipVersion;
	}
    public void setZipartifact(String zipArtifact){
		this.zipArtifact=zipArtifact;
	}
    public void setZipfilePath(String zipFilePath){
        this.zipFilePath=zipFilePath;
    }

    public String getZipgroup(){
		return zipGroup;
	}
    public String getZipversion() {
        return zipVersion;
    }
    public String getZipartifact() {
        return zipArtifact;
    }
    public String getZipfilePath() {
        return zipFilePath;
    }

}
