package id.ac.its.digitalsignature.ui.sign;

import android.os.Bundle;
import android.view.View;

import id.ac.its.digitalsignature.R;
import id.ac.its.digitalsignature.model.Document;
import id.ac.its.digitalsignature.ui.base.BaseActivity;

public class SignActivity extends BaseActivity implements SignMvpView{

//    @Inject
    SignMvpPresenter<SignMvpView> mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getActivityComponent().inject(SignActivity.this);

//        setUnBinder(ButterKnife.bind(this));

        mPresenter.onAttach(this);

//        setUp();
    }

//    @OnClick(R.id.btn_server_login)
    void onSignDocumentClick(View v) {
        mPresenter.onDocumentSign();
        mPresenter.onSignedDocumentUpload();
    }

    @Override
    public void initDocumentPreview(Document document) {
//        pdfView = findViewById('pdf_view')
//        pdfView.fromFile(document.getFile())
//                .swipeHorizontal(false)
//                .load();
    }
}
