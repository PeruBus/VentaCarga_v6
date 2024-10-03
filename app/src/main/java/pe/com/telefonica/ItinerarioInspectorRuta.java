package pe.com.telefonica.soyuz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;

public class ItinerarioInspectorRuta extends Fragment implements RecyclerViewInspectorItinAdapter.ItemClickListener {
    RecyclerViewInspectorItinAdapter adapter;
    ArrayList<ItinerarioModel> ItinerarioInspector = new ArrayList<>();
    private RecyclerView recyclerView;
    ProgressDialog progressDialog;
    int FlagValidaButton = 0;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    RequestQueue queue;
    SharedPreferences.Editor editor;
    Button btnBuscar;
    Dialog InspeccionDialog;
    Context ContextInspeccion;
    //private RecyclerView.Adapter adapterRecycler;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        gson = new Gson();

        return inflater.inflate(R.layout.recycler_view_inspector_itin, parent, false);

    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startBoletoService();
        RecyclerView recyclerView = view.findViewById(R.id.rvInspectorItinerario);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewInspectorItinAdapter(getContext(), ItinerarioInspector);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        btnBuscar =  view.findViewById(R.id.btnBuscar_insp);
        final EditText TXT_CO_VEHI =view.findViewById(R.id.codBus);

        /*InspeccionDialog = new Dialog(getActivity());
        InspeccionDialog.setContentView(R.layout.boleto_dialog);
        ContextInspeccion = InspeccionDialog.getContext();*/

        btnBuscar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Codigo_bus ="0"+TXT_CO_VEHI.getText();
                Log.d("Bus",Codigo_bus);
                String jsonItinerario = sharedPreferences.getString("jsonItinerario", "nada");
                Log.d("Itin",jsonItinerario);
                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                ArrayList<String> lista_itinerario = new ArrayList<>();
                Log.d("Itinerario",lista_itinerario.toString());

                if (jsonItinerario.equals("nada")) {

                } else {
                    lista_itinerario = gson.fromJson(jsonItinerario, type);
                }

                Log.d("Itinerario",lista_itinerario.toString());
                for (int i = 0; i < lista_itinerario.size(); i++) {
                    String[] itinerario = lista_itinerario.get(i).split("/");
                     Log.d("vehiitin",itinerario[4]);
                    if (Codigo_bus.toString().equals(itinerario[4])) {

                        String json_destinos = sharedPreferences.getString("json_destinos", "nada");
                        type = new TypeToken<ArrayList<String>>() {}.getType();
                        ArrayList<String> lista_destinos = gson.fromJson(json_destinos, type);
                        String CO_DEST_ORIG="";
                        String CO_DEST_FINA="";
                        for (int j = 0; j < lista_destinos.size(); j++) {
                            String[] data = lista_destinos.get(j).split("-");
                            if (data[0].equals(itinerario[6])) {
                                CO_DEST_ORIG=  data[0]+"-"+data[1];
                                break;
                            }
                        }
                        for (int m = 0; m < lista_destinos.size(); m++) {
                            String[] data = lista_destinos.get(m).split("-");
                            if (data[0].equals(itinerario[7])) {
                                CO_DEST_FINA=  data[0]+"-"+data[1];
                                break;
                            }
                        }
                        Log.d("origen",CO_DEST_ORIG);
                        ItinerarioInspector.clear();
                        ItinerarioModel Itinerario = new ItinerarioModel(itinerario[10], itinerario[5], itinerario[1],itinerario[3],
                                itinerario[9],itinerario[12],itinerario[8],itinerario[0],itinerario[2],itinerario[4],CO_DEST_ORIG,CO_DEST_FINA);
                        ItinerarioInspector.add(Itinerario);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, final int position) {
        ArrayList<String> CTRL_ZONA = getArray(sharedPreferences,gson,"jsonZona");
        List<ModeloZonaInspeccion> ModelZona = new ArrayList<>();
        for (int i =0;i<CTRL_ZONA.size();i++){
            String[] ZONA = CTRL_ZONA.get(i).split("/");
            ModeloZonaInspeccion Model_zona = new ModeloZonaInspeccion(ZONA[0],ZONA[1],ZONA[2],ZONA[3],ZONA[4]);
            ModelZona.add(Model_zona);
        }

        //DECLARA EL ARRAY
        final List<Spinner_model> LIST_ZONA_INSP = new ArrayList<>();

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.inspeccion_dialog);
        dialog.setTitle("Seleccionar");

        //DECLARA EL SPINNER
        final Spinner spinner_control = dialog.findViewById(R.id.spinner_tcrl_inicio);

        if(adapter.getItem(position).getCO_RUMB().equals("SUR"))
        {
            Collections.sort(ModelZona, new Comparator<ModeloZonaInspeccion>() {
                @Override
                public int compare(ModeloZonaInspeccion modeloZonaInspeccion, ModeloZonaInspeccion t1) {
                    return new Integer(modeloZonaInspeccion.getNU_ORDE_SUR()).compareTo(new Integer(t1.getNU_ORDE_SUR()));
                }
            });
        }else if(adapter.getItem(position).getCO_RUMB().equals("NOR"))
        {
            Collections.sort(ModelZona, new Comparator<ModeloZonaInspeccion>() {
                @Override
                public int compare(ModeloZonaInspeccion modeloZonaInspeccion, ModeloZonaInspeccion t1) {
                    return new Integer(modeloZonaInspeccion.getNU_ORDE_NOR()).compareTo(new Integer(t1.getNU_ORDE_NOR()));
                }
            });
        }
        LIST_ZONA_INSP.add(new Spinner_model("InfinityDev","Selecciona",""));
        for (int i = 0;i<ModelZona.size();i++){
            LIST_ZONA_INSP.add(new Spinner_model(ModelZona.get(i).getCO_TRAM(),ModelZona.get(i).getDE_TRAM(),ModelZona.get(i).getCO_ZONA()));
        }
        Button btn_Cancelar = dialog.findViewById(R.id.btnCancelar);
        Button btn_Iniciar = dialog.findViewById(R.id.btnInicioInsp);
        btn_Cancelar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
                dialog.dismiss();
            }
        });
        btn_Iniciar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Spinner_model so = (Spinner_model)spinner_control.getSelectedItem();
                if(so.id.equals("InfinityDev")){
                    Toast.makeText(getActivity(),"SELECCIONAR TRAMO CONTROL",Toast.LENGTH_SHORT).show();
                }else{
                    guardarDataMemoria("CO_EMPR_INSP",adapter.getItem(position).getCO_EMPR(),getActivity());
                    guardarDataMemoria("FE_PROG_INSP",adapter.getItem(position).getFE_PROG(),getActivity());
                    guardarDataMemoria("NU_SECU_INSP",adapter.getItem(position).getNU_SECU(),getActivity());
                    guardarDataMemoria("CO_RUMB_INSP",adapter.getItem(position).getCO_RUMB(),getActivity());
                    guardarDataMemoria("CO_VEHI_INSP",adapter.getItem(position).getCO_VEHI(),getActivity());
                    guardarDataMemoria("HO_SALI_INSP",adapter.getItem(position).getHO_SALI(),getActivity());
                    guardarDataMemoria("CO_DEST_ORIG",adapter.getItem(position).getCO_DEST_ORIG(),getActivity());
                    guardarDataMemoria("CO_DEST_FINA",adapter.getItem(position).getCO_DEST_FINA(),getActivity());
                    guardarDataMemoria("CO_TRAM_CTRL",so.id.toString().trim(),getActivity());

                    ArrayList<String> Lista_Tramo_CTRL = getArray(sharedPreferences,gson,"CTRL_INSP_POS");
                    ArrayList<String> ControlDestinos=new ArrayList<String>();
                    for (int i = 0 ;i<Lista_Tramo_CTRL.size();i++)
                    {
                        String[] Lista_CTLR=Lista_Tramo_CTRL.get(i).split("ƒ");
                        if(so.id.trim().equals(Lista_CTLR[1].trim()) && adapter.getItem(position).getCO_RUMB().equals(Lista_CTLR[2]))
                        {
                            ControlDestinos.add(Lista_CTLR[0]+"ƒ"+Lista_CTLR[3]);
                        }
                    }
                    Log.d("ListaDest",ControlDestinos.toString());
                    guardarDataMemoria("CTRL_DEST",gson.toJson(ControlDestinos),getActivity());
                    //GET_SERIE(adapter.getItem(position).getCO_VEHI());
                    ObtieneBoletos(adapter.getItem(position).getCO_EMPR(),adapter.getItem(position).getCO_RUMB(),adapter.getItem(position).getFE_PROG(),
                            adapter.getItem(position).getNU_SECU(),adapter.getItem(position).getHO_SALI(),adapter.getItem(position).getCO_VEHI(),dialog);
                }
            }
        });
        ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,LIST_ZONA_INSP);
        spinner_control.setAdapter(spinnerArray);
        dialog.show();
        dialog.setCancelable(false);
    }

//    void GET_SERIE(String CO_VEHI)
//    {
//        String ws_seriebus = getString(R.string.ws_ruta) + "SERIEBUS/" + CO_VEHI;
//        Log.d("getSerieBus",ws_seriebus);
//        JsonArrayRequest jsonArrayRequestSerie = new JsonArrayRequest(Request.Method.GET, ws_seriebus, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        if (response.length() == 0) {
//                            Toast.makeText(getActivity(), "El BUS no tiene series", Toast.LENGTH_LONG).show();
//                        }
//                        else if (response.length() > 1){
//                            JSONObject info;
//                            ArrayList<String> lista_series = new ArrayList<>();
//                            try{
//                                for (int i = 0; i < response.length(); i++) {
//                                    info = response.getJSONObject(i);
//                                    lista_series.add(info.getString("NU_SERI"));
//                                }
//                                String JSON_SERIE = gson.toJson(lista_series);
//                                guardarDataMemoria("jsonSerie", JSON_SERIE, getActivity());
//                            } catch (JSONException e){
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                Toast.makeText(getActivity(), "Error en la ws SEW.", Toast.LENGTH_LONG).show();
//                progressDialog.dismiss();
//            }
//        }){
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
//        jsonArrayRequestSerie.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        queue.add(jsonArrayRequestSerie);
//    }

    void ObtieneBoletos(String CO_EMPR,String CO_RUMB,String FE_PROG,String NU_SECU,String HO_SALI,String CO_VEHI,final Dialog dialog)
    {
        try{
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Espere...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            final RequestQueue queue = Volley.newRequestQueue(getContext());
            final Gson gson = new Gson();
            String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + CO_EMPR + "/" + NU_SECU  + "/" + CO_RUMB + "/" + FE_PROG;
            Log.d("respuesta",ws_getAsientosVendidos);
            final ArrayList<String> lista_reporteVenta = new ArrayList<>();
            JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            guardarDataMemoria("flag_insp_bol","InfinityDev",getActivity());
                            String flagInspeccionVenta = "true";
                            guardarDataMemoria("flagInspeccionVenta",flagInspeccionVenta,getContext());
                            String asientosVend = "";
                            if (response.length() == 0) {
                                asientosVend = "NoData";
                                lista_reporteVenta.add(asientosVend);
                                String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());
                                dialog.hide();
                                dialog.dismiss();
                                Date date = new Date();
                                final String fechaInspeccion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                //final String horaInspeccion = new SimpleDateFormat("hh:mm:ss").format(date);
                                guardarDataMemoria("fechaInspeccion",fechaInspeccion,getActivity());
                                //guardarDataMemoria("horaInspeccion",horaInspeccion,getActivity());
                                progressDialog.dismiss();
                                /*InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();*/
                                getActivity().finish();
                                startActivity(getActivity().getIntent());
                            } else if (response.length() > 0) {
                                try {
                                    JSONObject info;
                                    for (int i = 0; i < response.length(); i++) {
                                        info = response.getJSONObject(i);
                                        if(info.getString("ServicioEmpresa").equals("VIAJE")){
                                            asientosVend += info.getString("NU_ASIE") + "-" + info.getString("NU_DOCU") + "-" +
                                                    info.getString("CO_DEST_ORIG") + "-" + info.getString("CO_DEST_FINA") + "-" +
                                                    info.getString("CO_CLIE") + "-" + info.getString("IM_TOTA") + "-" +
                                                    info.getString("CO_EMPR") + "-" + info.getString("TI_DOCU") + "-" +
                                                    info.getString("LIBERADO") + "-" + info.getString("CARGA") + "-" +
                                                    info.getString("ServicioEmpresa") +"/";
                                        }else if(info.getString("ServicioEmpresa").equals("CARGA")){
                                            asientosVend += info.getString("NU_ASIE") + "-" + info.getString("NU_DOCU") + "-" +
                                                    info.getString("CO_DEST_ORIG") + "-" + info.getString("CO_DEST_FINA") + "-" +
                                                    info.getString("CO_CLIE") + "-" + info.getString("IM_TOTA") + "-" +
                                                    info.getString("CO_EMPR") + "-" + info.getString("TI_DOCU") + "-" +
                                                    info.getString("LIBERADO") + "-" + info.getString("CARGA") + "-" +
                                                    info.getString("ServicioEmpresa") + "-" + info.getString("TI_PROD") + "-" +
                                                    info.getString("CA_DOCU") +"/";
                                        }
                                    }
                                    asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                    lista_reporteVenta.add(asientosVend);
                                    String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                    guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());
                                    dialog.hide();
                                    dialog.dismiss();
                                    Date date = new Date();
                                    //final String fechaInspeccion = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                    final String fechaInspeccion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                    final String horaInspeccion = new SimpleDateFormat("hh:mm:ss").format(date);
                                    guardarDataMemoria("fechaInspeccion",fechaInspeccion,getActivity());
                                    guardarDataMemoria("horaInspeccion",horaInspeccion,getActivity());
                                    progressDialog.dismiss();
                                    /*InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();*/
                                    getActivity().finish();
                                    startActivity(getActivity().getIntent());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getActivity(), "Error en la ws ReporteVenta.", Toast.LENGTH_SHORT).show();
                    //errorWS(queue, error);
                    progressDialog.dismiss();
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
            /* -----------------------------------------*/
            jsonArrayRequestAsientosVendidos.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonArrayRequestAsientosVendidos);
            breakTime();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
}
