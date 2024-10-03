package pe.com.telefonica.soyuz;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
import com.google.gson.JsonObject;
import com.pax.dal.IDAL;
import com.pax.dal.IScanCodec;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class mataBoleto extends Fragment {
    ListView listView;
    private Boolean existe = false;
    private SharedPreferences sharedPreferences;
    final ArrayList<String> lista_boletosLeidos = new ArrayList<>();
    Button btn_buscarItin;
    RequestQueue queue;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;
    ProgressDialog progressDialog;
    ArrayList<String> ValidaDestinoViaje;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        return inflater.inflate(R.layout.embarque_codqr, parent, false);
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        final Gson gson = new Gson();

        final Context contextBuscarBoleto = view.getContext();
        queue = Volley.newRequestQueue(this.getContext());

        final Button btn_Camara = view.findViewById(R.id.btn_Camara);
        final Button btn_leerQR = view.findViewById(R.id.btn_leerQR);
        final Button btn_CambioBus = view.findViewById(R.id.btnBus);
        //final Button btn_buscarItin = view.findViewById(R.id.btn_buscarItin);
        btn_buscarItin = view.findViewById(R.id.btn_buscarItin);
        final Button btn_buscarItin_1 = view.findViewById(R.id.btn_buscarItin_1);
        final TextView txt_TituloEmbarque = view.findViewById(R.id.TituloEmbarque);
        listView = view.findViewById(R.id.listView_boletosLeidos);
        txt_TituloEmbarque.setText("CONTROL BOLETOS");

        btn_buscarItin.setText("Asignar");
        btn_buscarItin.setEnabled(false);

        if(sharedPreferences.getString("Modulo","NoData").equals("ANDROID_INSP"))
        {
            ValidaDestinoViaje = FuncionesAuxiliares.getArray(sharedPreferences,gson,"CTRL_DEST");
            btn_buscarItin.setText("Fin Inspeccion");
            btn_buscarItin.setEnabled(false);
        }



        AgregaBoletos(view);

        btn_leerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View vista) {

                try {

                    /* Se inicializa el scanner del equipo */
                    final IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
                    IScanner iScanner = dal.getScanner(EScannerType.REAR);
                    /* ----------------------------------------- */

                    if (iScanner.open()) {
                        if(sharedPreferences.getString("Modulo","NoData").equals("ANDROID_CONTROL")||
                                sharedPreferences.getString("Modulo","NoData").equals("ANDROID_INSP"))
                        {
                            iScanner.setContinuousInterval(1000);
                            iScanner.setContinuousTimes(0);
                        }
                        iScanner.start(new IScanner.IScanListener() {

                            @Override
                            public void onRead(String codigoQR) {
//                                if(sharedPreferences.getString("Modulo","NoData").equals("ANDROID_INSP"))
//                                {
                                try{
                                    final TextView txtCant = view.findViewById(R.id.txtCant);
                                    ValidaTramaInspectorRuta(codigoQR,vista);
                                    txtCant.setText("  "+lista_boletosLeidos.size());
                                }catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), "INGRESAR EL BOLETO MANUALMENTE.", Toast.LENGTH_LONG).show();
                                }

//                                }
//                                else{
//                                    //ValidaTrama(codigoQR,vista);
//                                    ValidaTramaControlador(codigoQR,vista);
//                                }
 //                               existe = false;
                            }

                            @Override
                            public void onFinish() {}

                            @Override
                            public void onCancel() {}
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al scanear el boleto.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);
                }
            }
        });

        btn_buscarItin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listView.getAdapter().getCount() != 0){
                    if(sharedPreferences.getString("Modulo","NoData").equals("ANDROID_INSP"))
                    {
                        InspectorRuta();
                    }else{
                        InspectorTrafico();
                    }
                }else {
                    Toast.makeText(getActivity(), "No se ha buscado un itinerario.", Toast.LENGTH_LONG).show();
                }


                /* Cambia a la vista de buscar boleto
                EmbarqueBuscarBus embarqueBuscarBus  = new EmbarqueBuscarBus();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, embarqueBuscarBus).commit();
                /* ----------------------------------------- */

            }
        });
        btn_buscarItin_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Cambia a la vista de buscar boleto */
                BuscaTcdocu_tota BuscaBoletoCliente = new BuscaTcdocu_tota();
                //BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, BuscaBoletoCliente).commit();
                /* ----------------------------------------- */

            }
        });

        btn_CambioBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                ItinerarioInspectorRuta ItinerarioInspector = new ItinerarioInspectorRuta();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, ItinerarioInspector).commit();
            }
        });

      /*  btn_BuscarBoleto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, buscarBoletoFragment).commit();*/

                /*BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container,buscarBoletoFragment,"fragment");
                transaction.addToBackStack(null);
                transaction.commit();*/

            /*}
        });*/

    }

//    boolean ValidaTrama(String Trama,final View view)
//    {
//        final String ID_EVENTO= "";
//        final String[] dataCodigoQR = Trama.split("\\|");
//        boolean Respuesta =false;
//        Log.d("trama",Trama);
//        for (int i = 0; i < lista_boletosLeidos.size(); i++) {
//
//            if(lista_boletosLeidos.get(i).contains(completarCorrelativo(Integer.valueOf(dataCodigoQR[3]))) && lista_boletosLeidos.get(i).contains(dataCodigoQR[2])){
//               //existe = true;
//                Toast.makeText(getActivity(),"BOLETO YA FUE AGREGADO", Toast.LENGTH_LONG).show();
//                return false;
//            }
//        }
//
//        if(dataCodigoQR[19].equals("VIAJE"))
//        {
//            Toast toast = Toast.makeText(getContext(),"SOLO SE ASIGNA BOLETOS DE VIAJE AL MANIFIESTO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }
//
//        else if(!sharedPreferences.getString("anf_codigoEmpresa", "NoData").toString().equals(dataCodigoQR[10].toString().trim()))
//        {
//            Log.d("Empresa",dataCodigoQR[10].toString());
//            Log.d("Empresa_itin",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
//            Toast toast = Toast.makeText(getContext(),"BOLETO NO PERTENECE A LA EMPRESA/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else if(!sharedPreferences.getString("anf_rumbo", "NoData").toString().equals(dataCodigoQR[11].toString().trim()))
//        {
//            Toast toast = Toast.makeText(getContext(),"RUMBO NO CONCIDE SEGUN ITINERARIO/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else if(sharedPreferences.getString("anf_codigoEmpresa", "NoData").toString().equals(dataCodigoQR[10].toString().trim()) &&
//                sharedPreferences.getString("anf_rumbo", "NoData").toString().equals(dataCodigoQR[11].toString().trim()) &&
//                sharedPreferences.getString("anf_secuencia", "NoData").toString().equals(dataCodigoQR[16].toString().trim())){
//            Toast toast = Toast.makeText(getContext(),"BOLETO MATADO, PERTENECE AL ITINERARIO/VALIDAR ORIGEN Y DESTINO SEGUN UBICACIÓN", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//
//        }
//        /*else if(!dataCodigoQR[16].toString().equals("NoData"))
//        {
//            Toast toast = Toast.makeText(getContext(),"BOLETO NO ES VALIDO PARA EL VIAJE ACTUAL/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }*/
//        else{
//            final String CorrelativoCompleto = completarCorrelativo(Integer.valueOf(dataCodigoQR[3]));
//            final String NU_DOCU=dataCodigoQR[2] +"-"+CorrelativoCompleto;
//            final String CO_EMPR = dataCodigoQR[10];
//            final String TI_DOCU="BLT";
//            final String FE_DOCU=dataCodigoQR[6];
//            final String wsValidaDocumento = getString(R.string.ws_ruta) + "TMVALIDA_DOCU/" + TI_DOCU + "/" + NU_DOCU+"/"+CO_EMPR+"/"+FE_DOCU;
//            Log.d("ultimaVenta",wsValidaDocumento);
//
//            JsonArrayRequest RequestValidador = new JsonArrayRequest(Request.Method.GET, wsValidaDocumento, null,
//                    new Response.Listener<JSONArray>() {
//                        @Override
//                        public void onResponse(JSONArray response) {
//                            if (response.length()>0) {
//                                JSONObject json;
//                                try   {
//                                    json = response.getJSONObject(0);
//                                    final String Matado = json.getString("MATADO");
//                                    final String NU_SECU_MATADO = json.getString("NU_SECU");
//                                    final String FE_VIAJ_MATADO = json.getString("FE_VIAJ").substring(0,10);
//                                    final String CO_RUMB_MATADO = json.getString("CO_RUMB");
//                                    guardarDataMemoria("KillBol",Matado,getContext());
//                                    String RespuestaWS = sharedPreferences.getString("KillBol","NoData");
//                                    if(!RespuestaWS.equals("NoData"))
//                                    {
//                                        if(RespuestaWS.equals("NO"))
//                                        {
//                                            AgregaBoletoLista(CO_EMPR,dataCodigoQR[2],CorrelativoCompleto,FE_DOCU,dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],ID_EVENTO);
//                                        }else{
//                                            if (NU_SECU_MATADO.equals(sharedPreferences.getString("anf_secuencia", "NoData")) &&
//                                                    FE_VIAJ_MATADO.equals(sharedPreferences.getString("anf_fechaProgramacion", "NoData")) &&
//                                                    CO_RUMB_MATADO.equals(sharedPreferences.getString("anf_rumbo", "NoData"))) {
//
//                                                Toast toast = Toast.makeText(getContext(),"BOLETO YA FUE MATADO PERTENECE AL ITINERARIO", Toast.LENGTH_LONG);
//                                                toast.setGravity(Gravity.CENTER, 50, 50);
//                                                toast.show();
//
//                                            }
//                                            else {
//                                                Toast toast = Toast.makeText(getContext(), "BOLETO NO ES VALIDO PARA EL VIAJE ACTUAL/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//                                                toast.setGravity(Gravity.CENTER, 50, 50);
//                                                toast.show();
//                                            }
//                                        }
//
//                                    }
//                                } catch (JSONException e) {
//
//                                }
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    error.printStackTrace();
//                    //Toast.makeText(getApplicationContext(), "Error en la ws UltimaVenta.", Toast.LENGTH_LONG).show();
//                    Toast toast = Toast.makeText(getContext(),"LECTURAR BOLETO CUANDO AYA COBERTURA / PARA AGREGAR A MANIFIESTO", Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.CENTER, 50, 50);
//                    toast.show();
//                }
//            }) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Map<String, String> headers = new HashMap<>();
//                    String Credencial = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                    String auth = "Basic " + Base64.encodeToString(Credencial.getBytes(), Base64.NO_WRAP);
//                    headers.put("Content-Type", "application/json");
//                    headers.put("Authorization", auth);
//                    return headers;
//                }
//            };
//            RequestValidador.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//            queue.add(RequestValidador);
//        }
//        return Respuesta;
//    }
    void AgregaBoletoLista(String codEmpresa,String Serie,String Correlativo,String FE_DOCU,String NombreCliente,String Origen,String Destino,String DOCU_CLIE,View view, String NU_ASIE,String IM_TOTA,String ID_EVENTO)
    {
        if(lista_boletosLeidos.size() == 0){

            lista_boletosLeidos.add("01"+"ƒ"+Serie+"ƒ"+Correlativo+"ƒ"+"BLT"+"ƒ"+FE_DOCU+"ƒ"+NombreCliente+"ƒ"+Origen+"ƒ"+Destino+"ƒ"+DOCU_CLIE+"ƒ"+NU_ASIE+"ƒ"+IM_TOTA+"ƒ"+ID_EVENTO);
            guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
            //Log.d("tamano", Integer.toString(lista_boletosLeidos.size()));

            /* Los boletos leídos se muestran en la tabla */
            //listView = view.findViewById(R.id.listView_boletosLeidos);
            TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getContext());
            listView.setAdapter(adapterBoletosLeidos);
            /* ----------------------------------------- */

            btn_buscarItin.setEnabled(true);


        }else{


            for (int i = 0; i < lista_boletosLeidos.size(); i++) {

                if(lista_boletosLeidos.get(i).contains(Correlativo) && lista_boletosLeidos.get(i).contains(Serie)){

                    existe = true;
                    Toast.makeText(getActivity(),"BOLETO YA FUE AGREGADO", Toast.LENGTH_LONG).show();
                    break;

                }
            }

            if(!existe){
                //lista_boletosLeidos.add(codEmpresa+"."+Serie+"."+Correlativo+"."+"BLT"+"."+FE_DOCU+"."+NombreCliente+"."+Origen+"."+Destino+"."+DOCU_CLIE);
                lista_boletosLeidos.add("01"+"ƒ"+Serie+"ƒ"+Correlativo+"ƒ"+"BLT"+"ƒ"+FE_DOCU+"ƒ"+NombreCliente+"ƒ"+Origen+"ƒ"+Destino+"ƒ"+DOCU_CLIE+"ƒ"+NU_ASIE+"ƒ"+IM_TOTA+"ƒ"+ID_EVENTO);
                guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
                Log.d("tamano", Integer.toString(lista_boletosLeidos.size()));
            }

        }

        /* Los boletos leídos se muestran en la tabla */
        //listView = view.findViewById(R.id.listView_boletosLeidos);

        TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
        listView.setAdapter(adapterBoletosLeidos);
        /* ----------------------------------------- */
        btn_buscarItin.setEnabled(true);
        Log.d("tamano", lista_boletosLeidos.toString());
    }
    public JSONObject generarJSON(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Gson gson = new Gson();
        final String Secuencia = sharedPreferences.getString("anf_secuencia","NoData");
        final String FechaItinerario = sharedPreferences.getString("anf_fechaProgramacion","NoData");
        final String hora_salida = sharedPreferences.getString("anf_horaSalida","NoData");
        Log.d("asignacion",lista_boletosLeidos.toString());
       // final ArrayList<String> lista_boletosLeidos = getArray(sharedPreferences, gson, "lista_boletosLeidos");
        JSONArray arrayBoletos =  new JSONArray();
        JSONObject embarque = new JSONObject();
        try {
            for(int i = 0; i<lista_boletosLeidos.size(); i++){

                //String[] boletoLeído = lista_boletosLeidos.get(i).split("\\.");
                String[] boletoLeído = lista_boletosLeidos.get(i).split("ƒ");

                if(boletoLeído[0].equals(sharedPreferences.getString("anf_codigoEmpresa", "NoData").toString())) {
                    JSONObject boleto = new JSONObject();
                    boleto.put("Empresa", boletoLeído[0]);
                    boleto.put("TipoDocumento", boletoLeído[3]);
                    boleto.put("NumeroDocumento", boletoLeído[1] + "-" + boletoLeído[2]);
                    boleto.put("SecuenciaItin", Secuencia);
                    boleto.put("FechaViajeItin", FechaItinerario);
                    boleto.put("HoraViajeItin", hora_salida);
                    arrayBoletos.put(boleto);
                    //ventaBlt = new DatabaseBoletos(getContext());
                    //sqLiteDatabase = ventaBlt.getWritableDatabase();
                    if (sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_CONTROL")||
                            sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_INSP")) {
                        try   {
                            ContentValues sqlQuery = new ContentValues();
                            sqlQuery.put("CO_EMPR", boletoLeído[0].toString());
                            sqlQuery.put("TI_DOCU", boletoLeído[3].toString());
                            sqlQuery.put("NU_DOCU", boletoLeído[1].toString()+"-"+boletoLeído[2].toString());
                            sqlQuery.put("NU_SECU", Secuencia.toString());
                            sqlQuery.put("FE_VIAJ",FechaItinerario.toString());
                            sqlQuery.put("HO_VIAJ",hora_salida.toString());
                            sqlQuery.put("CO_VEHI",sharedPreferences.getString("co_vehi_asig","NoData"));
                            sqLiteDatabase.insert("Asignacion",null,sqlQuery);
                        } catch (Exception e) {
                            String error = e.getMessage();
                        }

                    } else {

                    try {
                        ContentValues sqlQuery = new ContentValues();
                        sqlQuery.put("CO_EMPR", boletoLeído[0].toString());
                        sqlQuery.put("TI_DOCU", boletoLeído[3].toString());
                        sqlQuery.put("NU_DOCU", boletoLeído[1].toString() + "-" + String.valueOf(Integer.valueOf(boletoLeído[2].toString())));
                        sqlQuery.put("NU_SECU", Secuencia.toString());
                        sqlQuery.put("FE_VIAJ", FechaItinerario.toString());
                        sqlQuery.put("HO_VIAJ", hora_salida.toString());
                        sqlQuery.put("CO_VEHI", sharedPreferences.getString("anf_codigoVehiculo", "NoData"));
                        sqlQuery.put("NO_CLIE", boletoLeído[5]);
                        sqlQuery.put("CO_DEST_ORIG", boletoLeído[6]);
                        sqlQuery.put("CO_DEST_FINA", boletoLeído[7]);
                        sqlQuery.put("DOCU_IDEN", boletoLeído[8]);
                        sqlQuery.put("NU_ASIE", boletoLeído[9]);
                        sqlQuery.put("IM_TOTA", boletoLeído[10]);
                        sqlQuery.put("TIPO", "2");
                        sqLiteDatabase.insert("Manifiesto", null, sqlQuery);

                    } catch (Exception e) {
                        String error = e.getMessage();

                    }
                }
                }else {

                    Toast.makeText(getActivity(), "La empresa del boleto leído "+boletoLeído[1]+"-"+boletoLeído[2]+" es diferente al de itinerario.", Toast.LENGTH_LONG).show();
                }

            }

            //arrayBoletos.put(boleto);
            embarque.put("Embarque",arrayBoletos);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return embarque;
    }
    void AgregaBoletos(View view)
    {
        String DataArreglo = sharedPreferences.getString("lista_boletosLeidos","NoData");
        final TextView txtCant = view.findViewById(R.id.txtCant);
        if (DataArreglo.equals("NoData")==false)
        {
            String Arr =  DataArreglo.substring(1,(DataArreglo.length())-1);
            ArrayList<String> Data = new ArrayList<>();
            List<String> items = Arrays.asList(Arr.split("\\,"));

            final int size = items.size();
            for (int i = 0; i < size; i++)
            {
                //object = items.get(i);
                lista_boletosLeidos.add(items.get(i));
                //do something with i
            }
            btn_buscarItin.setEnabled(true);
            guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
            listView = view.findViewById(R.id.listView_boletosLeidos);
            TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
            listView.setAdapter(adapterBoletosLeidos);

            txtCant.setText("  "+size);

            //List<String> list = new ArrayList<String>(Arrays.asList(string.split(" , ")));
        }


    }

//    boolean ValidaTramaControlador(String Trama,final View view)
//    {
//        final String ID_EVENTO="";
//        final String[] dataCodigoQR = Trama.split("\\|");
//        boolean Respuesta =false;
//        Log.d("trama",Trama);
//        for (int i = 0; i < lista_boletosLeidos.size(); i++) {
//
//            if(lista_boletosLeidos.get(i).contains(completarCorrelativo(Integer.valueOf(dataCodigoQR[3]))) && lista_boletosLeidos.get(i).contains(dataCodigoQR[2])){
//                //existe = true;
//                Toast.makeText(getActivity(),"BOLETO YA FUE AGREGADO", Toast.LENGTH_LONG).show();
//                return false;
//            }
//        }
//
//        if(!dataCodigoQR[19].equals("VIAJE"))
//        {
//            Toast toast = Toast.makeText(getContext(),"SOLO SE ASIGNA BOLETOS DE VIAJE AL MANIFIESTO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else if(!sharedPreferences.getString("anf_codigoEmpresa", "NoData").toString().equals(dataCodigoQR[10].toString().trim()))
//        {
//            Log.d("Empresa",dataCodigoQR[10].toString());
//            Log.d("Empresa_itin",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
//            Toast toast = Toast.makeText(getContext(),"BOLETO NO PERTENECE A LA EMPRESA/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else if(!sharedPreferences.getString("anf_rumbo", "NoData").toString().equals(dataCodigoQR[11].toString().trim()))
//        {
//            Toast toast = Toast.makeText(getContext(),"RUMBO NO CONCIDE SEGUN ITINERARIO/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else if(sharedPreferences.getString("anf_codigoEmpresa", "NoData").toString().equals(dataCodigoQR[10].toString().trim()) &&
//                sharedPreferences.getString("anf_rumbo", "NoData").toString().equals(dataCodigoQR[11].toString().trim()) &&
//                sharedPreferences.getString("anf_secuencia", "NoData").toString().equals(dataCodigoQR[16].toString().trim())){
//            Toast toast = Toast.makeText(getContext(),"BOLETO MATADO, PERTENECE AL ITINERARIO/VALIDAR ORIGEN Y DESTINO SEGUN UBICACIÓN", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//
//        }else if(!dataCodigoQR[16].toString().equals("NoData"))
//        {
//            Toast toast = Toast.makeText(getContext(),"BOLETO NO ES VALIDO PARA EL VIAJE ACTUAL/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else{
//            final String CorrelativoCompleto = completarCorrelativo(Integer.valueOf(dataCodigoQR[3]));
//            final String NU_DOCU=dataCodigoQR[2] +"-"+CorrelativoCompleto;
//            final String CO_EMPR = dataCodigoQR[10];
//            final String TI_DOCU="BLT";
//            final String FE_DOCU=dataCodigoQR[6];
//            final String wsValidaDocumento = getString(R.string.ws_ruta) + "TMVALIDA_DOCU/" + TI_DOCU + "/" + NU_DOCU+"/"+CO_EMPR+"/"+FE_DOCU;
//            Log.d("ultimaVenta",wsValidaDocumento);
//
//            JsonArrayRequest RequestValidador = new JsonArrayRequest(Request.Method.GET, wsValidaDocumento, null,
//                    new Response.Listener<JSONArray>() {
//                        @Override
//                        public void onResponse(JSONArray response) {
//                            if (response.length()>0) {
//                                JSONObject json;
//                                try   {
//                                    json = response.getJSONObject(0);
//                                    final String Matado = json.getString("MATADO");
//                                    final String NU_SECU_MATADO = json.getString("NU_SECU");
//                                    final String FE_VIAJ_MATADO = json.getString("FE_VIAJ").substring(0,10);
//                                    final String CO_RUMB_MATADO = json.getString("CO_RUMB");
//                                    guardarDataMemoria("KillBol",Matado,getContext());
//                                    String RespuestaWS = sharedPreferences.getString("KillBol","NoData");
//                                    if(!RespuestaWS.equals("NoData"))
//                                    {
//                                        if(RespuestaWS.equals("NO"))
//                                        {
//                                            AgregaBoletoLista(CO_EMPR,dataCodigoQR[2],CorrelativoCompleto,FE_DOCU,dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],ID_EVENTO);
//                                        }else{
//                                            if (NU_SECU_MATADO.equals(sharedPreferences.getString("anf_secuencia", "NoData")) &&
//                                                    FE_VIAJ_MATADO.equals(sharedPreferences.getString("anf_fechaProgramacion", "NoData")) &&
//                                                    CO_RUMB_MATADO.equals(sharedPreferences.getString("anf_rumbo", "NoData"))) {
//
//                                                Toast toast = Toast.makeText(getContext(),"BOLETO YA FUE MATADO PERTENECE AL ITINERARIO", Toast.LENGTH_LONG);
//                                                toast.setGravity(Gravity.CENTER, 50, 50);
//                                                toast.show();
//
//                                            }
//                                            else {
//                                                Toast toast = Toast.makeText(getContext(), "BOLETO NO ES VALIDO PARA EL VIAJE ACTUAL/REALIZAR VENTA DE BOLETO", Toast.LENGTH_LONG);
//                                                toast.setGravity(Gravity.CENTER, 50, 50);
//                                                toast.show();
//                                            }
//                                        }
//
//                                    }
//                                } catch (JSONException e) {
//
//                                }
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    error.printStackTrace();
//                    //Toast.makeText(getApplicationContext(), "Error en la ws UltimaVenta.", Toast.LENGTH_LONG).show();
//                    Toast toast = Toast.makeText(getContext(),"LECTURAR BOLETO CUANDO AYA COBERTURA / PARA AGREGAR A MANIFIESTO", Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.CENTER, 50, 50);
//                    toast.show();
//                }
//            }) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Map<String, String> headers = new HashMap<>();
//                    String Credencial = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
//                    String auth = "Basic " + Base64.encodeToString(Credencial.getBytes(), Base64.NO_WRAP);
//                    headers.put("Content-Type", "application/json");
//                    headers.put("Authorization", auth);
//                    return headers;
//                }
//            };
//            RequestValidador.setRetryPolicy(new DefaultRetryPolicy(20 * 5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//            queue.add(RequestValidador);
//        }
//        return Respuesta;
//    }

    boolean ValidaTramaInspectorRuta(String Trama,final View view)
    {

        final String[] dataCodigoQR = Trama.split("\\|");
        Log.d("TramaQR",dataCodigoQR.toString());
        String Evento="";
        boolean Respuesta =false;
        Log.d("trama",Trama);

        for (int i = 0; i < lista_boletosLeidos.size(); i++) {
            if(lista_boletosLeidos.get(i).contains(completarCorrelativo(Integer.valueOf(dataCodigoQR[3]))) && lista_boletosLeidos.get(i).contains(dataCodigoQR[2])){
                //existe = true;
                Toast.makeText(getActivity(),"EL BOLETO "+dataCodigoQR[2]+"-"+dataCodigoQR[3]+" YA FUE AGREGADO", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
//        if(!dataCodigoQR[19].equals("VIAJE"))
//        {
//            Toast toast = Toast.makeText(getContext(),"SOLO SE INSPECCIONA DOCUMENTOS DE VIAJE", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }
//        else
//        if(!sharedPreferences.getString("CO_EMPR_INSP", "NoData").toString().equals(dataCodigoQR[10].toString().trim()))
//        {
//            AgregaBoletoLista(dataCodigoQR[10],dataCodigoQR[2],completarCorrelativo(Integer.valueOf(dataCodigoQR[3])),dataCodigoQR[6],dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],"1");
//            Log.d("Empresa",dataCodigoQR[10].toString());
//            Log.d("Empresa_itin",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
//            Toast toast = Toast.makeText(getContext(),"BOLETO NO PERTENECE A LA EMPRESA", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER, 50, 50);
//            toast.show();
//            Respuesta=false;
//            return false;
//        }else
        if(!sharedPreferences.getString("CO_RUMB_INSP", "NoData").toString().equals(dataCodigoQR[11].toString().trim()))
        {
            AgregaBoletoLista(dataCodigoQR[10],dataCodigoQR[2],completarCorrelativo(Integer.valueOf(dataCodigoQR[3])),dataCodigoQR[6],dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],"2");
            Toast toast = Toast.makeText(getContext(),"RUMBO NO CONCIDE SEGUN ITINERARIO", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 50, 50);
            toast.show();
            Respuesta=false;
            return false;
        }else if(dataCodigoQR[19].equals("VIAJE"))
        {
            Toast toast = Toast.makeText(getContext(),"EL BOLETO DE VIAJE "+dataCodigoQR[2]+"-"+dataCodigoQR[3]+" FUE INSPECCIONADO CORRECTAMENTE", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 50, 50);
            toast.show();
            AgregaBoletoLista(dataCodigoQR[10],dataCodigoQR[2],completarCorrelativo(Integer.valueOf(dataCodigoQR[3])),dataCodigoQR[6],dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],"3");
            Respuesta=true;
            return true;
        }else if(!dataCodigoQR[19].equals("VIAJE"))
        {
            Toast toast = Toast.makeText(getContext(),"EL BOLETO DE CARGA "+dataCodigoQR[2]+"-"+dataCodigoQR[3]+" FUE INSPECCIONADO CORRECTAMENTE", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 50, 50);
            toast.show();
            AgregaBoletoLista(dataCodigoQR[10],dataCodigoQR[2],completarCorrelativo(Integer.valueOf(dataCodigoQR[3])),dataCodigoQR[6],dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],"3");
            Respuesta=true;
            return true;
        }
//        else if(dataCodigoQR[2].length()!=3){
//            Toast toast = Toast.makeText(getContext(),"EL QR ESTA DAÑADO O ES INCORRECTO, FAVOR DE INGRESAR EL BOLETO MANUALMENTE", Toast.LENGTH_SHORT);
//            Respuesta=true;
//            return true;
//        }

        return Respuesta;
    }
    boolean ValidaDestinos(final String CO_DEST_FINA)
    {
        for(int i =0 ; i< ValidaDestinoViaje.size();i++)
        {
            String[] ValidaDestinos =ValidaDestinoViaje.get(i).split("ƒ");
            if(ValidaDestinos[1].equals(CO_DEST_FINA))
            {
                return true;
            }
        }
        return  false;
    }

    void InspectorTrafico()
    {
        final RequestQueue queue = Volley.newRequestQueue(getActivity());

        final JSONObject jsonObject = generarJSON();
        Log.d("json", jsonObject.toString());
        final String ws_getEmbarque = getString(R.string.ws_ruta) + "Embarque";
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Espere");
        progressDialog.setMessage("Asignando");
        progressDialog.setCancelable(false);
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                MyJSONArrayRequest jsonArrayRequestEmbarque = new MyJSONArrayRequest(Request.Method.PUT, ws_getEmbarque, jsonObject,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.length() > 0) {

                                    JSONObject info;
                                    try {

                                        info = response.getJSONObject(0);
                                        String respuesta = info.getString("Respuesta");
                                        Log.d("respuesta", respuesta);

                                        SharedPreferences.Editor Elimina = sharedPreferences.edit();
                                        Elimina.remove("lista_boletosLeidos");
                                        Elimina.commit();
                                        progressDialog.dismiss();
                                        /* Cambia a la vista de buscar boleto */
                                        if(sharedPreferences.getString("Modulo","NoData").equals("ANDROID_CONTROL")||
                                                sharedPreferences.getString("Modulo","NoData").equals("ANDROID_INSP"))
                                        {
                                            ItinerarioFragment embarqueLeerBoletos = new ItinerarioFragment();
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_base, embarqueLeerBoletos).commit();

                                        }else {

                                            VentaBoletosFragment embarqueLeerBoletos = new VentaBoletosFragment();
                                            FragmentManager fragmentManager = getFragmentManager();
                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                            fragmentTransaction.replace(R.id.fragment_base, embarqueLeerBoletos).commit();
                                        }
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
                        Toast.makeText(getActivity(), "Error en la ws Embarque. No se pudo realizar el embarque.", Toast.LENGTH_LONG).show();
                        //errorWS(queue, error);
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
                jsonArrayRequestEmbarque.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonArrayRequestEmbarque);
            }
        });
        hilo.start();
        progressDialog.show();
    }
    void InspectorRuta(){
        try{
            final RequestQueue queue = Volley.newRequestQueue(getContext());
            final Gson gson = new Gson();
            final JSONObject jsonObject = TCDOCU_INSP_RUTA();
            String ws_InspectorRuta = getString(R.string.ws_ruta) + "TCDOCU_INSP_RTA";
            Log.d("respuesta",ws_InspectorRuta);
            Log.d("dataWS_insp",jsonObject.toString());
            GuardarDataSQLlite(jsonObject);
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Espere...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            MyJSONArrayRequest JsonRequestInps = new MyJSONArrayRequest(Request.Method.POST, ws_InspectorRuta, jsonObject,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d("request","true");
                            guardarDataMemoria("flag_insp_bol","InfinityDev",getActivity());
                            String flagInspeccionVenta = "false";
                            guardarDataMemoria("flagInspeccionVenta",flagInspeccionVenta,getContext());
                            guardarDataMemoria("lista_boletosLeidos","NoData",getActivity());
                            progressDialog.dismiss();
                            getActivity().finish();
                            startActivity(getActivity().getIntent());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //error.printStackTrace();
                    Log.d("request_1","true");
                    guardarDataMemoria("lista_boletosLeidos","NoData",getActivity());
                    guardarDataMemoria("flag_insp_bol","InfinityDev",getActivity());
                    String flagInspeccionVenta = "false";
                    guardarDataMemoria("flagInspeccionVenta",flagInspeccionVenta,getContext());
                    progressDialog.dismiss();
                    getActivity().finish();
                    startActivity(getActivity().getIntent());
                    Toast.makeText(getActivity(), "Se activa modo Offline.", Toast.LENGTH_LONG).show();
                }
            }){
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
            JsonRequestInps.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(JsonRequestInps);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
    JSONObject TCDOCU_INSP_RUTA()
    {
        final JSONObject InspeccionDocumentoRuta = new JSONObject();
        JSONObject CABECERA_INSP = new JSONObject();
        JSONArray DETALLE_INSP =  new JSONArray();
        Date date = new Date();
        final Gson gson = new Gson();
        final String fechaInspeccion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        try{
            CABECERA_INSP.put("FE_INSP_INI",sharedPreferences.getString("fechaInspeccion","NoData"));
            CABECERA_INSP.put("FE_INSP_FIN",fechaInspeccion);
            CABECERA_INSP.put("CO_TRAM",sharedPreferences.getString("CO_TRAM_CTRL","NoData"));
            CABECERA_INSP.put("CO_TRAB",sharedPreferences.getString("CodUsuario","NoData"));
            CABECERA_INSP.put("CO_RUMB",sharedPreferences.getString("CO_RUMB_INSP","NoData"));
            CABECERA_INSP.put("FE_VIAJ",sharedPreferences.getString("FE_PROG_INSP","NoData"));
            CABECERA_INSP.put("NU_SECU_VIAJ",sharedPreferences.getString("NU_SECU_INSP","NoData"));
            CABECERA_INSP.put("CO_EMPR",sharedPreferences.getString("CO_EMPR_INSP","NoData"));

            //ArrayList<String> TDDOCU_INSP_RTA = getArray(sharedPreferences,gson,"lista_boletosLeidos");
            String DataArreglo = sharedPreferences.getString("lista_boletosLeidos","NoData");
            String Arr =  DataArreglo.substring(1,(DataArreglo.length())-1);
            //ArrayList<String> Data = new ArrayList<>();
            List<String> TDDOCU_INSP_RTA = Arrays.asList(Arr.split("\\,"));

            Log.d("BoletosAsigna",TDDOCU_INSP_RTA.toString());
            for (int i = 0;i<TDDOCU_INSP_RTA.size();i++){
                String[] NU_DOCU_INSP =TDDOCU_INSP_RTA.get(i).split("ƒ");
                JSONObject detalle_insp = new JSONObject();
                detalle_insp.put("ID_EVENT",NU_DOCU_INSP[11]);
                detalle_insp.put("CO_EMPR",NU_DOCU_INSP[0]);
                detalle_insp.put("TI_DOCU",NU_DOCU_INSP[3]);
                detalle_insp.put("NU_DOCU",NU_DOCU_INSP[1]+"-"+NU_DOCU_INSP[2]);
                DETALLE_INSP.put(detalle_insp);
            }

            InspeccionDocumentoRuta.put("TCDOCU_INSP_RTA",CABECERA_INSP);
            InspeccionDocumentoRuta.put("TDDOCU_INSP_RTA",DETALLE_INSP);
            return InspeccionDocumentoRuta;
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        Log.d("dataWS",InspeccionDocumentoRuta.toString());
        return InspeccionDocumentoRuta;
    }
    void GuardarDataSQLlite(JSONObject jsonObject)
    {
        try{
            Date date = new Date();
            ContentValues cv = new ContentValues();
            cv.put("data_boleto", jsonObject.toString());
            cv.put("estado", "pendiente");
            cv.put("tipo", "inspeccion");
            cv.put("liberado", "");
            cv.put("nu_docu","");
            cv.put("ti_docu","");
            cv.put("co_empr",sharedPreferences.getString("guardar_idEmpresa", "NoData"));
            cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));
            cv.put("puesto", "InspectorRuta");
            sqLiteDatabase.insert("VentaBoletos", null, cv);

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
