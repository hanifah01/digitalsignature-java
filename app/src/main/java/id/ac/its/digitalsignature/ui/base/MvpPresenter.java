package id.ac.its.digitalsignature.ui.base;

public interface MvpPresenter<V extends MvpView> {
    void onAttach(V mvpView);
}
