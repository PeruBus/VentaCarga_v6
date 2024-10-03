package pe.com.telefonica.soyuz;

public class ReporteControladorTrafico {
    private String pCO_VEHI;
    private String origen;
    private String destino;
    private String fechaVenta;
    private String correlativoPasaje;
    private String seriePasaje;
    private String empresa;
    private String cantidad;
    private String nombreAnfitrion;

    public ReporteControladorTrafico(String CO_VEHI, String Origen, String Destino, String Fe_Viaj,String correlativoPasaje ,String seriePasaje,String Empresa,
                                     String Cantidad,String Anfitrion) {
        this.pCO_VEHI = CO_VEHI;
        this.origen = Origen;
        this.destino = Destino;
        this.fechaVenta = Fe_Viaj;
        this.correlativoPasaje=correlativoPasaje;
        this.seriePasaje=seriePasaje;
        this.empresa=Empresa;
        this.cantidad=Cantidad;
        this.nombreAnfitrion=Anfitrion;
    }
    public String getpCO_VEHI() {
        return pCO_VEHI;
    }
    public String getpfechaVenta() {
        return fechaVenta;
    }
    public String getpempresa() {
        return empresa;
    }
    public String getpnombreAnfitrion() {
        return nombreAnfitrion;
    }
    public String getpcantidad() {
        return cantidad;
    }
    public String getEmpresa() {
        return empresa;
    }
    public void setCo_Vehi(String pCO_VEHI) {
        this.pCO_VEHI = pCO_VEHI;
    }
    public String getorigen() {
        return origen;
    }
    public void setDestino(String destino) {
        this.destino = destino;
    }

}
