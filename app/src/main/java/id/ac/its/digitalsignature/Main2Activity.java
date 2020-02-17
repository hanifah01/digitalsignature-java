package id.ac.its.digitalsignature;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.PdfSigner;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import id.ac.its.digitalsignature.model.UploadDocumentResponse;
import id.ac.its.digitalsignature.service.CertificationService;
import id.ac.its.digitalsignature.service.DocumentService;
import id.ac.its.digitalsignature.utility.CsrHelperJava;
import id.ac.its.digitalsignature.utility.ResponseBodyDownloaderJava;
import id.ac.its.digitalsignature.utility.SignHelperJava;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static id.ac.its.digitalsignature.MainActivity.ANDROID_KEYALIAS;
import static id.ac.its.digitalsignature.MainActivity.ANDROID_KEYSTORE;

public class Main2Activity extends AppCompatActivity {
    private KeyPairGenerator kpg;
    private KeyPair kp;
    private Integer keySize = 2048;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("tag", "Creating key");
        try {
            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            kpg.initialize(
                    new KeyGenParameterSpec.Builder(
                            ANDROID_KEYALIAS,
                            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .setKeySize(keySize)
                            .build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        kp = kpg.generateKeyPair();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(
                        (BuildConfig.DEBUG)?HttpLoggingInterceptor.Level.BODY:HttpLoggingInterceptor.Level.NONE
                ))
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://10.107.254.108/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

//        KeyP kp = getKeyPair(keyStore, ANDROID_KEYALIAS);
        if (kp != null){
            Log.d("key pair", "There is a key pair owo");
            String csrString = null;
            try {
                PKCS10CertificationRequest csr = CsrHelperJava.getInstance().generateCSR(kp, "Edwin");
                String encryptedCsr = Base64.encodeToString(csr.getEncoded(), Base64.DEFAULT);
                csrString =
                        "-----BEGIN CERTIFICATE REQUEST-----\n" +  encryptedCsr + "-----END CERTIFICATE REQUEST-----\n";
            } catch (IOException e) {
                e.printStackTrace();
            }

            CertificationService mCertificationService = retrofit.create(CertificationService.class);
            mCertificationService.signCSR(csrString).enqueue(
                    new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.code() == 200){
//                        pubkey_text.text = response.body()?.contentType().toString()
                                File file = new File(getApplicationContext().getExternalFilesDir(null), "certificate.cert");
                                boolean isWrittenToDisk = ResponseBodyDownloaderJava.getInstance().download(response.body(), file);
                                if (isWrittenToDisk) {
                                    Log.d("tag", "Download Succesfull");
                                    File certificateFile = new File(getApplicationContext().getExternalFilesDir(null),"certificate.cert");

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("tag", "Error ${error.message}");
                        }
                    });
        }

        DocumentService documentService = retrofit.create(DocumentService.class);

        //documentService.getDocument("document/blank.pdf").enqueue(new Callback<ResponseBody>() {
        documentService.getDocument("Document/test.pdf").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200){
                    File pdfLoc =  new File(getApplicationContext().getExternalFilesDir(null), "test.pdf");
                    boolean isWrittenToDisk = ResponseBodyDownloaderJava.getInstance().download(response.body(), pdfLoc);
                    if (isWrittenToDisk) {
                        try {
                            String certPathString = new File(getApplicationContext().getExternalFilesDir(null),"certificate.cert").toString();
                            String certString = usingBufferedReader(certPathString);
                            String x509CertString = certString
                                    .replace("-----BEGIN CERTIFICATE-----","")
                                    .replace("-----END CERTIFICATE-----","");
                            byte[] x509Cert = Base64.decode(x509CertString, Base64.DEFAULT);
                            InputStream isX509Cert = new ByteArrayInputStream(x509Cert);
                            Log.d("x509Cert", x509CertString);

                            Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(isX509Cert);
                            Certificate[] chain2 = { cert };
                            isX509Cert.close();

                            String src = new File(getApplicationContext().getExternalFilesDir(null),"test.pdf").toString();
                            String dst = new File(getApplicationContext().getExternalFilesDir(null),"new-blank-2.pdf").toString();
                            SignHelperJava.getInstance().sign(src, dst, chain2, kp.getPrivate(), DigestAlgorithms.SHA256, "AndroidKeyStoreBCWorkaround", PdfSigner.CryptoStandard.CMS, "Only Testing", "Surabaya");
                        } catch (KeyStoreException e) {
                            e.printStackTrace();
                        } catch (CertificateException e) {
                            e.printStackTrace();
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("tag", "Error ${error.message}");
            }
        });
        //File file = new File(getApplicationContext().getExternalFilesDir(null), "new-blank-2.pdf");
        File file = new File(getExternalFilesDir(null) + File.separator + "new-blank-2.pdf");
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("document", file.getName(), requestBody);

        documentService.saveDocument(fileToUpload).enqueue(new Callback<UploadDocumentResponse>() {
            @Override
            public void onResponse(Call<UploadDocumentResponse> call, Response<UploadDocumentResponse> response) {
                if(response.code() == 200){
                    Log.d("stat","Sent Succesfully");
                } else{
                    Log.e("ohno", "something bad happpen");
                    Log.e("m", response.message());
                }
            }
            @Override
            public void onFailure(Call<UploadDocumentResponse> call, Throwable t) {
                Log.e("tag", "Error ${error.message}");
            }
        });

    }

    private static String usingBufferedReader(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private KeyPair getKeyPair(
            KeyStore keystore,
            String alias,
            KeyStore keyStore, String androidKeyalias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
//        val key = keystore.getKey(alias, password.toCharArray()) as PrivateKey
        KeyStore.Entry entry= keystore.getEntry(alias, null);
        if (entry instanceof KeyStore.PrivateKeyEntry == false) {
            Log.w("tag", "Not an instance of a PrivateKeyEntry");
            return null;
        }
        KeyStore.PrivateKeyEntry privateKey = (KeyStore.PrivateKeyEntry) entry;
        Certificate cert = keystore.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();

        return new KeyPair(publicKey, privateKey.getPrivateKey());
    }
}
