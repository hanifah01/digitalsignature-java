package id.ac.its.digitalsignature.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CertificationService {

    @FormUrlEncoded
    /*@POST("certificate/csr/sign")
    Call<ResponseBody> signCSR(
            @Field("csr") String Csr
    );*/
    @POST("phpCSR/index.php")
    Call<ResponseBody> signCSR(
            @Field("csr") String Csr
    );

}
