package pe.com.telefonica.soyuz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

public class RecyclerServicioVipAdapter extends RecyclerView.Adapter<RecyclerServicioVipAdapter.ViewHolder>{
    private List<ServicioExpressModel> mData;
    private LayoutInflater mInflater;
    private RecyclerServicioVipAdapter.ItemClickListener mClickListener;

    RecyclerServicioVipAdapter(Context context, List<ServicioExpressModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }
    @Override
    public RecyclerServicioVipAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.servicio_especial_rv_row, parent, false);
        return new RecyclerServicioVipAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RecyclerServicioVipAdapter.ViewHolder holder, int position) {
        ServicioExpressModel Mod = mData.get(position);
        holder.txt_ho_sali_vip_rv.setText("Salida:"+Mod.getHO_SALI());
       // holder.text_Empresa_vip_rv.setText("EMPRESA:"+Mod.getCO_EMPR() );
        holder.text_Bus_vip_rv.setText("Bus:"+Mod.getCO_VEHI());
       // holder.text_NuSecu_vip_rv.setText("SECUENCIA:"+Mod.getNU_SECU());
        holder.text_Cant_Bol_vip_rv.setText("Asientos Disponibles:"+Mod.getNU_ASIE_DIS());
       // holder.txt_Rumbo_vip_rv.setText("RUMBO:"+Mod.getCO_RUMB());
        //holder.txt_CantAsie_vip_rv.setText("NU_ASIE:"+Mod.getCANT_ASIE());
       // holder.txt_TipoServicio_vip_rv.setText("SERVICIO:"+Mod.getST_TIPO_SERV());
        holder.txt_Origen_vip_rv.setText("Origen:"+Mod.getCO_DEST_ORIG());
        holder.txt_Destino_vip_rv.setText("Destino:"+Mod.getCO_DEST_FINA());
        holder.txt_TIPO_BUSS_rv.setText(Mod.getDE_TIPO_BUS());


    }
    @Override
    public int getItemCount() {
        return mData.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_ho_sali_vip_rv;
        TextView text_Empresa_vip_rv;
        TextView text_Bus_vip_rv;
        TextView text_NuSecu_vip_rv;
        TextView text_Cant_Bol_vip_rv;
        TextView txt_Rumbo_vip_rv;
        //TextView txt_CantAsie_vip_rv;
        TextView txt_TipoServicio_vip_rv;
        TextView txt_Origen_vip_rv;
        TextView txt_Destino_vip_rv;
        TextView txt_TIPO_BUSS_rv;
        ViewHolder(View itemView) {
            super(itemView);
            txt_ho_sali_vip_rv = itemView.findViewById(R.id.txt_ho_sali_vip);
          //  text_Empresa_vip_rv = itemView.findViewById(R.id.text_Empresa_vip);
            text_Bus_vip_rv = itemView.findViewById(R.id.text_Bus_vip);
         //   text_NuSecu_vip_rv = itemView.findViewById(R.id.text_NuSecu_vip);
            text_Cant_Bol_vip_rv = itemView.findViewById(R.id.text_Cant_Bol_vip);
         //   txt_Rumbo_vip_rv = itemView.findViewById(R.id.txt_Rumbo_vip);
          //  txt_CantAsie_vip_rv = itemView.findViewById(R.id.txt_CantAsie_vip);
         //   txt_TipoServicio_vip_rv = itemView.findViewById(R.id.txt_TipoServicio_vip);
            txt_Origen_vip_rv = itemView.findViewById(R.id.txt_Origen_vip);
            txt_Destino_vip_rv = itemView.findViewById(R.id.txt_Destino_vip);
            txt_TIPO_BUSS_rv = itemView.findViewById(R.id.txt_de_tipo_buss);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    ServicioExpressModel getItem(int id) {
        return mData.get(id);
    }
    public String getTextList(int id)
    {
        return  mData.get(id).toString();
    }
    void setClickListener(RecyclerServicioVipAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
