package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarIntegerMemoria;

public class ServicioExpressFragment extends Fragment implements RecyclerServicioVipAdapter.ItemClickListener {
    RecyclerServicioVipAdapter adapter;
    ArrayList<ServicioExpressModel> ServicioVipItin = new ArrayList<ServicioExpressModel>();
    private RecyclerView recyclerView;
    ProgressDialog progressDialog;
    int FlagValidaButton = 0;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    RequestQueue queue;
    SharedPreferences.Editor editor;

    private RecyclerView.Adapter adapterRecycler;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gson = new Gson();
        /*TDPROG_ITIN_SE(sharedPreferences.getString("Dest_fina_se","NoData"),
                sharedPreferences.getString("NU_SECU_ESAG","NoData"),
                sharedPreferences.getString("guardar_unidad","NoData"),
                sharedPreferences.getString("guardar_agencia","NoData"),
                sharedPreferences.getString("CO_RUMB_SE_GET","NoData"));*/
        Log.d("DATA_INIC", sharedPreferences.getString("TDPORG_ITIN_SE", "NoData"));
        return inflater.inflate(R.layout.servicio_especial_itin, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CargaLista();

        RecyclerView recyclerView = view.findViewById(R.id.rvVipExpress);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerServicioVipAdapter(getContext(), ServicioVipItin);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

/*        recyclerView = view.findViewById(R.id.rvVipExpress);
        adapterRecycler = new RecyclerServicioVipAdapter(getContext(), ServicioVipItin);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapterRecycler);*/
        TDPROG_ITIN_SE(sharedPreferences.getString("Dest_fina_se","NoData"),
                sharedPreferences.getString("NU_SECU_ESAG","NoData"),
                sharedPreferences.getString("guardar_unidad","NoData"),
                sharedPreferences.getString("guardar_agencia","NoData"),
                sharedPreferences.getString("CO_RUMB_SE_GET","NoData"));

    }

    @Override
    public void onItemClick(View view, final int position) {
        Log.d("prueba",adapter.getItem(position).getCO_RUMB());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Espere");
        progressDialog.setMessage("Cargando Itinerario");
        progressDialog.setCancelable(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                guardarDataMemoria("CO_RUMB_SE",adapter.getItem(position).getCO_RUMB(),getActivity());
                guardarDataMemoria("FE_PROG_SE",adapter.getItem(position).getFE_PROG(),getActivity());
                guardarDataMemoria("NU_SECU_SE",adapter.getItem(position).getNU_SECU(),getActivity());
                guardarDataMemoria("HO_SALI_SE",adapter.getItem(position).getHO_SALI(),getActivity());
                guardarDataMemoria("CO_TIPO_BUSS_SE",adapter.getItem(position).getCO_TIPO_BUSS(),getActivity());
                guardarDataMemoria("CO_VEHI_SE", adapter.getItem(position).getCO_VEHI(),getActivity());
                guardarDataMemoria("ST_TIPO_SERV", adapter.getItem(position).getST_TIPO_SERV(),getActivity());
                GetAsientos(adapter.getItem(position).getCO_RUMB(), adapter.getItem(position).getCO_TIPO_BUSS());
            }
        });
        thread.start();
        progressDialog.show();
        //Toast.makeText(getActivity(), "You clicked " + adapter.getItem(position)+ "a"+  adapter.getItem(position).getCO_EMPR() + " on row number " + position, Toast.LENGTH_SHORT).show();
        //Log.d("prueba",adapter.getItem(position).getCO_RUMB());
        /*guardarDataMemoria("CO_RUMB_SE",adapter.getItem(position).getCO_RUMB(),getActivity());
        guardarDataMemoria("FE_PROG_SE",adapter.getItem(position).getFE_PROG(),getActivity());
        guardarDataMemoria("NU_SECU_SE",adapter.getItem(position).getNU_SECU(),getActivity());
        guardarDataMemoria("HO_SALI_SE",adapter.getItem(position).getHO_SALI(),getActivity());
        guardarDataMemoria("CO_TIPO_BUSS_SE",adapter.getItem(position).getCO_TIPO_BUSS(),getActivity());
        guardarDataMemoria("CO_VEHI_SE", adapter.getItem(position).getCO_VEHI(),getActivity());
        guardarDataMemoria("ST_TIPO_SERV", adapter.getItem(position).getST_TIPO_SERV(),getActivity());
        GetAsientos(adapter.getItem(position).getCO_RUMB(), adapter.getItem(position).getCO_TIPO_BUSS());*/
//        GetVentaExpress(adapter.getItem(position).getCO_EMPR(),adapter.getItem(position).getNU_SECU(),adapter.getItem(position).getCO_RUMB(),adapter.getItem(position).getFE_PROG(),sharedPreferences.getString("Dest_fina_se","NoData"));
/*        guardarDataMemoria("guardar_agencia", agencia, getActivity());
        guardarDataMemoria("guardar_unidad", unidad, getActivity());
        guardarDataMemoria("guardar_caja", caja, getActivity());*/
    }

    void CargaLista() {

        final ArrayList<String> Itinerario_SE = getArray(sharedPreferences, gson, "TDPORG_ITIN_SE");
        for (int i = 0; i < Itinerario_SE.size(); i++) {
            String[] VipExpress_itin = Itinerario_SE.get(i).split("ƒ");
            int CANT_ASIE_DIS = Integer.parseInt(VipExpress_itin[0])-Integer.parseInt(VipExpress_itin[11]);
            ServicioExpressModel Mod = new ServicioExpressModel(VipExpress_itin[3], VipExpress_itin[0], VipExpress_itin[1], VipExpress_itin[4], VipExpress_itin[5], VipExpress_itin[6], VipExpress_itin[7], VipExpress_itin[8],
                    VipExpress_itin[12], VipExpress_itin[13], VipExpress_itin[9], VipExpress_itin[10], VipExpress_itin[11], VipExpress_itin[2],VipExpress_itin[14],String.valueOf(CANT_ASIE_DIS));
            ServicioVipItin.add(Mod);
        }
        //adapterRecycler.notifyDataSetChanged();
        adapter.notifyDataSetChanged();
    }

    void GetAsientos(final String Rumbo, final String Tipo_Bus) {
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        String ws_getDistribucionAsientos = getString(R.string.ws_ruta) + "DistribucionAsiento/Rumbo/" + Rumbo + "/TipoBus/" + Tipo_Bus;
        Log.d("DistribucionAsiento", ws_getDistribucionAsientos);
        JsonArrayRequest jsonArrayRequestDistribucionAsientos = new JsonArrayRequest(Request.Method.GET, ws_getDistribucionAsientos, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() == 0) {
                    Toast.makeText(getActivity(), "No hay distribución de asientos para el bus de este itinerario.", Toast.LENGTH_LONG).show();
                    //errorView();
                } else if (response.length() > 0) {
                    try {
                        int num_asientos = response.length();
                        String num_col = "0";
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject info = response.getJSONObject(i);
                            String num_col_temp = info.getString("NU_COLU");
                            if (Integer.parseInt(num_col_temp) > Integer.parseInt(num_col)) {
                                num_col = num_col_temp;
                            }
                        }
                        guardarIntegerMemoria("anf_numAsientos", num_asientos,getActivity());
                        guardarIntegerMemoria("anf_numCol", Integer.valueOf(num_col), getActivity());
                        GetVentaExpress("01",sharedPreferences.getString("NU_SECU_SE","NoData"),sharedPreferences.getString("CO_RUMB_SE","NoData"),
                               sharedPreferences.getString("FE_PROG_SE","NoData"),sharedPreferences.getString("Dest_fina_se","NoData"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getActivity(), "Error en la ws getDistribucionAsientos.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        /* ----------------------------------------- */
        jsonArrayRequestDistribucionAsientos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestDistribucionAsientos);
    }

    public void errorView() {

        //btn_login.setEnabled(true);
        progressDialog.dismiss();

        Intent intent = new Intent(getActivity(), ErrorActivity.class);
        //LoginActivity.this.finish();
        getActivity().finish();
        startActivity(intent);
    }

    private void errorWS(SharedPreferences.Editor editor, RequestQueue queue, VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar conectividad del dispositivo.", Toast.LENGTH_LONG).show();

        } else if (error instanceof TimeoutError) {
            Toast.makeText(getActivity(), "Se excedió el tiempo de espera.", Toast.LENGTH_LONG).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getActivity(), "Error en la autenticación.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ServerError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar credenciales e IP del servidor.", Toast.LENGTH_LONG).show();

        } else if (error instanceof NetworkError) {
            Toast.makeText(getActivity(), "No hay conectividad.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ParseError) {
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor.", Toast.LENGTH_LONG).show();

        }

        queue.getCache().clear();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
//        progressDialog.dismiss();
        errorView();
    }

    void GetVentaExpress(final String CO_EMPR, final String NU_SECU, final String CO_RUMB, final String FE_VIAJ, final String CO_DEST_FINA) {
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        //String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + CO_EMPR + "/" + NU_SECU + "/" + CO_RUMB + "/" + FE_VIAJ;
        String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "TCDOCU_CLIE_SE/" + NU_SECU + "/" + CO_RUMB + "/" + FE_VIAJ+"/"+CO_DEST_FINA+"/"+CO_EMPR;
        Log.d("asientos vendidos", ws_getAsientosVendidos);
        /* ----------------------------------------- */

        /* Request que obtiene los asientos vendidos */
        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        String asientosVend = "";
                        guardarDataMemoria("cantBoletos", String.valueOf(response.length()), getActivity());
                        ArrayList<String> lista_reporteVenta = new ArrayList<>();
                        ArrayList<String> lista_reporteVentaGPS = new ArrayList<>();
                        //Log.d("loginCant",String.valueOf(response.length()));
                        if (response.length() == 0) {
                            //progressDialog.dismiss();
                            /* Se cambia a la vista de Distribucion de asientos */
                            //guardarDataMemoria("cantBoletos",String.valueOf(response.length()),getApplicationContext());
                            /*Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
                            LoginActivity.this.finish();
                            startActivity(intent);*/
                            /* ----------------------------------------- */

                            asientosVend = asientosVend.substring(0, 0);
                            lista_reporteVentaGPS.add(asientosVend);

                            String json_reporteVentaGPS = gson.toJson(lista_reporteVentaGPS);
                            guardarDataMemoria("anf_jsonReporteVentaGPS", json_reporteVentaGPS, getActivity());

                            ArrayList<String> lista_asientosVendidosRuta = new ArrayList<>();
                            String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                            guardarDataMemoria("anf_jsonReporteVentaVIP", jsonReporteVentaRuta, getActivity());

                            String jsonReporteVenta = gson.toJson(lista_reporteVenta);
                            guardarDataMemoria("anf_jsonReporteVenta", jsonReporteVenta, getActivity());
                            /* ----------------------------------------- */
                            //GeneraToken(sharedPreferences);
                            progressDialog.dismiss();
                            VentaVipExpress VentaVipExpress_fragment = new VentaVipExpress();
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_base, VentaVipExpress_fragment).commit();

                        } else if (response.length() > 0) {
                            try {

                                JSONObject info;


                                /* Se genera una trama con los asientos vendidos y se obtiene el número del asientos, ambos se guardan en memoria */
                                for (int i = 0; i < response.length(); i++) {

                                    info = response.getJSONObject(i);

                                        lista_reporteVenta.add(info.getString("NU_ASIE"));

                                        asientosVend += info.getString("NU_ASIE") + "-" +
                                                info.getString("NU_DOCU") + "-" +
                                                info.getString("CO_DEST_ORIG") + "-" +
                                                info.getString("CO_DEST_FINA") + "-" +
                                                info.getString("IM_TOTA") + "-" +
                                                info.getString("CO_EMPR") + "-" +
                                                info.getString("TI_DOCU")+"/";
                                }
                                //guardarDataMemoria("cantBoletos",String.valueOf(response.length()),getApplicationContext());
                                //Log.d("loginCant",String.valueOf(response.length()));
                                asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                lista_reporteVentaGPS.add(asientosVend);

                                String json_reporteVentaGPS = gson.toJson(lista_reporteVentaGPS);
                                guardarDataMemoria("anf_jsonReporteVentaGPS", json_reporteVentaGPS, getActivity());

                                ArrayList<String> lista_asientosVendidosRuta = new ArrayList<>();
                                String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                guardarDataMemoria("anf_jsonReporteVentaVIP", jsonReporteVentaRuta, getActivity());

                                String jsonReporteVenta = gson.toJson(lista_reporteVenta);
                                guardarDataMemoria("anf_jsonReporteVenta", jsonReporteVenta, getActivity());
                                /* ----------------------------------------- */
                                //GeneraToken(sharedPreferences);
                                progressDialog.dismiss();
                                VentaVipExpress VentaVipExpress_fragment = new VentaVipExpress();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_base, VentaVipExpress_fragment).commit();
                                /*Intent intent = new Intent(LoginActivity.this, AppSideBarActivity.class);
                                LoginActivity.this.finish();
                                startActivity(intent);*/
                                /* ----------------------------------------- */

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getActivity(), "Error en la ws getReporteVenta.", Toast.LENGTH_LONG).show();
                errorWS(editor, queue, error);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        /* ----------------------------------------- */
        jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestAsientosVendidos);
        breakTime();
    }
    public void TDPROG_ITIN_SE(final String CO_DEST_FINA,final String NU_SECUS,final String CO_UNID,final String CO_AGEN,final String CO_RUMB)
    {
        try{
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            final RequestQueue queue = Volley.newRequestQueue(getContext());
            String ws_ItinerarioSE = getString(R.string.ws_ruta) + "TDPROG_ITIN_SE/" + NU_SECUS.replace(" ","") + "/" + CO_RUMB+"/01/"+CO_UNID+"/"+CO_AGEN+"/"+CO_DEST_FINA;
            Log.d("Itin_SE",ws_ItinerarioSE);
            JsonArrayRequest TDPROG_ITIN_SE = new JsonArrayRequest(Request.Method.GET, ws_ItinerarioSE, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d("Online",response.toString());
                            if (response.length()>0) {
                                JSONObject info;
                                final ArrayList<String> TDPROG_ITIN_SE = new ArrayList<>();
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        info = response.getJSONObject(i);
                                        final String TramaRequest =info.getString("CANT_ASIE") +"ƒ"+info.getString("HO_SALI") +"ƒ"+info.getString("CO_EMPR")
                                                +"ƒ"+info.getString("CO_VEHI")+"ƒ"+info.getString("FE_PROG").substring(0,10)+"ƒ"+info.getString("ST_TIPO_SERV")+"ƒ"+info.getString("CO_DEST_ORIG")
                                                +"ƒ"+info.getString("CO_DEST_FINA")+"ƒ"+info.getString("NU_SECU")+"ƒ"+info.getString("CO_TIPO_BUSS")+"ƒ"+info.getString("CO_RUMB")
                                                +"ƒ"+info.getString("CANT_VENT")+"ƒ"+info.getString("ANFITRION")+"ƒ"+info.getString("CONDUCTOR")+"ƒ"+info.getString("DE_TIPO_BUSS");
                                        if (!TDPROG_ITIN_SE.contains(TramaRequest)) {
                                            TDPROG_ITIN_SE.add(TramaRequest);
                                        }
                                    }
                                }catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                                final String Itin_SE = gson.toJson(TDPROG_ITIN_SE);
                                Log.d("VentaSP",Itin_SE);
                                guardarDataMemoria("TDPORG_ITIN_SE",Itin_SE,getActivity());
                                Log.d("data", sharedPreferences.getString("TDPORG_ITIN_SE", "NoData"));
                                CargaLista();

                                progressDialog.dismiss();

                            }else{
                                Toast.makeText(getActivity(), "NO AHI ITINERARIO SERVICIO ESPECIAL DISPONIBLE", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getActivity(), "Error en la ws UltimaVenta.", Toast.LENGTH_LONG).show();
//                    errorWS(editor, queue, error);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String Credencial = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                    String auth = "Basic " + Base64.encodeToString(Credencial.getBytes(), Base64.NO_WRAP);
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            TDPROG_ITIN_SE.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(TDPROG_ITIN_SE);

        }catch(Exception ex)
        {

        }
    }
}
