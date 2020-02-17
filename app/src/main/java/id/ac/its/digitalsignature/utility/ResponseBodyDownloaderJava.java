package id.ac.its.digitalsignature.utility;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

public class ResponseBodyDownloaderJava {

    private static ResponseBodyDownloaderJava instance = null;

    public static ResponseBodyDownloaderJava getInstance() {
        if (instance == null){
            instance = new ResponseBodyDownloaderJava();
        }
        return instance;
    }

    public Boolean download(ResponseBody body, File fileLocation){
        try {
            //File fileLocation = new File(getExternalFilesDir(null) + File.separator + "test.pdf");
            Log.d("tag", "Location ".concat(fileLocation.toString()));
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
                    fileSizeDownloaded += (long)read;
                    Log.d("tag", "file download: " + fileSizeDownloaded + "of" + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }

    }
}
