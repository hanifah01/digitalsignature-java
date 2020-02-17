package id.ac.its.digitalsignature.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DocumentNameResponse {

    @SerializedName("documents_name")
    List<String> names;
}
