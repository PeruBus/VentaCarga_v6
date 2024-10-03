package pe.com.telefonica.soyuz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;

public class tabla_reporte_insp extends BaseAdapter {

    private ArrayList<String> lista_inspecciones;
    private Context mContext;
    public tabla_reporte_insp(ArrayList<String> array_inspecc, Context context){
        lista_inspecciones = array_inspecc;
        mContext = context;
    }

    @Override
    public int getCount() {

        String[] dataReporteInspeccion = lista_inspecciones.get(0).split("/");
        return dataReporteInspeccion.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Gson gson = new Gson();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.tabla_reporte_insp, null);

        } else {
            view = convertView;
        }

        TextView txt_feprog = view.findViewById(R.id.txt_feprog);
        TextView txt_ininsp = view.findViewById(R.id.txt_ininsp);
        TextView txt_fiinsp = view.findViewById(R.id.txt_fiinsp);
        TextView txt_covehi = view.findViewById(R.id.txt_covehi);
        TextView txt_cainsp = view.findViewById(R.id.txt_cainsp);

        String[] dataReporteInspeccion = lista_inspecciones.get(0).split("/");

        if(!dataReporteInspeccion[0].equals("NoData")) {
            ArrayList<String> lista_inspecciones = getArray(sharedPreferences, gson, "json_repoinsp");

            String[] lista_fech = new String[dataReporteInspeccion.length];
            String[] lista_inic = new String[dataReporteInspeccion.length];
            String[] lista_finn = new String[dataReporteInspeccion.length];
            String[] lista_vehi = new String[dataReporteInspeccion.length];
            String[] lista_cant = new String[dataReporteInspeccion.length];

            for (int i = 0; i < dataReporteInspeccion.length; i++) {

                String[] dataCantiInspe = dataReporteInspeccion[i].split("-");
                lista_fech[i] = dataCantiInspe[0] + "-" + dataCantiInspe[1] + "-" + dataCantiInspe[2];
                lista_inic[i] = dataCantiInspe[3];
                lista_finn[i] = dataCantiInspe[4];
                lista_vehi[i] = dataCantiInspe[5];
                lista_cant[i] = dataCantiInspe[6];

            }

            txt_feprog.setText(lista_fech[position]);
            txt_ininsp.setText(lista_inic[position]);
            txt_fiinsp.setText(lista_finn[position]);
            txt_covehi.setText(lista_vehi[position]);
            txt_cainsp.setText(lista_cant[position]);

        }
        return view;
    }

}