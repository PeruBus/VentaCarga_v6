package pe.com.telefonica.soyuz;

public class Spinner_model {
    public String id = "0";
    public String name = "";
    public String abbrev = "";
    public Spinner_model(String _id, String _name, String _abbrev )
    {
        id = _id;
        name = _name;
        abbrev = _abbrev;
    }
    /*public String toString()
    {
        return( name + " (" + abbrev + ")" );
    }*/
    public String toString()
    {
        return(name);
    }
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Spinner_model)) {
            return false;
        }
        Spinner_model Model = (Spinner_model) o;
        return Model.id.equals(id) &&
                Model.abbrev.equals(abbrev) &&
                Model.name.equals(name);
    }

}
