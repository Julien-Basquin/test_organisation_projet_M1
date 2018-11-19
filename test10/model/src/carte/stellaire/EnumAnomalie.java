package carte.stellaire;

public enum EnumAnomalie {

	NEBULEUSE(1),
	EPAVE(2),
	COMETE(3),
	ASTEROIDE(4),
	PIRATE(5),
	SOLEIL_SUPPLEMENTAIRE(6),
	TROU_NOIR(7);
	
	private int numero;
	private static int nbAnomalie = 7;
	
	private EnumAnomalie(int numero) {
		this.numero=numero;
	}
	
	public static EnumAnomalie type() {
		int type = (int) (nbAnomalie*Math.random()+1);
		for(EnumAnomalie e : values()) {
			if(type==e.getNumero()) {
				return e;
			}
		}
		return null;
	}
	
	public int getNumero() {
		return numero;
	}
	
}
