package id.ac.its.digitalsignature.model;

import com.google.gson.annotations.SerializedName;

public class UploadDocumentResponse {
    @SerializedName("documentName")
    String name;

    @SerializedName("createdAt")
    String createdAt;
}
