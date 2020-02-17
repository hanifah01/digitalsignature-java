package id.ac.its.digitalsignature;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import id.ac.its.digitalsignature.service.CertificationService;
import id.ac.its.digitalsignature.utility.CsrHelperJava;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    static String TAG="keypair";
    static String ANDROID_KEYSTORE = "AndroidKeyStore";
    static String ANDROID_KEYALIAS= "AndroidKeyAlias";


    KeyPairGenerator kpg;
    static  int keySize = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if(!keyStore.containsAlias(ANDROID_KEYALIAS)){
                Log.d(TAG, "Creating key");

                kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);
                kpg.initialize(new KeyGenParameterSpec.Builder
                        (ANDROID_KEYALIAS,KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1).setKeySize(keySize)
                .build());

                kpg.generateKeyPair();
            }

            //blm slese
            OkHttpClient.Builder client = new OkHttpClient.Builder();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.github.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client.build())
                    .build();

            KeyPair kp = getKeyPair(keyStore, ANDROID_KEYALIAS);
            if(kp!=null){
                Log.d(TAG, "There is a key pair owo");
                PKCS10CertificationRequest csr = CsrHelperJava.getInstance().generateCSR(kp, "Edwin");
                String encryptedCsr = Base64.encodeToString(csr.getEncoded(), Base64.DEFAULT);
                String csrString = "-----BEGIN CERTIFICATE REQUEST-----\\n" + encryptedCsr +"-----END CERTIFICATE REQUEST-----\\n";

                CertificationService mCertificationService = retrofit.create(CertificationService.class );
                mCertificationService.signCSR(csrString).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.code()==200){
                            //File file = File(context.fileDir, "certificate.cert");
                            //boolean isWrittenToDisk = writeResponseBodyToDisk(response.body(), file);
                            //if(isWrittenToDisk){
                            //    Log.d("tag", "Download Succesfull");

                           // }

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

            }

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException |
                NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }


    }

    private boolean writeResponseBodyToDisk(ResponseBody body, File fileLocation){
        try {
            Log.d("tag", "Location" + fileLocation);
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(fileLocation);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("File Download: " , fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public KeyPair getKeyPair(KeyStore keyStore, String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore.Entry entry = keyStore.getEntry(alias, null);

        PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        Certificate cert = keyStore.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();

        return new KeyPair(publicKey, privateKey);

    }
}
