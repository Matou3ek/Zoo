package cz.matousek.zoo;

/**
 * Created by matousekl on 9/21/2017.
 */

public enum Operation {
    GET("animals"),
    POST("add"),
    PUT("update/"),
    DELETE("remove/");

    private String url;

    private Operation(String url){
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
