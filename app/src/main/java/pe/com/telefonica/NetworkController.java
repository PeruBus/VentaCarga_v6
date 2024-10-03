package pe.com.telefonica.soyuz;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class NetworkController extends Application {

    private static final String TAG = NetworkController.class.getSimpleName();
    private RequestQueue requestQueue;
    private static NetworkController networkController;


    @Override
    public void onCreate() {
        super.onCreate();

        networkController = this;
    }

    public static synchronized NetworkController getInstance(){
        return networkController;
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag){
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue();
    }

    public <T> void addToRequestQueue(Request<T> req){
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

}
