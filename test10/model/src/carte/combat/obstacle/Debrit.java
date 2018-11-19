package carte.combat.obstacle;

public class Debrit extends Obstacle{
	
	/**
	 * cr�ation d'une zone car�e de debrit de 200/200
	 * @param coordonee du milieu
	 */
	public Debrit(int x, int y) {
		super();
		this.addCoordonee(x+100, y+100);
		this.addCoordonee(x+100, y-100);
		this.addCoordonee(x-100, y+100);
		this.addCoordonee(x-100, y-100);
	}
	
	
}
