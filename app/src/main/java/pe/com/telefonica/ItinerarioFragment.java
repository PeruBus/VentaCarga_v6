package pe.com.telefonica.soyuz;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarIntegerMemoria;


public class ItinerarioFragment extends Fragment {


    ProgressDialog progressDialog;
    RequestQueue queue;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.itinerario, parent, false);
    }


    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        startBoletoService();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        final TableLayout tableLayout = view.findViewById(R.id.table_layout);

        final EditText editText_codBus = view.findViewById(R.id.codBus);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText_codBus.getWindowToken(), 0);
        imm.showSoftInput(editText_codBus, 0);

        Button btn_buscar = view.findViewById(R.id.btn_buscar);



        if (sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_CONTROL") ||
                sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_INSP")) {

            btn_buscar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /* Se obtiene un arreglo con todos los itinerarios */
                    String jsonItinerario = sharedPreferences.getString("jsonItinerario", "nada");
                    Type type = new TypeToken<ArrayList<String>>() {}.getType();
                    ArrayList<String> lista_itinerario = new ArrayList<>();
                    if (jsonItinerario.equals("nada")) {

                    } else {
                        lista_itinerario = gson.fromJson(jsonItinerario, type);
                    }
                    /* ----------------------------------------- */

                    /* Se itera en función a la lista de itinerarios */
                    for (int i = 0; i < lista_itinerario.size(); i++) {
                        String[] itinerario = lista_itinerario.get(i).split("/");
                        // itinerario[0] = CONDUCTOR
                        // itinerario[1] = CODIGO_CONDUCTOR
                        // itinerario[2] = ANFITRION
                        // itinerario[3] = CODIGO_ANFITIRON
                        // itinerario[4] = CODIGO_VEHICULO
                        // itinerario[5] = CODIGO_EMPRESA
                        // itinerario[6] = ORIGEN
                        // itinerario[7] = DESTINO
                        // itinerario[8] = RUMBO
                        // itinerario[9] = FECHA PROGRAMACION
                        // itinerario[10] = SECUENCIA
                        // itinerario[11] = TIPO_BUS
                        // itinerario[12] = HORA_SALIDA
                        // itinerario[13] = SERVICIO

                        /* Validación en caso se encuentre el código de bus buscado */
                        String Codigo_bus ="0"+editText_codBus.getText();
                        if (Codigo_bus.toString().equals(itinerario[4])) {
                            guardarIntegerMemoria("indiceSeleccionado", i, getActivity());

                            /* Se obtiene un arreglo con todos los destinos */
                            String json_destinos = sharedPreferences.getString("json_destinos", "nada");
                            type = new TypeToken<ArrayList<String>>() {}.getType();
                            ArrayList<String> lista_destinos = gson.fromJson(json_destinos, type);
                            /* ----------------------------------------- */

                            /* Se crea la fila y se configura sus propiedades */
                            TableRow tableRow = new TableRow(getActivity());
                            tableRow.setId(0);

                            TableRow r = (TableRow) tableLayout.getChildAt(2);
                            tableLayout.removeView(r);

                            tableRow.setLayoutParams(new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT));
                            tableRow.setPadding(10, 10, 10, 10);
                            tableRow.setClickable(true);

                            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
                            TableRow.LayoutParams params1 = new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT, 1);
                            params.setMargins(0, 0, 10, 0);
                            /* ----------------------------------------- */

                            /* Se agrega un listener a cada fila creada */
                            tableRow.setOnClickListener(onClickListener);
                            /* ----------------------------------------- */

                            TextView secuencia = new TextView(getContext());
                            secuencia.setLayoutParams(params);
                            secuencia.setTextColor(Color.parseColor("#000000"));
                            secuencia.setGravity(Gravity.CENTER);

                            TextView conductor = new TextView(getContext());
                            conductor.setLayoutParams(params1);
                            conductor.setTextColor(Color.parseColor("#000000"));
                            conductor.setGravity(Gravity.CENTER);

                            TextView anfitrion = new TextView(getContext());
                            anfitrion.setLayoutParams(params1);
                            anfitrion.setTextColor(Color.parseColor("#000000"));
                            anfitrion.setGravity(Gravity.CENTER);

                            TextView vehiculo = new TextView(getContext());
                            vehiculo.setLayoutParams(params);
                            vehiculo.setTextColor(Color.parseColor("#000000"));
                            vehiculo.setGravity(Gravity.CENTER);

                            TextView empresa = new TextView(getContext());
                            empresa.setLayoutParams(params);
                            empresa.setTextColor(Color.parseColor("#000000"));
                            empresa.setGravity(Gravity.CENTER);

                            TextView origen = new TextView(getContext());
                            origen.setLayoutParams(params);
                            origen.setTextColor(Color.parseColor("#000000"));
                            origen.setGravity(Gravity.CENTER);

                            TextView destino = new TextView(getContext());
                            destino.setLayoutParams(params);
                            destino.setTextColor(Color.parseColor("#000000"));
                            destino.setGravity(Gravity.CENTER);

                            TextView rumbo = new TextView(getContext());
                            rumbo.setLayoutParams(params);
                            rumbo.setTextColor(Color.parseColor("#000000"));
                            rumbo.setGravity(Gravity.CENTER);

                            TextView fecha = new TextView(getContext());
                            fecha.setLayoutParams(params);
                            fecha.setTextColor(Color.parseColor("#000000"));
                            fecha.setGravity(Gravity.CENTER);

                            /* Se obtienen los valores de secuencia, conductor, anfitrion, vehiculo, empresa, origen, destino, rumbo y fecha */
                            secuencia.setText(itinerario[10]);
                            conductor.setText(itinerario[0]);
                            anfitrion.setText(itinerario[2]);
                            vehiculo.setText(itinerario[4]);
                            empresa.setText(itinerario[5]);

                            for (int j = 0; j < lista_destinos.size(); j++) {
                                String[] data = lista_destinos.get(j).split("-");
                                // dataDestinos[0] = CO_DEST
                                // dataDestinos[1] = DE_DEST

                                if (data[0].equals(itinerario[6])) {
                                    origen.setText(data[1]);
                                    break;
                                }
                            }

                            for (int m = 0; m < lista_destinos.size(); m++) {
                                String[] data = lista_destinos.get(m).split("-");
                                // dataDestinos[0] = CO_DEST
                                // dataDestinos[1] = DE_DEST

                                if (data[0].equals(itinerario[7])) {
                                    destino.setText(data[1]);
                                    break;
                                }
                            }

                            rumbo.setText(itinerario[8]);
                            fecha.setText(itinerario[9]);
                            /* ----------------------------------------- */

                            /* Cada TextView se añade a la fila y la fila se añade a la tabla */
                            tableRow.addView(secuencia);
                            tableRow.addView(conductor);
                            tableRow.addView(anfitrion);
                            tableRow.addView(vehiculo);
                            tableRow.addView(empresa);
                            tableRow.addView(origen);
                            tableRow.addView(destino);
                            tableRow.addView(rumbo);
                            tableRow.addView(fecha);

                            tableLayout.addView(tableRow);
                            /* ----------------------------------------- */
                        }
                        /* ----------------------------------------- */
                    }
                    /* ----------------------------------------- */
                }
            });
            /* ----------------------------------------- */

        } else if (sharedPreferences.getString("puestoUsuario", "NoData").equals("BOLETERO")) {
            /* Caso cuando el rol de usuario es Boletero */

            /* Se deshabilita las opciones de búsqueda de bus */
            editText_codBus.setVisibility(View.INVISIBLE);
            btn_buscar.setVisibility(View.INVISIBLE);
            /* ----------------------------------------- */

            /* Se genera un arreglo con todos los itinerarios */
            String jsonItinerario = sharedPreferences.getString("jsonItinerario", "nada");
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> lista_itinerario = new ArrayList<>();
            if (jsonItinerario.equals("nada")) {

            } else {
                lista_itinerario = gson.fromJson(jsonItinerario, type);
            }
            /* ----------------------------------------- */

            /* Se itera en función a la lista de itinerarios */
            for (int i = 0; i < lista_itinerario.size(); i++) {

                /* Se obtiene un arreglo con todos los destinos */
                String json_destinos = sharedPreferences.getString("json_destinos", "nada");
                type = new TypeToken<ArrayList<String>>() {}.getType();
                ArrayList<String> lista_destinos = gson.fromJson(json_destinos, type);
                /* ----------------------------------------- */

                /* Se obtiene un itinerario de la lista de itinerarios */
                String[] itinerario = lista_itinerario.get(i).split("/");
                /* ----------------------------------------- */

                /* Se crea la fila y se configura sus propiedades */
                TableRow tableRow = new TableRow(getActivity());
                tableRow.setId(i);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.setPadding(10, 10, 10, 10);
                tableRow.setClickable(true);

                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
                TableRow.LayoutParams params1 = new TableRow.LayoutParams(300, TableRow.LayoutParams.WRAP_CONTENT, 1);
                params.setMargins(0, 0, 10, 0);
                /* ----------------------------------------- */

                /* Se agrega un listener a cada fila creada */
                tableRow.setOnClickListener(onClickListener);
                /* ----------------------------------------- */

                TextView secuencia = new TextView(getContext());
                secuencia.setLayoutParams(params);
                secuencia.setTextColor(Color.parseColor("#000000"));
                secuencia.setGravity(Gravity.CENTER);

                TextView conductor = new TextView(getContext());
                conductor.setLayoutParams(params1);
                conductor.setTextColor(Color.parseColor("#000000"));
                conductor.setGravity(Gravity.CENTER);

                TextView anfitrion = new TextView(getContext());
                anfitrion.setLayoutParams(params1);
                anfitrion.setTextColor(Color.parseColor("#000000"));
                anfitrion.setGravity(Gravity.CENTER);

                TextView vehiculo = new TextView(getContext());
                vehiculo.setLayoutParams(params);
                vehiculo.setTextColor(Color.parseColor("#000000"));
                vehiculo.setGravity(Gravity.CENTER);

                TextView empresa = new TextView(getContext());
                empresa.setLayoutParams(params);
                empresa.setTextColor(Color.parseColor("#000000"));
                empresa.setGravity(Gravity.CENTER);

                TextView origen = new TextView(getContext());
                origen.setLayoutParams(params);
                origen.setTextColor(Color.parseColor("#000000"));
                origen.setGravity(Gravity.CENTER);

                TextView destino = new TextView(getContext());
                destino.setLayoutParams(params);
                destino.setTextColor(Color.parseColor("#000000"));
                destino.setGravity(Gravity.CENTER);

                TextView rumbo = new TextView(getContext());
                rumbo.setLayoutParams(params);
                rumbo.setTextColor(Color.parseColor("#000000"));
                rumbo.setGravity(Gravity.CENTER);

                TextView fecha = new TextView(getContext());
                fecha.setLayoutParams(params);
                fecha.setTextColor(Color.parseColor("#000000"));
                fecha.setGravity(Gravity.CENTER);

                /* Se obtienen los valores de secuencia, conductor, anfitrion, vehiculo, empresa, origen, destino, rumbo y fecha */
                secuencia.setText(itinerario[10]);
                conductor.setText(itinerario[0]);
                anfitrion.setText(itinerario[2]);
                vehiculo.setText(itinerario[4]);
                empresa.setText(itinerario[5]);

                for (int j = 0; j < lista_destinos.size(); j++) {
                    String[] data = lista_destinos.get(j).split("-");
                    // dataDestinos[0] = CO_DEST
                    // dataDestinos[1] = DE_DEST

                    if (data[0].equals(itinerario[6])) {
                        origen.setText(data[1]);
                        break;
                    }
                }

                for (int m = 0; m < lista_destinos.size(); m++) {
                    String[] data = lista_destinos.get(m).split("-");
                    // dataDestinos[0] = CO_DEST
                    // dataDestinos[1] = DE_DEST

                    if (data[0].equals(itinerario[7])) {
                        destino.setText(data[1]);
                        break;
                    }
                }

                rumbo.setText(itinerario[8]);
                fecha.setText(itinerario[9]);
                /* ----------------------------------------- */

                /* Cada TextView se añade a la fila y la fila se añade a la tabla */
                tableRow.addView(secuencia);
                tableRow.addView(conductor);
                tableRow.addView(anfitrion);
                tableRow.addView(vehiculo);
                tableRow.addView(empresa);
                tableRow.addView(origen);
                tableRow.addView(destino);
                tableRow.addView(rumbo);
                tableRow.addView(fecha);

                tableLayout.addView(tableRow);
                /* ----------------------------------------- */
            }
            /* ----------------------------------------- */
        }
        /* ----------------------------------------- */
    }

	/**
	* Obtiene la información correspondiente al bus seleccionado mediente consultas a la Web service.
	*/
    /* TODO: CLICK LISTENER PARA LA SELECCIÓN DE UNA FILA */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(final View v) {

            progressDialog = new ProgressDialog(getActivity());

            progressDialog.setTitle("Espere por favor");
            progressDialog.setMessage("Cargando itinerario seleccionado...");
            progressDialog.setCancelable(false);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    /*final RequestQueue queue = Volley.newRequestQueue(getActivity());*/
                    final Gson gson = new Gson();
                    String jsonItinerario = sharedPreferences.getString("jsonItinerario", "nada");
                    Type type = new TypeToken<ArrayList<String>>() {}.getType();
                    ArrayList<String> lista_itinerario = gson.fromJson(jsonItinerario, type);
                    String[] itinerarioSeleccionado = new String[0];

                    if (sharedPreferences.getString("Modulo", "nada").equals("ANDROID_CONTROL")){

                        itinerarioSeleccionado = lista_itinerario.get(sharedPreferences.getInt("indiceSeleccionado", 0)).split("/");

                        guardarDataMemoria("anf_codigoEmpresa", itinerarioSeleccionado[5], getActivity());
                        guardarDataMemoria("anf_rumbo", itinerarioSeleccionado[8], getActivity());
                        guardarDataMemoria("anf_fechaProgramacion", itinerarioSeleccionado[9], getActivity());
                        guardarDataMemoria("anf_secuencia", itinerarioSeleccionado[10], getActivity());
                        guardarDataMemoria("anf_horaSalida",itinerarioSeleccionado[12], getActivity());
                        guardarDataMemoria("co_vehi_asig",itinerarioSeleccionado[4],getActivity());

                        Log.d("Itinerario_sel", lista_itinerario.get(sharedPreferences.getInt("indiceSeleccionado", 0)).toString());
                        progressDialog.dismiss();
                        mataBoleto trasbordoFragment = new mataBoleto();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_base, trasbordoFragment).commit();
                    }else if(sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_INSP")){
                        itinerarioSeleccionado = lista_itinerario.get(sharedPreferences.getInt("indiceSeleccionado", 0)).split("/");
                        InspectorRutaCarga(itinerarioSeleccionado[5],itinerarioSeleccionado[8],itinerarioSeleccionado[9],itinerarioSeleccionado[10],itinerarioSeleccionado[12],itinerarioSeleccionado[4]);

                    }
                   /* Log.d("Itinerario_sel", lista_itinerario.get(sharedPreferences.getInt("indiceSeleccionado", 0)).toString());
                    progressDialog.dismiss();
                    mataBoleto trasbordoFragment = new mataBoleto();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, trasbordoFragment).commit();*/

                }
            });
            thread.start();
            progressDialog.show();

        }
    };
    void InspectorRutaCarga(String CO_EMPR,String CO_RUMB,String FE_PROG,String NU_SECU,String HO_SALI,String CO_VEHI)
    {
        try{
            ObtieneBoletos(CO_EMPR,CO_RUMB,FE_PROG,NU_SECU,HO_SALI,CO_VEHI);
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    void ObtieneBoletos(String CO_EMPR,String CO_RUMB,String FE_PROG,String NU_SECU,String HO_SALI,String CO_VEHI)
    {
        try{
            queue = Volley.newRequestQueue(getContext());
            final Gson gson = new Gson();
            String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + CO_EMPR + "/" + NU_SECU  + "/" + CO_RUMB + "/" + FE_PROG;
            final ArrayList<String> lista_reporteVenta = new ArrayList<>();
            JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {

                                        String asientosVend = "";

                                        if (response.length() == 0) {
                                            asientosVend = "NoData";
                                            lista_reporteVenta.add(asientosVend);
                                            String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                            guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());

                                            progressDialog.dismiss();

                                            String flagInspeccionVenta = "true";
                                            guardarDataMemoria("flagInspeccionVenta",flagInspeccionVenta,getContext());
                                            /*editor.putString("flagInspeccionVenta", flagInspeccionVenta);
                                            editor.commit();*/

                                            /* Cuadro de pregunta*/
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("¿El Anfitrión se encuentra en cabina?").setTitle("Inspección");
                                            /* ----------------------------------------- */

                                            /* Borón SI del cuadro de pregunta*/
                                            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                    /* Se cambia a la vista de inspección de venta*/
                                                    InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                                    FragmentManager fragmentManager = getFragmentManager();
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();
                                                    /* -----------------------------------------*/

                                                }
                                            });
                                            /* -----------------------------------------*/

                                            /* Botón NO del cuadro de pregunta*/
                                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Toast.makeText(getActivity(), "Generación de ocurrencia.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            /* -----------------------------------------*/

                                            AlertDialog dialog = builder.create();
                                            dialog.setCancelable(false);
                                            dialog.show();

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

                                                /* Se elimina el último "/" de la trama, se agrega al arreglo y se guarda en memoria*/
                                                asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                                lista_reporteVenta.add(asientosVend);
                                                String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                                guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());
                                                /* -----------------------------------------*/

                                                progressDialog.dismiss();

                                                /* Cuadro de pregunta*/
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                builder.setMessage("¿El Anfitrión se encuentra en el bus?").setTitle("Inspección");
                                                /* ----------------------------------------- */

                                                /* Borón SI del cuadro de pregunta*/
                                                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        /* Se cambia a la vista de inspección de venta*/
                                                        InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                                        FragmentManager fragmentManager = getFragmentManager();
                                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                        fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();
                                                        /* -----------------------------------------*/
                                                    }
                                                });
                                                /* -----------------------------------------*/

                                                /* Botón NO del cuadro de pregunta*/
                                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        /* Se cambia a la vista de inspección de venta*/
                                                        InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                                        FragmentManager fragmentManager = getFragmentManager();
                                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                        fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();
                                                        /* -----------------------------------------*/

                                                        Toast.makeText(getActivity(), "Generación de ocurrencia.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                /* -----------------------------------------*/

                                                AlertDialog dialog = builder.create();
                                                dialog.setCancelable(false);
                                                dialog.show();

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
    /* Se obtiene un arreglo con todos los itinerarios
                    String jsonItinerario = sharedPreferences.getString("jsonItinerario", "nada");
                    Type type = new TypeToken<ArrayList<String>>() {}.getType();
                    ArrayList<String> lista_itinerario = gson.fromJson(jsonItinerario, type);
                    /* -----------------------------------------

                    String[] itinerarioSeleccionado = new String[0];
                    // itinerarioSeleccionado[0] = CONDUCTOR
                    // itinerarioSeleccionado[1] = CODIGO_CONDUCTOR
                    // itinerarioSeleccionado[2] = ANFITRION
                    // itinerarioSeleccionado[3] = CODIGO_ANFITIRON
                    // itinerarioSeleccionado[4] = CODIGO_VEHICULO
                    // itinerarioSeleccionado[5] = CODIGO_EMPRESA
                    // itinerarioSeleccionado[6] = ORIGEN
                    // itinerarioSeleccionado[7] = DESTINO
                    // itinerarioSeleccionado[8] = RUMBO
                    // itinerarioSeleccionado[9] = FECHA PROGRAMACION
                    // itinerarioSeleccionado[10] = SECUENCIA
                    // itinerarioSeleccionado[11] = TIPO_BUS
                    // itinerarioSeleccionado[12] = HORA_SALIDA
                    // itinerarioSeleccionado[13] = SERVICIO

                    /* Se guarda en memoria los datos necesarios dependiendo del rol del usuario que inició sesión
                    if (sharedPreferences.getString("puestoUsuario", "nada").equals("ANFITRION ESTANDAR")) {

                        itinerarioSeleccionado = lista_itinerario.get(sharedPreferences.getInt("indiceSeleccionado", 0)).split("/");

                        guardarDataMemoria("tras_codigoEmpresa", itinerarioSeleccionado[5], getActivity());
                        guardarDataMemoria("tras_rumbo", itinerarioSeleccionado[8], getActivity());
                        guardarDataMemoria("tras_fechaProgramacion", itinerarioSeleccionado[9], getActivity());
                        guardarDataMemoria("tras_secuencia", itinerarioSeleccionado[10], getActivity());

                    }else if (sharedPreferences.getString("puestoUsuario", "nada").equals("INSPECTOR DE RUTA")) {

                        itinerarioSeleccionado = lista_itinerario.get(sharedPreferences.getInt("indiceSeleccionado", 0)).split("/");

                        guardarDataMemoria("insp_codigoEmpresa", itinerarioSeleccionado[5], getActivity());
                        guardarDataMemoria("insp_codigoConductor", itinerarioSeleccionado[1], getActivity());
                        guardarDataMemoria("insp_codigoAnfitrion", itinerarioSeleccionado[3], getActivity());
                        guardarDataMemoria("insp_codigoVehiculo", itinerarioSeleccionado[4], getActivity());
                        guardarDataMemoria("insp_origen", itinerarioSeleccionado[6], getActivity());
                        guardarDataMemoria("insp_destino", itinerarioSeleccionado[7], getActivity());
                        guardarDataMemoria("insp_rumbo", itinerarioSeleccionado[8], getActivity());
                        guardarDataMemoria("insp_fechaProgramacion", itinerarioSeleccionado[9], getActivity());
                        guardarDataMemoria("insp_secuencia", itinerarioSeleccionado[10], getActivity());

                    } else if (sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")) {

                        itinerarioSeleccionado = lista_itinerario.get(v.getId()).split("/");
                        guardarDataMemoria("anf_codigoEmpresa", itinerarioSeleccionado[5], getActivity());
                        guardarDataMemoria("anf_codigoVehiculo", itinerarioSeleccionado[4], getActivity());
                        guardarDataMemoria("anf_fechaProgramacion", itinerarioSeleccionado[9], getActivity());
                        guardarDataMemoria("anf_horaSalida", itinerarioSeleccionado[12], getActivity());
                        guardarDataMemoria("anf_servicio", itinerarioSeleccionado[13], getActivity());
                        guardarDataMemoria("anf_origen", itinerarioSeleccionado[6], getActivity());
                        guardarDataMemoria("anf_destino", itinerarioSeleccionado[7], getActivity());
                        guardarDataMemoria("anf_secuencia", itinerarioSeleccionado[10], getActivity());
                        guardarDataMemoria("anf_tipoBus", itinerarioSeleccionado[11], getActivity());
                        guardarDataMemoria("anf_rumbo", itinerarioSeleccionado[8], getActivity());
                        guardarDataMemoria("anf_nombre", itinerarioSeleccionado[2], getActivity());

                    }
                    /* ----------------------------------------- */

                    /* Se realizan request a la Web service dependiendo del rol del usuario que ha iniciado sesión
                    if (sharedPreferences.getString("puestoUsuario", "nada").equals("ANFITRION ESTANDAR")) {

                        /* Ruta de la Web service
                        String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + itinerarioSeleccionado[5] + "/" + itinerarioSeleccionado[10] + "/" + itinerarioSeleccionado[8] + "/" + itinerarioSeleccionado[9];
                        /* -----------------------------------------

                        Log.d("Transbordows",ws_getAsientosVendidos);

                        /* Arreglos
                        // lista_reporteVenta: arreglo que contiene los asientos vendidos
                        // lista_trasbordo: arreglo que se va a usar para guardar los boletos que van a ser transbordados
                        final ArrayList<String> lista_reporteVenta = new ArrayList<>();
                        final ArrayList<String> lista_trasbordo = new ArrayList<>();
                        /* ----------------------------------------- */

                        /* Request que obtiene los asientos vendidos
                        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {

                                        String asientosVend = "";


                                        if (response.length() == 0) {

                                            asientosVend = "NoData";
                                            lista_reporteVenta.add(asientosVend);
                                            String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                            guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());

                                            String json_trasbordo = gson.toJson(lista_trasbordo);
                                            guardarDataMemoria("insp_jsonTrasbordo", json_trasbordo, getActivity());

                                            progressDialog.dismiss();

                                            /* Se cambia a la vista de trasbordo
                                            TrasbordoFragment trasbordoFragment = new TrasbordoFragment();
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_base, trasbordoFragment).commit();
                                            /* -----------------------------------------
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

                                                /* Se elimina el último "/" de la trama, se agrega al arreglo y se guarda en memoria
                                                asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                                lista_reporteVenta.add(asientosVend);
                                                String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                                guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());
                                                /* ----------------------------------------- */

                                                /* Se guarda el arreglo de trasbordo en memoria
                                                String json_trasbordo = gson.toJson(lista_trasbordo);
                                                guardarDataMemoria("insp_jsonTrasbordo", json_trasbordo, getActivity());
                                                /* -----------------------------------------

                                                progressDialog.dismiss();

                                                /* Se cambia a la vista de trasbordo
                                                TrasbordoFragment trasbordoFragment = new TrasbordoFragment();
                                                FragmentManager fragmentManager = getFragmentManager();
                                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                fragmentTransaction.replace(R.id.fragment_base, trasbordoFragment).commit();
                                                /* -----------------------------------------
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(getActivity(), "Error en la ws de ReporteVenta.", Toast.LENGTH_SHORT).show();
                               // errorWS(queue, error);
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
                        /* -----------------------------------------
                        queue.add(jsonArrayRequestAsientosVendidos);
                        breakTime();

                    }else if (sharedPreferences.getString("puestoUsuario", "nada").equals("INSPECTOR DE RUTA")) {

                        /* Ruta de la Web service
                        String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + itinerarioSeleccionado[5] + "/" + itinerarioSeleccionado[10] + "/" + itinerarioSeleccionado[8] + "/" + itinerarioSeleccionado[9];
                        /* ----------------------------------------- */

                        /* Arreglo
                        // lista_reporteVenta: arreglo que va a contener los asientos vendidos
                        final ArrayList<String> lista_reporteVenta = new ArrayList<>();
                        /* -----------------------------------------

                        /* Request que obtiene los asientos vendidos
                        JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {

                                        String asientosVend = "";

                                        if (response.length() == 0) {

                                            asientosVend = "NoData";
                                            lista_reporteVenta.add(asientosVend);
                                            String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                            guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());

                                            progressDialog.dismiss();

                                            String flagInspeccionVenta = "true";
                                            editor.putString("flagInspeccionVenta", flagInspeccionVenta);
                                            editor.commit();

                                            /* Cuadro de pregunta
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("¿El Anfitrión se encuentra en cabina?").setTitle("Inspección");
                                            /* ----------------------------------------- */

                                            /* Borón SI del cuadro de pregunta
                                            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                    /* Se cambia a la vista de inspección de venta
                                                    InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                                    FragmentManager fragmentManager = getFragmentManager();
                                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                    fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();
                                                    /* -----------------------------------------

                                                }
                                            });
                                            /* -----------------------------------------

                                            /* Botón NO del cuadro de pregunta
                                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Toast.makeText(getActivity(), "Generación de ocurrencia.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            /* -----------------------------------------

                                            AlertDialog dialog = builder.create();
                                            dialog.setCancelable(false);
                                            dialog.show();

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

                                                /* Se elimina el último "/" de la trama, se agrega al arreglo y se guarda en memoria
                                                asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                                lista_reporteVenta.add(asientosVend);
                                                String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                                guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());
                                                /* -----------------------------------------

                                                progressDialog.dismiss();

                                                /* Cuadro de pregunta
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                builder.setMessage("¿El Anfitrión se encuentra en el bus?").setTitle("Inspección");
                                                /* ----------------------------------------- */

                                                /* Borón SI del cuadro de pregunta
                                                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        /* Se cambia a la vista de inspección de venta
                                                        InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                                        FragmentManager fragmentManager = getFragmentManager();
                                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                        fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();
                                                        /* -----------------------------------------
                                                    }
                                                });
                                                /* -----------------------------------------

                                                /* Botón NO del cuadro de pregunta
                                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {

                                                        /* Se cambia a la vista de inspección de venta
                                                        InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                                                        FragmentManager fragmentManager = getFragmentManager();
                                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                        fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commit();
                                                        /* -----------------------------------------

                                                        Toast.makeText(getActivity(), "Generación de ocurrencia.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                /* -----------------------------------------

                                                AlertDialog dialog = builder.create();
                                                dialog.setCancelable(false);
                                                dialog.show();

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
                        /* -----------------------------------------
                        queue.add(jsonArrayRequestAsientosVendidos);
                        breakTime();

                    }
                }
                /* ----------------------------------------- */

    private void errorWS(RequestQueue queue, VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar conectividad del dispositivo.", Toast.LENGTH_LONG).show();

        }else if (error instanceof TimeoutError) {
            Toast.makeText(getActivity(), "Se excedió el tiempo de espera.", Toast.LENGTH_LONG).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getActivity(), "Error en la autenticación.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ServerError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar credenciales e IP del servidor.", Toast.LENGTH_LONG).show();

        } else if (error instanceof NetworkError) {
            Toast.makeText(getActivity(), "No hay conectividad.", Toast.LENGTH_LONG).show();

        }else if (error instanceof ParseError) {
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor.", Toast.LENGTH_LONG).show();

        }

        queue.getCache().clear();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });

        Intent intent = new Intent(getActivity(), ErrorActivity.class);
        startActivity(intent);
    }
}
