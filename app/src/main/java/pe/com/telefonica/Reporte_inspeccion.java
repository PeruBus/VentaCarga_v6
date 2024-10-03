package pe.com.telefonica.soyuz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;

public class Reporte_inspeccion extends Fragment {

    SharedPreferences sharedPreferences;
    Gson gson;
    Dialog InspeccionDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gson = new Gson();
        return inflater.inflate(R.layout.reporte_inspeccion, parent, false);
    }

//    @Override
//    public void onViewCreated(final View view, Bundle savedInstanceState) {
//        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//        final Dialog dialog = new Dialog(getActivity());
//
//        final RequestQueue queue = Volley.newRequestQueue(getContext());
//        final String ws_ReporCantInsp = getString(R.string.ws_ruta) + "ReporteCantInsp/"+sharedPreferences.getString("CodUsuario", "");
//        final ArrayList<String> lista_reporteCantInsp = new ArrayList<>();
//        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_ReporCantInsp, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        guardarDataMemoria("flag_insp_bol","InfinityDev",getActivity());
//                        String flagInspeccionVenta = "true";
//                        guardarDataMemoria("flagInspeccionVenta",flagInspeccionVenta,getContext());
//                        String cantidinsp = "";
//                        if (response.length() > 0) {
//                            try {
//                                JSONObject info;
//                                for (int i = 0; i < response.length(); i++) {
//                                    info = response.getJSONObject(i);
//                                    cantidinsp += info.getString("FE_PROG") + "-" + info.getString("IN_INSP") + "-" +
//                                            info.getString("FI_INSP") + "-" + info.getString("CO_VEHI") + "-" +
//                                            info.getString("CA_INSP") +"/";
//                                }
//                                cantidinsp = cantidinsp.substring(0, cantidinsp.length() - 1);
//                                lista_reporteCantInsp.add(cantidinsp);
//                                String json_reporteVenta = gson.toJson(lista_reporteCantInsp);
//                                guardarDataMemoria("insp_jsonReporteCantInsp", json_reporteVenta, getActivity());
//                                dialog.hide();
//                                dialog.dismiss();
//                                progressDialog.dismiss();
//                                getActivity().finish();
//                                startActivity(getActivity().getIntent());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                Toast.makeText(getActivity(), "Error en la ws ReporteVenta.", Toast.LENGTH_SHORT).show();
//                //errorWS(queue, error);
//                progressDialog.dismiss();
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                String auth = "Basic "
//                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
//                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", auth);
//                return headers;
//            }
//        };
//        /* -----------------------------------------*/
//        jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        queue.add(jsonArrayRequestAsientosVendidos);
//        breakTime();
//    }




    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        final ArrayList<String> lista_reporteCantInsp = getArray(sharedPreferences, gson, "insp_jsonReporteCantInsp");
        ListView listView = view.findViewById(R.id.listView_repins);
        tabla_reporte_insp adapterInspecciones = new tabla_reporte_insp(lista_reporteCantInsp, getActivity());
        listView.setAdapter(adapterInspecciones);
    }


}