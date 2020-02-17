package id.ac.its.digitalsignature.utility;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class SignHelperJava {

    private static SignHelperJava instance = null;

    public static SignHelperJava getInstance() {
        if (instance == null){
            instance = new SignHelperJava();
        }
        return instance;
    }

    public void sign(
            String src , String dest ,
            Certificate[] chain,
            PrivateKey pk, String digestAlgorithm, String provider,
            PdfSigner.CryptoStandard subfilter,
            String reason, String location
    ) throws IOException, GeneralSecurityException {
        // Creating the reader and the signer
        try {
            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), false);
            // Creating the appearance
            PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                    .setReason(reason)
                    .setLocation(location)
                    .setReuseAppearance(false);
            Rectangle rect = new Rectangle(PageSize.A4.getBottom() - 120F, 648F, 200F, 100F);
            appearance.setPageRect(rect).setPageNumber(1);
            signer.setFieldName("sig");
            // Creating the signature
            PrivateKeySignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
            BouncyCastleDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, chain, null, null, null, 4*8192, subfilter);
        }catch (IOException e) {
            throw new IOException(e.getMessage());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new GeneralSecurityException(e.getMessage());
        }
    }
}
