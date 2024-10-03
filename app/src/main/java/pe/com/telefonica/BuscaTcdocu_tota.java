package pe.com.telefonica.soyuz;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;

public class BuscaTcdocu_tota extends Fragment {
    ListView listView;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    private Boolean existe = false;
    final ArrayList<String> lista_boletosLeidos = new ArrayList<>();

//    Spinner sp_serie;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        gson = new Gson();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.activity_busca_tcdocu_tota,parent,false );
    }
    @Override
    public void onViewCreated(View view ,Bundle  savedInstanceState)
    {
        final Button btn_escan = view.findViewById(R.id.btn_escan);
        final Button btn_budocu = view.findViewById(R.id.btn_budocu);
        final Button btn_anadir = view.findViewById(R.id.btn_anadir);
        final Button btn_cancelar = view.findViewById(R.id.btn_cancelar);
        final EditText etxt_NuSeri = view.findViewById(R.id.etxt_NuSeri);
        final EditText etxt_NuCorr = view.findViewById(R.id.etxt_NuCorr);
        final EditText etxt_CoClie = view.findViewById(R.id.etxt_CoClie);
        final TextView txt_NuDocu = view.findViewById(R.id.txt_NuDocu);
        final TextView txt_CoClie = view.findViewById(R.id.txt_CoClie);
        final TextView txt_NoClie = view.findViewById(R.id.txt_NoClie);
        final TextView txt_NuDnis = view.findViewById(R.id.txt_NuDnis);
        final TextView txt_NoPasa = view.findViewById(R.id.txt_NoPasa);
        final TextView txt_CoVehi = view.findViewById(R.id.txt_CoVehi);
        final TextView txt_CoTipo = view.findViewById(R.id.txt_CoTipo);
        final TextView txt_CoEsta = view.findViewById(R.id.txt_CoEsta);
        final TextView txt_FeEmis = view.findViewById(R.id.txt_FeEmis);
        final TextView txt_HoEmis = view.findViewById(R.id.txt_HoEmis);
        final TextView txt_DeAgen = view.findViewById(R.id.txt_DeAgen);
        final TextView txt_CoOrig = view.findViewById(R.id.txt_CoOrig);
        final TextView txt_DeOrig = view.findViewById(R.id.txt_DeOrig);
        final TextView txt_CoDest = view.findViewById(R.id.txt_CoDest);
        final TextView txt_DeDest = view.findViewById(R.id.txt_DeDest);
        final TextView txt_FeViaj = view.findViewById(R.id.txt_FeViaj);
        final TextView txt_HoViaj = view.findViewById(R.id.txt_HoViaj);
        final TextView txt_CoRumb = view.findViewById(R.id.txt_CoRumb);
        final TextView txt_NuSecu = view.findViewById(R.id.txt_NuSecu);
        final TextView txt_ImTota = view.findViewById(R.id.txt_ImTota);
        final TextView txt_NuAsie = view.findViewById(R.id.txt_NuAsie);
        final TextView txt_CoTrab = view.findViewById(R.id.txt_CoTrab);
//        sp_serie = view.findViewById(R.id.spin_serie);
        AgregaBoletos(view);


//        GET_SERIE();

//        sp_serie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Object item = parent.getItemAtPosition(position);
//                etxt_NuSeri.setText(item.toString());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        btn_escan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try{
                    final IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
                    IScanner iScanner = dal.getScanner(EScannerType.REAR);
                    if (iScanner.open()) {
                        iScanner.start(new IScanner.IScanListener() {
                            @Override
                            public void onRead(String codigoQR) {
                                final String[] dataCodigoQR = codigoQR.split("\\|");
                                Log.d("TramaQR",dataCodigoQR.toString());
                                String Evento="";
                                Log.d("trama",codigoQR);
                                if(codigoQR.length()<=8){
                                    Toast.makeText(getActivity(),"BUSCANDO DNI "+dataCodigoQR[0], Toast.LENGTH_LONG).show();
                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                                    final String CodigoCliente = dataCodigoQR[0];
                                    final String ws_buscarTCDOCU_TOTA2 = getString(R.string.ws_ruta) + "BusBoletoTota2/"+CodigoCliente;
                                    progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setTitle("Espere");
                                    progressDialog.setMessage("Buscando Documento");
                                    progressDialog.setCancelable(false);
                                    MyJSONArrayRequest RequestBuscaBol2 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA2, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length()>0)
                                                    {
                                                        JSONObject json;
                                                        try{
                                                            json = response.getJSONObject(0);
                                                            final String NuDocu = json.getString("NU_DOCU");
                                                            final String CoClie = json.getString("CO_CLIE");
                                                            final String NoClie = json.getString("NO_CLIE");
                                                            final String NuDnis = json.getString("NU_DNIS");
                                                            final String NoPasa = json.getString("NO_PASA");
                                                            final String CoVehi = json.getString("BUS");
                                                            final String CoTipo = json.getString("TIPO");
                                                            final String CoEsta = json.getString("ESTADO");
                                                            final String FeEmis = json.getString("D_EMISION");
                                                            final String HoEmis = json.getString("H_EMISION");
                                                            final String DeAgen = json.getString("DE_AGEN");
                                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                                            final String DeOrig = json.getString("ORIGEN");
                                                            final String CoDest = json.getString("CO_DEST_FINA");
                                                            final String DeDest = json.getString("DESTINO");
                                                            final String FeViaj = json.getString("FE_VIAJ");
                                                            final String HoViaj = json.getString("HO_VIAJ");
                                                            final String NuSecu = json.getString("NU_SECU");
                                                            final String CoRumb = json.getString("CO_RUMB");
                                                            final String ImTota = json.getString("IM_TOTA");
                                                            final String NuAsie = json.getString("NU_ASIE");
                                                            final String CoTrab = json.getString("VENDEDOR");

                                                            txt_NuDocu.setText(NuDocu);
                                                            txt_CoClie.setText(CoClie);
                                                            txt_NoClie.setText(NoClie);
                                                            txt_NuDnis.setText(NuDnis);
                                                            txt_NoPasa.setText(NoPasa);
                                                            txt_CoVehi.setText(CoVehi);
                                                            txt_CoTipo.setText(CoTipo);
                                                            txt_CoEsta.setText(CoEsta);
                                                            txt_FeEmis.setText(FeEmis);
                                                            txt_HoEmis.setText(HoEmis);
                                                            txt_DeAgen.setText(DeAgen);
                                                            txt_CoOrig.setText(CoOrig);
                                                            txt_DeOrig.setText(DeOrig);
                                                            txt_CoDest.setText(CoDest);
                                                            txt_DeDest.setText(DeDest);
                                                            txt_FeViaj.setText(FeViaj);
                                                            txt_HoViaj.setText(HoViaj);
                                                            txt_CoRumb.setText(CoRumb);
                                                            txt_NuSecu.setText(NuSecu);
                                                            txt_ImTota.setText(ImTota);
                                                            txt_NuAsie.setText(NuAsie);
                                                            txt_CoTrab.setText(CoTrab);

                                                        }catch (Exception e)
                                                        {
                                                            Log.d("error",e.getMessage());
                                                        }
                                                    }
                                                    Log.d("data",response.toString());
                                                    progressDialog.dismiss();
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

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
                                    RequestBuscaBol2.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(RequestBuscaBol2);
                                }
                                else if(dataCodigoQR[2].length()==4 && dataCodigoQR[3].length()==8){
                                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                                    final String CorrelativoCompleto = dataCodigoQR[2]+"-00"+dataCodigoQR[3];
                                    Toast.makeText(getActivity(),"BUSCANDO BOLETO "+CorrelativoCompleto, Toast.LENGTH_LONG).show();
                                    final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota1/"+CorrelativoCompleto;
                                    progressDialog = new ProgressDialog(getActivity());
                                    progressDialog.setTitle("Espere");
                                    progressDialog.setMessage("Buscando Documento");
                                    progressDialog.setCancelable(false);
                                    MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    if (response.length()>0)
                                                    {
                                                        JSONObject json;
                                                        try{
                                                            json = response.getJSONObject(0);
                                                            final String NuDocu = json.getString("NU_DOCU");
                                                            final String CoClie = json.getString("CO_CLIE");
                                                            final String NoClie = json.getString("NO_CLIE");
                                                            final String NuDnis = json.getString("NU_DNIS");
                                                            final String NoPasa = json.getString("NO_PASA");
                                                            final String CoVehi = json.getString("BUS");
                                                            final String CoTipo = json.getString("TIPO");
                                                            final String CoEsta = json.getString("ESTADO");
                                                            final String FeEmis = json.getString("D_EMISION");
                                                            final String HoEmis = json.getString("H_EMISION");
                                                            final String DeAgen = json.getString("DE_AGEN");
                                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                                            final String DeOrig = json.getString("ORIGEN");
                                                            final String CoDest = json.getString("CO_DEST_FINA");
                                                            final String DeDest = json.getString("DESTINO");
                                                            final String FeViaj = json.getString("FE_VIAJ");
                                                            final String HoViaj = json.getString("HO_VIAJ");
                                                            final String NuSecu = json.getString("NU_SECU");
                                                            final String CoRumb = json.getString("CO_RUMB");
                                                            final String ImTota = json.getString("IM_TOTA");
                                                            final String NuAsie = json.getString("NU_ASIE");
                                                            final String CoTrab = json.getString("VENDEDOR");

                                                            txt_NuDocu.setText(NuDocu);
                                                            txt_CoClie.setText(CoClie);
                                                            txt_NoClie.setText(NoClie);
                                                            txt_NuDnis.setText(NuDnis);
                                                            txt_NoPasa.setText(NoPasa);
                                                            txt_CoVehi.setText(CoVehi);
                                                            txt_CoTipo.setText(CoTipo);
                                                            txt_CoEsta.setText(CoEsta);
                                                            txt_FeEmis.setText(FeEmis);
                                                            txt_HoEmis.setText(HoEmis);
                                                            txt_DeAgen.setText(DeAgen);
                                                            txt_CoOrig.setText(CoOrig);
                                                            txt_DeOrig.setText(DeOrig);
                                                            txt_CoDest.setText(CoDest);
                                                            txt_DeDest.setText(DeDest);
                                                            txt_FeViaj.setText(FeViaj);
                                                            txt_HoViaj.setText(HoViaj);
                                                            txt_CoRumb.setText(CoRumb);
                                                            txt_NuSecu.setText(NuSecu);
                                                            txt_ImTota.setText(ImTota);
                                                            txt_NuAsie.setText(NuAsie);
                                                            txt_CoTrab.setText(CoTrab);

                                                        }catch (Exception e)
                                                        {
                                                            Log.d("error",e.getMessage());
                                                        }
                                                    }
                                                    Log.d("data",response.toString());
                                                    progressDialog.dismiss();
                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

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
                                    RequestBuscaBol1.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                    queue.add(RequestBuscaBol1);
                                }

                            }
                            @Override
                            public void onFinish() {}
                            @Override
                            public void onCancel() {}
                        });
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al scanear el boleto.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);
                }
            }
        });

        btn_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                mataBoleto mataBoleto = new mataBoleto();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, mataBoleto).commit();
            }
        });

        btn_anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                mataBoleto maBoleto = new mataBoleto();
                final String ANuDocu = txt_NuDocu.getText().toString();
                final String ANuSeri = ANuDocu.substring(0,4);
                final String ANuCorr = ANuDocu.substring(5,15);
                final String AFeEmis = txt_FeEmis.getText().toString();
                final String ANoPasa = txt_NoPasa.getText().toString();
                final String ACoOrig = txt_CoOrig.getText().toString();
                final String ACoDest = txt_CoDest.getText().toString();
                final String ANuAsie = txt_NuAsie.getText().toString();
                final String AImTota = txt_ImTota.getText().toString();
                final String ACoClie = txt_CoClie.getText().toString();

                if(lista_boletosLeidos.size() == 0){

                }else{


                    for (int i = 0; i < lista_boletosLeidos.size(); i++) {

                        if(lista_boletosLeidos.get(i).contains(ANuCorr)){

                            existe = true;
                            Toast.makeText(getActivity(),"El boleto ya fue agregado", Toast.LENGTH_LONG).show();
                            break;

                        }
                    }

                    if(!existe){
                        lista_boletosLeidos.add("01"+"ƒ"+ANuSeri+"ƒ"+ANuCorr+"ƒ"+"BLT"+"ƒ"+AFeEmis+"ƒ"+ANoPasa+"ƒ"+ACoOrig+"ƒ"+ACoDest+"ƒ"+ANuDocu+"ƒ"+ANuAsie+"ƒ"+AImTota+"ƒ"+"3");
                        guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
                        Toast toast = Toast.makeText(getContext(),"BOLETO INSPECCIONADO", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 50, 50);
                        toast.show();
                    }

                }
                    mataBoleto mataBoleto = new mataBoleto();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, mataBoleto).commit();


            }
        });

        btn_budocu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (etxt_CoClie.getText().toString().equals("") ){
                    if (etxt_NuSeri.getText().toString().equals("") ){
                        Toast.makeText(getActivity(),"INGRESE UNA SERIE",Toast.LENGTH_LONG).show();
                        btn_budocu.setEnabled(true);
                        return;
                    }else if(etxt_NuCorr.getText().toString().equals("")){
                        Toast.makeText(getActivity(),"INGRESE CORRELATIVO",Toast.LENGTH_LONG).show();
                        btn_budocu.setEnabled(true);
                        return;
                    }
                    else if (etxt_NuSeri.getText().toString().length()>0 && etxt_NuCorr.getText().toString().length()>0){
                        final RequestQueue queue = Volley.newRequestQueue(getContext());
                        final String CompletaCeroCorr = completarCorrelativo(Integer.valueOf(etxt_NuCorr.getText().toString()));
                        final String CorrelativoCompleto = etxt_NuSeri.getText().toString()+"-"+CompletaCeroCorr;
                        final String ws_buscarTCDOCU_TOTA1 = getString(R.string.ws_ruta) + "BusBoletoTota1/"+CorrelativoCompleto;
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle("Espere");
                        progressDialog.setMessage("Buscando Documento");
                        progressDialog.setCancelable(false);
                        MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA1, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length()>0)
                                        {
                                            JSONObject json;
                                            try{
                                                json = response.getJSONObject(0);
                                                final String NuDocu = json.getString("NU_DOCU");
                                                final String CoClie = json.getString("CO_CLIE");
                                                final String NoClie = json.getString("NO_CLIE");
                                                final String NuDnis = json.getString("NU_DNIS");
                                                final String NoPasa = json.getString("NO_PASA");
                                                final String CoVehi = json.getString("BUS");
                                                final String CoTipo = json.getString("TIPO");
                                                final String CoEsta = json.getString("ESTADO");
                                                final String FeEmis = json.getString("D_EMISION");
                                                final String HoEmis = json.getString("H_EMISION");
                                                final String DeAgen = json.getString("DE_AGEN");
                                                final String CoOrig = json.getString("CO_DEST_ORIG");
                                                final String DeOrig = json.getString("ORIGEN");
                                                final String CoDest = json.getString("CO_DEST_FINA");
                                                final String DeDest = json.getString("DESTINO");
                                                final String FeViaj = json.getString("FE_VIAJ");
                                                final String HoViaj = json.getString("HO_VIAJ");
                                                final String NuSecu = json.getString("NU_SECU");
                                                final String CoRumb = json.getString("CO_RUMB");
                                                final String ImTota = json.getString("IM_TOTA");
                                                final String NuAsie = json.getString("NU_ASIE");
                                                final String CoTrab = json.getString("VENDEDOR");

                                                txt_NuDocu.setText(NuDocu);
                                                txt_CoClie.setText(CoClie);
                                                txt_NoClie.setText(NoClie);
                                                txt_NuDnis.setText(NuDnis);
                                                txt_NoPasa.setText(NoPasa);
                                                txt_CoVehi.setText(CoVehi);
                                                txt_CoTipo.setText(CoTipo);
                                                txt_CoEsta.setText(CoEsta);
                                                txt_FeEmis.setText(FeEmis);
                                                txt_HoEmis.setText(HoEmis);
                                                txt_DeAgen.setText(DeAgen);
                                                txt_CoOrig.setText(CoOrig);
                                                txt_DeOrig.setText(DeOrig);
                                                txt_CoDest.setText(CoDest);
                                                txt_DeDest.setText(DeDest);
                                                txt_FeViaj.setText(FeViaj);
                                                txt_HoViaj.setText(HoViaj);
                                                txt_CoRumb.setText(CoRumb);
                                                txt_NuSecu.setText(NuSecu);
                                                txt_ImTota.setText(ImTota);
                                                txt_NuAsie.setText(NuAsie);
                                                txt_CoTrab.setText(CoTrab);

                                            }catch (Exception e)
                                            {
                                                Log.d("error",e.getMessage());
                                            }
                                        }
                                        Log.d("data",response.toString());
                                        progressDialog.dismiss();
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

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
                            RequestBuscaBol1.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            queue.add(RequestBuscaBol1);
                    }

                }
                else if (etxt_CoClie.getText().toString().length()>0){
                    final RequestQueue queue = Volley.newRequestQueue(getContext());
                    final String CodigoCliente = etxt_CoClie.getText().toString();
                    Toast.makeText(getActivity(),"BUSCANDO CLIENTE "+CodigoCliente, Toast.LENGTH_LONG).show();
                    final String ws_buscarTCDOCU_TOTA2 = getString(R.string.ws_ruta) + "BusBoletoTota2/"+CodigoCliente;
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("Espere");
                    progressDialog.setMessage("Buscando Documento");
                    progressDialog.setCancelable(false);
                    MyJSONArrayRequest RequestBuscaBol2 = new MyJSONArrayRequest(Request.Method.GET, ws_buscarTCDOCU_TOTA2, null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if (response.length()>0)
                                    {
                                        JSONObject json;
                                        try{
                                            json = response.getJSONObject(0);
                                            final String NuDocu = json.getString("NU_DOCU");
                                            final String CoClie = json.getString("CO_CLIE");
                                            final String NoClie = json.getString("NO_CLIE");
                                            final String NuDnis = json.getString("NU_DNIS");
                                            final String NoPasa = json.getString("NO_PASA");
                                            final String CoVehi = json.getString("BUS");
                                            final String CoTipo = json.getString("TIPO");
                                            final String CoEsta = json.getString("ESTADO");
                                            final String FeEmis = json.getString("D_EMISION");
                                            final String HoEmis = json.getString("H_EMISION");
                                            final String DeAgen = json.getString("DE_AGEN");
                                            final String CoOrig = json.getString("CO_DEST_ORIG");
                                            final String DeOrig = json.getString("ORIGEN");
                                            final String CoDest = json.getString("CO_DEST_FINA");
                                            final String DeDest = json.getString("DESTINO");
                                            final String FeViaj = json.getString("FE_VIAJ");
                                            final String HoViaj = json.getString("HO_VIAJ");
                                            final String NuSecu = json.getString("NU_SECU");
                                            final String CoRumb = json.getString("CO_RUMB");
                                            final String ImTota = json.getString("IM_TOTA");
                                            final String NuAsie = json.getString("NU_ASIE");
                                            final String CoTrab = json.getString("VENDEDOR");

                                            txt_NuDocu.setText(NuDocu);
                                            txt_CoClie.setText(CoClie);
                                            txt_NoClie.setText(NoClie);
                                            txt_NuDnis.setText(NuDnis);
                                            txt_NoPasa.setText(NoPasa);
                                            txt_CoVehi.setText(CoVehi);
                                            txt_CoTipo.setText(CoTipo);
                                            txt_CoEsta.setText(CoEsta);
                                            txt_FeEmis.setText(FeEmis);
                                            txt_HoEmis.setText(HoEmis);
                                            txt_DeAgen.setText(DeAgen);
                                            txt_CoOrig.setText(CoOrig);
                                            txt_DeOrig.setText(DeOrig);
                                            txt_CoDest.setText(CoDest);
                                            txt_DeDest.setText(DeDest);
                                            txt_FeViaj.setText(FeViaj);
                                            txt_HoViaj.setText(HoViaj);
                                            txt_CoRumb.setText(CoRumb);
                                            txt_NuSecu.setText(NuSecu);
                                            txt_ImTota.setText(ImTota);
                                            txt_NuAsie.setText(NuAsie);
                                            txt_CoTrab.setText(CoTrab);

                                        }catch (Exception e)
                                        {
                                            Log.d("error",e.getMessage());
                                        }
                                    }
                                    Log.d("data",response.toString());
                                    progressDialog.dismiss();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

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
                    RequestBuscaBol2.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    queue.add(RequestBuscaBol2);

                }
            }
        });

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
            guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
            listView = view.findViewById(R.id.listView_boletosLeidos);
            TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
            listView.setAdapter(adapterBoletosLeidos);

            //List<String> list = new ArrayList<String>(Arrays.asList(string.split(" , ")));
        }


    }

//    void GET_SERIE(){
//        final RequestQueue queue = Volley.newRequestQueue(getContext());
//
////        final Spinner_model so = (Spinner_model)spin_serie.getSelectedItem();
//        String ws_seriebus = getString(R.string.ws_ruta) + "SERIEBUS/" + sharedPreferences.getString("CO_VEHI_INSP", "nada");
//        MyJSONArrayRequest RequestBuscaBol1 = new MyJSONArrayRequest(Request.Method.GET, ws_seriebus, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        if (response.length()>0)
//                        {
//                            final ArrayList<String> lista_series = new ArrayList<>();
//                            JSONObject json;
//                            try{
//                                for (int i = 0; i < response.length(); i++){
//                                    json = response.getJSONObject(i);
//                                    lista_series.add(json.getString("NU_SERI"));
//                                }
////                                String JSON_SERIE = gson.toJson(lista_series);
////                                guardarDataMemoria("jsonSeries", JSON_SERIE, getApplicationContext());
//                            }catch (Exception e)
//                            {
//                                Log.d("error",e.getMessage());
//                            }
//                            ArrayAdapter spinnerArray = new ArrayAdapter(getContext(),R.layout.spinner_series_bus,lista_series);
//                            sp_serie.setAdapter(spinnerArray);
//                            String JSON_SERIES = gson.toJson(lista_series);
//                            guardarDataMemoria("jsonSeries", JSON_SERIES, getContext());
//
////                            spin_serie.setAdapter((SpinnerAdapter) lista_series);
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
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
//        RequestBuscaBol1.setRetryPolicy(new DefaultRetryPolicy(timeout,numeroIntentos,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        queue.add(RequestBuscaBol1);
//    }
}