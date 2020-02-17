package id.ac.its.digitalsignature.service;

import id.ac.its.digitalsignature.model.DocumentNameResponse;
import id.ac.its.digitalsignature.model.UploadDocumentResponse;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface DocumentService {

    @GET("/Document/test.pdf")
    Call<DocumentNameResponse> listDocument();

    @GET
    Call<ResponseBody> getDocument(@Url String filename);

    @POST("/Document")
    @Multipart
    Call<UploadDocumentResponse> saveDocument(@Part MultipartBody.Part document);




}
