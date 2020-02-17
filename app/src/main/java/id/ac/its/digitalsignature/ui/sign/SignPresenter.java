package id.ac.its.digitalsignature.ui.sign;

import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.PdfSigner;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import id.ac.its.digitalsignature.mock.AppLogger;
import id.ac.its.digitalsignature.mock.CompositeDisposable;
import id.ac.its.digitalsignature.mock.DataManager;
import id.ac.its.digitalsignature.mock.SchedulerProvider;
import id.ac.its.digitalsignature.model.Document;
import id.ac.its.digitalsignature.ui.base.BasePresenter;
import id.ac.its.digitalsignature.utility.SignHelperJava;

public class SignPresenter<V extends SignMvpView>  extends BasePresenter<V> implements SignMvpPresenter<V> {

    private Document mDocument;
    private String mSignedDocumentLocation;

    public SignPresenter(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable) {
        super(dataManager, schedulerProvider, compositeDisposable);
    }

    @Override
    public void onDocumentLoad(final String documentId) {
        getMvpView().showLoading();

        getCompositeDisposable().add(
                getDataManager().doGetDocument(documentId)
                        .subscribe(new Consumer<Document>() {
                            @Override
                            public void accept(Document response) throws Exception {
                                AppLogger.d("Loading Document with id %s", documentId);
                                mDocument = response;
                                getMvpView().initDocumentPreview(response);
                                getMvpView().hideLoading();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                getMvpView().hideLoading();
                            }
                        })
        );
    }

    @Override
    public void onDocumentSign() {
        getMvpView().showLoading();

        if (mDocument == null){
            getMvpView().showMessage("Cannot Sign Document of Undefined");
            getMvpView().hideLoading();
            return;
        }

        mSignedDocumentLocation = mDocument.getFile().toString() + "-signed";
        PrivateKey pk = null;
        Certificate[] cert = null;
        try {
            SignHelperJava.getInstance().sign(mDocument.getFile().toString(), documentDestination, cert, pk,DigestAlgorithms.SHA256,"AndroidKeyStoreBCWorkaround", PdfSigner.CryptoStandard.CMS, "Sign Document", "Surabaya" );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        getMvpView().showMessage("Sign Success");
        getMvpView().hideLoading();
    }

    @Override
    public void onSignedDocumentUpload() {
        getMvpView().showLoading();

        File signedDocument = new File(mSignedDocumentLocation);
        getCompositeDisposable().add(
                getDataManager().doUploadSignedDocument(signedDocument)
                        .subscribe(new Consumer<Document>() {
                            @Override
                            public void accept(Object response) throws Exception {

                                getMvpView().showMessage("Upload Success");
                                getMvpView().hideLoading();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                getMvpView().hideLoading();
                            }
                        })
        );
        getMvpView().hideLoading();
    }

}
