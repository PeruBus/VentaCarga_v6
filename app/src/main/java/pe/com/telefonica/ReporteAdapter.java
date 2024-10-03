package pe.com.telefonica.soyuz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ReporteAdapter{
    private ArrayList<ReporteControladorTrafico> boletaArrayList;
    private Context context;
    public ReporteAdapter(Context context, ArrayList<ReporteControladorTrafico> planets) {
        this.context = context;
        this.boletaArrayList = planets;
    }
    public class ReportetHolder extends RecyclerView.ViewHolder {
        private TextView txtBus, text_NuSecu, text_Cant_Bol, txt_Rumbo,txt_Empresa;
        public ReportetHolder(View itemView) {
            super(itemView);
            txtBus = itemView.findViewById(R.id.text_Bus);
            text_NuSecu = itemView.findViewById(R.id.text_NuSecu);
            text_Cant_Bol = itemView.findViewById(R.id.text_Cant_Bol);
            txt_Rumbo = itemView.findViewById(R.id.txt_Rumbo);
            txt_Empresa=itemView.findViewById(R.id.text_Empresa);
        }
        public void setDetails(ReporteControladorTrafico bol) {
            txtBus.setText(bol.getEmpresa());
            txt_Empresa.setText(bol.getEmpresa());
            text_NuSecu.setText(String.format("Distance from Sun : %d Million KM", bol.getEmpresa()));
            text_Cant_Bol.setText(String.format("Surface Gravity : %d N/kg", bol.getEmpresa()));
            txt_Rumbo.setText(String.format( "Diameter : %d KM", bol.getEmpresa()));
        }


    }
    //@Override
    public int getItemCount() {
        return boletaArrayList.size();
    }
    //@Override
    public ReportetHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context) .inflate (R.layout.recyclerview_row, parent, false);
        return new ReportetHolder(view);
    }
    //@Override
    public void onBindViewHolder(ReportetHolder holder, int position) {
        ReporteControladorTrafico planet = boletaArrayList.get(position);
        holder.setDetails(planet);
    }




}


