package id.ac.its.digitalsignature.ui.sign;

import id.ac.its.digitalsignature.model.Document;
import id.ac.its.digitalsignature.ui.base.MvpView;

public interface SignMvpView extends MvpView {

    void initDocumentPreview(Document document);

}
