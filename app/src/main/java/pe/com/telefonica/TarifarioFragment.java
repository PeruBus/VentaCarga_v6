package pe.com.telefonica.soyuz;

import android.content.Context;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;



public class TarifarioFragment extends Fragment {
    private IDAL dal;
    private IPrinter printer;
    ArrayList<String> lista_tarifasViaje = new ArrayList<>();

    Gson gson;
    RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tarifario, parent, false);
    }

	/**
	* Implementación de la lógica para poder visualizar el tarifario de viaje o carga según la opción que se seleccione.
	* @param view
	* @param savedInstanceState
	*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Spinner spinner_tarifario = view.findViewById(R.id.spinner_tarifario);
        Context context_tarifario = view.getContext();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        TextView txtTari = view.findViewById(R.id.txt_tari);
        txtTari.setText("TARIFARIO - "+ sharedPreferences.getString("NU_TARI","NoData"));
        Button btn_ImprimeTarifa = view.findViewById(R.id.btn_impr_Tari);
        Button btn_Actualiza = view.findViewById(R.id.btn_act_tari);
        /* Se genera un arreglo con los dos tipos de tarifa que se van a mostrar */
        ArrayList<String> tipoTarifa = new ArrayList<>();
        tipoTarifa.add("Tarifa de Viaje");
        tipoTarifa.add("Tarifa de Carga");
        /* ----------------------------------------- */
        gson = new Gson();

        /* Se añade la lista de tipo de tarifa al spinner */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context_tarifario, android.R.layout.simple_spinner_item, tipoTarifa);
        spinner_tarifario.setAdapter(adapter);
        /* ----------------------------------------- */
        try {
            dal = NeptuneLiteUser.getInstance().getDal(getContext());
            printer = dal.getPrinter();
            printer.init();
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
        } catch (Exception e) {
            //Log.e("IMPRESORA", "No se puede inicializar la impresora");
        }

        /* TODO: ITEM SELECTED LISTENER PARA EL SPINNER DE TIPO DE TARIFA */
        spinner_tarifario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                /* Depende de la selección en el spinner, se muestra el tarifario de viaje o carga */
                if (position == 0){

                    /* Se muestra la vista de tarifario de viaje */
                    TarifarioViajeFragment tarifarioViajeFragment = new TarifarioViajeFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment, tarifarioViajeFragment);
                    ft.commit();
                    /* ----------------------------------------- */

                }else if (position == 1){

                    /* Se muestra el tarifario de carga */
                    TarifarioCargaFragment tarifarioCargaFragment = new TarifarioCargaFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.fragment, tarifarioCargaFragment);
                    ft.commit();
                    /* ----------------------------------------- */
                }
                /* ----------------------------------------- */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        btn_Actualiza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetTarifaRuta();
            }
        });
        btn_ImprimeTarifa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> Tari = getArray(sharedPreferences,gson,"anf_jsonTarifasViaje");
                ImprimeTarifa(printer,Tari,sharedPreferences.getString("anf_codigoEmpresa","NoData"));

            }
        });
        /* ----------------------------------------- */
    }
    public void  GetTarifaRuta(){
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        ConnectivityManager cm =
                (ConnectivityManager)  getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        /* ----------------------------------------- */

        /* Validación si el equipo tiene conectividad */
        if (isConnected) {
            SharedPreferences sharedPreferences;
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String ws_Valida_Tarifa = getString(R.string.ws_ruta) + "TMVALIDA_TARIFA/" + sharedPreferences.getString("anf_rumbo", "NoData") + "/" + sharedPreferences.getString("anf_fechaProgramacion", "NoData")
                    + "/" + sharedPreferences.getString("anf_secuencia", "NoData") + "/" + sharedPreferences.getString("NU_TARI", "NoData") + "/" + sharedPreferences.getString("CO_TIPO", "NoData");

            final String RUMBO = sharedPreferences.getString("anf_rumbo","NoData");
            final String FE_PROG_ITIN = sharedPreferences.getString("anf_fechaProgramacion","NoData");
            final String NU_SECU = sharedPreferences.getString("anf_secuencia","NoData");
            final String NU_TARI = sharedPreferences.getString("NU_TARI","NoData");
            final String CO_TIPO = sharedPreferences.getString("CO_TIPO","NoData");
            final String CO_EMPR = sharedPreferences.getString("anf_codigoEmpresa","NoData");
            /* ----------------------------------------- */
            Log.d("urlWS_TARIFA", ws_Valida_Tarifa);
            /* Request que obtiene todas las tarifas de viaje y carga */
            JsonArrayRequest jsonArrayRequestTarifa = new JsonArrayRequest(Request.Method.GET, ws_Valida_Tarifa, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.length() == 0) {
                                Toast.makeText(getContext(), "NO SE ENCUENTRA UN NUEVO TARIFARIO ASIGNADO", Toast.LENGTH_LONG).show();
                            } else if (response.length() > 0) {
                                try {
                                    JSONObject info;
                                    info = response.getJSONObject(0);
                                    String Resultado = info.getString("Respuesta");
                                    //Log.d("pruebaSincroniza", Resultado.toString());
                                    if (Resultado.equals("1")) {
                                        //Log.d("pruebaSincroniza", "1");
                                        Tarifario(RUMBO,CO_EMPR,NU_SECU,FE_PROG_ITIN);
                                    }else{
                                        Toast.makeText(getContext(), "NO SE AH MODIFICADO EL TARIFARIO AUN", Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(getActivity(), "Error en la ws getTarifas.", Toast.LENGTH_LONG).show();
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
            queue.add(jsonArrayRequestTarifa);
        }

    }
    void Tarifario(String Rumbo, final String Empresa, String Secuencia, String FechaProgramacion)
    {
        final RequestQueue queue = Volley.newRequestQueue(getContext());
        String ws_getTarifa = getString(R.string.ws_ruta) + "GetTarifa/" + Rumbo + "/" + Empresa + "/" +
                Secuencia + "/" + FechaProgramacion;
        /* ----------------------------------------- */
        Log.d("urlWS_TARIFA", ws_getTarifa);
        /* Request que obtiene todas las tarifas de viaje y carga */
        JsonArrayRequest jsonArrayRequestTarifa = new JsonArrayRequest(Request.Method.GET, ws_getTarifa, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) {
                            /* En caso el usuario no tenga itinerario asignados no puede iniciar sesión */
                            Toast.makeText(getContext(), "No hay tarifas para este itinerario.", Toast.LENGTH_LONG).show();
                            /* ----------------------------------------- */

                        }else if (response.length() > 0) {
                            try {
                                JSONObject info;
                                ArrayList<String> lista_tarifasViaje = new ArrayList<>();
                                //ArrayList<String> lista_tarifasCarga = new ArrayList<>();
                                //final String NU_TARI =  info = response.getJSONObject(0);
                                info = response.getJSONObject(0);
                                guardarDataMemoria("NU_TARI",info.getString("NU_TARI"),getContext());
                                guardarDataMemoria("CO_TIPO",info.getString("CO_TIPO"),getContext());

                                /* Se generan dos tramas, una con las tarifas de viaje y otra de carga, y ambas se guardan en memoria */
                                for (int i = 0; i < response.length(); i++) {
                                    info = response.getJSONObject(i);

                                    /*if (info.getString("TI_TARI").equals("TARIFA VIAJE")) {*/

                                    lista_tarifasViaje.add(info.getString("CO_DEST_ORIG") + "-"
                                            + info.getString("CO_DEST_FINA") + "-" + Integer.toString(info.getInt("PR_BASE")));

                                    //Integer.toString(info.getInt("PR_BASE")));

                                    /*}
                                    /*else if (info.getString("TI_TARI").equals("TARIFA CARGA")) {

                                        lista_tarifasCarga.add(info.getString("PRODUCTO") + "-"
                                                + info.getString("ORIGEN") + "-"
                                                + info.getString("DESTINO") + "-"
                                                + Integer.toString(info.getInt("IMPORTE")));
                                    }*/
                                }

                                String jsonTarifasViaje = gson.toJson(lista_tarifasViaje);
                                guardarDataMemoria("anf_jsonTarifasViaje", jsonTarifasViaje, getContext());

                                TarifarioFragment Tarifario = new TarifarioFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_base, Tarifario).commit();
                                /*String jsonTarifasCarga = gson.toJson(lista_tarifasCarga);
                                guardarDataMemoria("anf_jsonTarifasCarga", jsonTarifasCarga, getApplicationContext());
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
                Toast.makeText(getContext(), "ERROR DE COBERTURA", Toast.LENGTH_LONG).show();
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
        queue.add(jsonArrayRequestTarifa);
    }
    void ImprimeTarifa(IPrinter printerx, ArrayList<String> Tarifario, String CO_EMPR)
    {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {

            /* Se inicializa la impresora del equipo */
            printerx.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printerx.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            Date date = new Date();
            String FechaImpresion = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(date);

            StringBuilder Ticket= new StringBuilder();
            Ticket.append("--------------------------------\n");
            Ticket.append("   COD_BUS  : "+ sharedPreferences.getString("anf_codigoVehiculo","Nodata")+"\n");
            Ticket.append("   TARIFARIO: "+sharedPreferences.getString("NU_TARI","NoData")+"\n");
            Ticket.append("   EMPRESA: "+CO_EMPR+"\n");
            Ticket.append("   FECHA: "+FechaImpresion+"\n");
            Ticket.append("   RUMBO: "+sharedPreferences.getString("anf_rumbo","NoData")+"\n");
            Ticket.append("--------------------------------\n");
            int Elementro=1;
            for (int i=0;i<Tarifario.size();i++)
            {

                String[] Tari = Tarifario.get(i).split("-");
                Ticket.append("|"+Tari[0]+"-"+Tari[1]+"->"+Tari[2]+"|");
                if(Elementro==3){
                    Ticket.append("\n");
                    Ticket.append("--------------------------------\n");
                    Elementro=0;
                }
                Elementro=Elementro+1;
            }

            Ticket.append("\n\n\n\n\n");
            printerx.printStr(Ticket.toString(), null);
            int iRetError = printerx.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Error al inicializar la impresora.", Toast.LENGTH_LONG).show();

            //Intent intent = new Intent(getActivity(), ErrorActivity.class);
            //startActivity(intent);
        }
    }
}
