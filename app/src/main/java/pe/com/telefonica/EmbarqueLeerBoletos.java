package pe.com.telefonica.soyuz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;

public class EmbarqueLeerBoletos extends Fragment {

    /**
     * Lista que almacena los boletos leídos.
     */
    private ListView listView;
    /**
     * Bandera de existencia del correlativo leido.
     */
    private Boolean existe = false;

    private SharedPreferences sharedPreferences;

    final ArrayList<String> lista_boletosLeidos = new ArrayList<>();
     Button btn_buscarItin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return inflater.inflate(R.layout.embarque_codqr, parent, false);
    }

    /**
     * Implementación de la lógica para realizar la búsqueda de un boleto de viaje.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        startBoletoService();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        final Context contextBuscarBoleto = view.getContext();

        final Button btn_leerQR = view.findViewById(R.id.btn_leerQR);
        //final Button btn_buscarItin = view.findViewById(R.id.btn_buscarItin);
        btn_buscarItin = view.findViewById(R.id.btn_buscarItin);
        final Button btn_buscarItin_1 = view.findViewById(R.id.btn_buscarItin_1);

        btn_buscarItin.setEnabled(false);
        //Log.d("lista",lista_boletosLeidos.toString())
        /* Arreglo */
        // lista_destinos: arreglo que contiene todos los destinos
        //final ArrayList<String> lista_boletosLeidos = new ArrayList<>();
        /* ----------------------------------------- */
        AgregaBoletos(view);
        btn_leerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    /* Se inicializa el scanner del equipo */
                    final IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
                    IScanner iScanner = dal.getScanner(EScannerType.REAR);
                    /* ----------------------------------------- */

                    if (iScanner.open()) {
                        iScanner.setContinuousTimes(0);
                        iScanner.start(new IScanner.IScanListener() {

                            @Override
                            public void onRead(String codigoQR) {

                                existe = false;

                                /* Se obtiene la data leída con el scanner */
                                String[] dataCodigoQR = codigoQR.split("\\|");
                                // dataCodigoQR[0] = ruc empresa
                                // dataCodigoQR[1] = tipoDocumento
                                // dataCodigoQR[2] = serie
                                // dataCodigoQR[3] = correlativo
                                /* ----------------------------------------- */

                                /* Se genera el número del boleto leído */
                                //Log.d("core", dataCodigoQR[3]);
                                String numCorrelativoBLTLeido = completarCorrelativo(Integer.valueOf(dataCodigoQR[3]));
                                //String numBoletoLeido = dataCodigoQR[2]+"-"+numCorrelativoBLTLeido;
                                /* ----------------------------------------- */

                                /* Arreglo para las empresas */
                                final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                                /* ----------------------------------------- */
                                //Log.d("tamano", lista_empresas.toString());

                                /* Selección de la empresa según itinerario */
                                String codEmpresa = "";
                                for (int i = 0; i < lista_empresas.size(); i++) {

                                    String[] dataEmpresa = lista_empresas.get(i).split("-");
                                    // dataEmpresa[0] = CODIGO_EMPRESA
                                    // dataEmpresa[1] = EMPRESA
                                    // dataEmpresa[2] = DIRECCION
                                    // dataEmpresa[3] = LIMA
                                    // dataEmpresa[4] = DEPARTAMENTO
                                    // dataEmpresa[5] = PROVINCIA
                                    // dataEmpresa[6] = DISTRITO
                                    // dataEmpresa[7] = RUC
                                    // dataEmpresa[8] = RAZON_SOCIAL

                                    if(dataCodigoQR[0].equals(dataEmpresa[7])){
                                        codEmpresa = dataEmpresa[0];
                                        break;
                                    }
                                }
                                /* ----------------------------------------- */

                                String tipoDocumento;
                                if (dataCodigoQR[1].equals("01")) {
                                    tipoDocumento = "FAC";
                                } else {
                                    tipoDocumento = "BLT";
                                }

                                //FOR
                                //tramaBoletoLeido += "origen-destino-"+dataCodigoQR[2]+"-01-"+dataCodigoQR[6]+"/";
                                if(lista_boletosLeidos.size() == 0){

                                    lista_boletosLeidos.add(codEmpresa+"."+dataCodigoQR[2]+"."+numCorrelativoBLTLeido+"."+tipoDocumento+"."+dataCodigoQR[6]);
                                    guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
                                    //Log.d("tamano", Integer.toString(lista_boletosLeidos.size()));

                                    /* Los boletos leídos se muestran en la tabla */
                                    listView = view.findViewById(R.id.listView_boletosLeidos);
                                    TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
                                    listView.setAdapter(adapterBoletosLeidos);
                                    /* ----------------------------------------- */

                                    btn_buscarItin.setEnabled(true);


                                }else{


                                    for (int i = 0; i < lista_boletosLeidos.size(); i++) {

                                        if(lista_boletosLeidos.get(i).contains(numCorrelativoBLTLeido)){

                                            existe = true;
                                            Toast.makeText(getActivity(),"El boleto ya fue agregado", Toast.LENGTH_LONG).show();
                                            break;

                                        }
                                    }

                                    if(!existe){
                                        lista_boletosLeidos.add(codEmpresa+"."+dataCodigoQR[2]+"."+numCorrelativoBLTLeido+"."+tipoDocumento+"."+dataCodigoQR[6]);
                                        guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
                                        Log.d("tamano", Integer.toString(lista_boletosLeidos.size()));
                                    }

                                }

                                /* Los boletos leídos se muestran en la tabla */
                                listView = view.findViewById(R.id.listView_boletosLeidos);
                                TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
                                listView.setAdapter(adapterBoletosLeidos);
                                /* ----------------------------------------- */

                                btn_buscarItin.setEnabled(true);
                                Log.d("tamano", lista_boletosLeidos.toString());
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

                /* Cambia a la vista de buscar boleto */
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
                BuscarBoleto BuscaBoletoCliente = new BuscarBoleto();
                //BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, BuscaBoletoCliente).commit();
                /* ----------------------------------------- */

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
    public void startBoletoService() {
        BoletoService.startService(getActivity(), true);
    }
    void AgregaBoletos(View view)
    {
        String DataArreglo = sharedPreferences.getString("lista_boletosLeidos","NoData");
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

            //List<String> list = new ArrayList<String>(Arrays.asList(string.split(" , ")));
        }


    }
}

