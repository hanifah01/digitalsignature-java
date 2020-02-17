package id.ac.its.digitalsignature.ui.sign;

import id.ac.its.digitalsignature.ui.base.MvpPresenter;
import id.ac.its.digitalsignature.model.Document;

public interface SignMvpPresenter<V extends SignMvpView> extends MvpPresenter<V> {

    void onDocumentLoad(String documentId);

    void onDocumentSign();

    void onSignedDocumentUpload();
}
