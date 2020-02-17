package id.ac.its.digitalsignature.mock;

import java.io.File;

import id.ac.its.digitalsignature.model.Document;

public interface DataManager {

    Observable<Document> doGetDocument(String documentId);

    void doUploadSignedDocument(File document);
}
