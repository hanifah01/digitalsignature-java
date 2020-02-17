package id.ac.its.digitalsignature.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiInterface {

    @GET("Document/test.pdf")
    Call<ResponseBody> downloadFileWithFixedUrl();

    @GET
    Call<ResponseBody> downloadFileUrl(@Url String url);
}
