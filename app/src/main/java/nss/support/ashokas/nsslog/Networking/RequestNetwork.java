package nss.support.ashokas.nsslog.Networking;

/**
 * Created by DARK-DEVIL on 5/1/2020.
 */


import java.util.HashMap;

public class RequestNetwork<T> {
    private HashMap<String, Object> params = new HashMap<>();
    private HashMap<String, Object> headers = new HashMap<>();

    private T activity;

    private int requestType = 0;

    public RequestNetwork(T activity) {
        this.activity = activity;
    }

    public void setHeaders(HashMap<String, Object> headers) {
        this.headers = headers;
    }

    public void setParams(HashMap<String, Object> params, int requestType) {
        this.params = params;
        this.requestType = requestType;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public HashMap<String, Object> getHeaders() {
        return headers;
    }

    public T getActivity() {
        return activity;
    }

    public int getRequestType() {
        return requestType;
    }

    public void startRequestNetwork(String method, String url, String tag, RequestListener requestListener) {
        RequestNetworkController.getInstance().execute(this, method, url, tag, requestListener);
    }

    public interface RequestListener {
        public void onResponse(String tag, String response);
        public void onErrorResponse(String tag, String message);
    }
}
