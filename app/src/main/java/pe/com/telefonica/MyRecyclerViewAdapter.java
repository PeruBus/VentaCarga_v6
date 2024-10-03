package pe.com.telefonica.soyuz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<ReporteControladorTrafico> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    MyRecyclerViewAdapter(Context context, List<ReporteControladorTrafico> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReporteControladorTrafico animal = mData.get(position);
        //holder.myTextView.setText(animal.getCantidad());
        holder.txtBus.setText("CODIGO VEHICULO : "+animal.getpCO_VEHI());
        holder.text_NuSecu.setText("SECUENCIA VIAJE: "+animal.getpnombreAnfitrion());
        holder.text_Cant_Bol.setText("CANT.BOL_ASIGNADOS: "+animal.getpcantidad());
        holder.txt_Rumbo.setText("");
        holder.txt_Empresa.setText("EMPRESA : "+animal.getEmpresa());
    }
    @Override
    public int getItemCount() {
        return mData.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView,txtBus, text_NuSecu, text_Cant_Bol, txt_Rumbo,txt_Empresa;

        ViewHolder(View itemView) {
            super(itemView);
            txtBus = itemView.findViewById(R.id.text_Bus);
            text_NuSecu = itemView.findViewById(R.id.text_NuSecu);
            text_Cant_Bol = itemView.findViewById(R.id.text_Cant_Bol);
            txt_Rumbo = itemView.findViewById(R.id.txt_Rumbo);
            txt_Empresa=itemView.findViewById(R.id.text_Empresa);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    ReporteControladorTrafico getItem(int id) {
            return mData.get(id);
    }
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
