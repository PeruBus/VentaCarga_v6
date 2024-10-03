package pe.com.telefonica.soyuz;

public class ItinerarioModel {
    public String getNU_SECU() {
        return NU_SECU;
    }

    public String getCO_EMPR() {
        return CO_EMPR;
    }

    public String getCO_EMPL_COND() {
        return CO_EMPL_COND;
    }

    public String getCO_EMPL_AYUD() {
        return CO_EMPL_AYUD;
    }

    public String getFE_PROG() {
        return FE_PROG;
    }

    public String getHO_SALI() {
        return HO_SALI;
    }

    public String getCO_RUMB() {
        return CO_RUMB;
    }

    public String getNO_COND() {
        return NO_COND;
    }

    public String getNO_AYUD() {
        return NO_AYUD;
    }

    public String getCO_DEST_ORIG() {
        return CO_DEST_ORIG;
    }

    public String getCO_DEST_FINA() {
        return CO_DEST_FINA;
    }

    public String getCO_VEHI() {
        return CO_VEHI;
    }

    public String NU_SECU="";
    public String CO_EMPR="";
    public String CO_EMPL_COND="";
    public String CO_EMPL_AYUD="";
    public String FE_PROG="";
    public String HO_SALI="";
    public String CO_RUMB="";
    public String NO_COND="";
    public String NO_AYUD="";
    public String CO_DEST_ORIG="";
    public String CO_DEST_FINA="";
    public String CO_VEHI="";





    public ItinerarioModel(String pNU_SECU, String pCO_EMPR, String pCO_EMPL_COND,String pCO_EMPL_AYUD,
                           String pFE_PROG,String pHO_SALI,String pCO_RUMB,String pNO_COND,String pNO_AYUD,String pCO_VEHI,String pCO_DEST_ORIG,String pCO_DEST_FINA)
    {
        this.NU_SECU = pNU_SECU;
        this.CO_EMPR = pCO_EMPR;
        this.CO_EMPL_COND = pCO_EMPL_COND;
        this.CO_EMPL_AYUD=pCO_EMPL_AYUD;
        this.FE_PROG=pFE_PROG;
        this.HO_SALI=pHO_SALI;
        this.CO_RUMB=pCO_RUMB;
        this.NO_COND=pNO_COND;
        this.NO_AYUD=pNO_AYUD;
        this.CO_VEHI=pCO_VEHI;
        this.CO_DEST_ORIG=pCO_DEST_ORIG;
        this.CO_DEST_FINA=pCO_DEST_FINA;
    }
    public String toString()
    {
        return(NU_SECU+"ƒ"+ CO_EMPR +"ƒ"+  FE_PROG +"ƒ"+HO_SALI +"ƒ"+CO_VEHI);
    }


}
