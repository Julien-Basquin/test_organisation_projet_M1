package carte.stellaire;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import carte.stellaire.Systeme.Position;
import parametre.Parametre;

public class Carte {
	/**Liste des systèmes*/
	private List<Systeme> listeSysteme;
	private Map<Systeme.Position, Systeme> positionSysteme;
	
	public Carte(Parametre parametres) {
		listeSysteme = new ArrayList<Systeme>();

		generationSystemes(parametres);
		liaisonSystemes();
//		reevaluation();
	}

	/**
	 * Génération des systèmes (génération en largeur)
	 * 
	 * @param tailleSysteme		Nombre de systèmes de la partie
	 */
	private void generationSystemes(Parametre parametre) {
		int index = 0, couche = 0, rang = 0,id=0;

		//Génération du premier système
		Systeme premierSysteme = new Systeme(parametre.getAbondanceRessource(), parametre.getNbMaxPlanete(), parametre.getNbMaxAnomalie(), couche, rang,id);
		couche++;
		listeSysteme.add(premierSysteme);
		int systemeAGenerer = premierSysteme.getNbLiensMax();

		//Génération des autres systèmes de la carte
		while (listeSysteme.size() < parametre.getTailleCarte().getQuantite()) {
			if (listeSysteme.get(index).getNbLiens() < listeSysteme.get(index).getNbLiensMax()) {
				Systeme systeme = new Systeme(parametre.getAbondanceRessource(), parametre.getNbMaxPlanete(), parametre.getNbMaxAnomalie(), couche, rang,++id);
				listeSysteme.add(systeme);
				int distance = (int) (Math.random()*10+1);
				listeSysteme.get(index).faireLien(systeme, distance);
				rang++;
				systemeAGenerer--;
			}

			if (systemeAGenerer == 0
					|| listeSysteme.get(index).getNbLiens() >= listeSysteme.get(index).getNbLiensMax()) {
				index++;
				if (listeSysteme.get(index).getRang() == 0) {
					couche++;
					rang = 0;
				}
				systemeAGenerer = (int)(Math.random()*listeSysteme.get(index).getNbLiensMax()-1);
			}
		}
		
		positionSysteme = new HashMap<Systeme.Position, Systeme>();
		
		for (Systeme sys : listeSysteme) {
			positionSysteme.put(sys.getPosition(), sys);
		}
	}

	/**Liaison des systèmes pouvant être liés*/
	private void liaisonSystemes() {
		//Ensemble des systèmes très selon leur position (couche, rang)
		TreeMap<Position, Systeme> mapSysteme = new TreeMap<Position, Systeme>(new Comparator<Position>() {
			@Override
			public int compare(Position o1, Position o2) {
				return o1.getCouche() < o2.getCouche() ? -1 :
					(o1.getCouche() > o2.getCouche() ? 1 : 
						(o1.getRang() < o2.getRang() ? -1 : 
							(o1.getRang() > o2.getRang() ? 1 : 0)));
			}
		});
		
		for (Systeme sys : listeSysteme) {
			mapSysteme.put(sys.getPosition(), sys);
		}

		//Ensemble des liaision pouvant être effectuées pour chaque système
		Map<Systeme, List<Systeme>> ensembleLiaison = new TreeMap<Systeme, List<Systeme>>(new Comparator<Systeme>() {
			@Override
			public int compare(Systeme o1, Systeme o2) {
				return o1.getCouche() < o2.getCouche() ? -1 :
					(o1.getCouche() > o2.getCouche() ? 1 : 
						(o1.getRang() < o2.getRang() ? -1 : 
							(o1.getRang() > o2.getRang() ? 1 : 0)));
			}
		});
		
		//Vérification des liaisons pouvant être effecutées pour chaque système
		for (Systeme sys : listeSysteme) {
			ensembleLiaison.put(sys, new ArrayList<Systeme>());

			//Liaison gauche
			if (mapSysteme.containsKey(sys.getPositionPrecedente())
					&& verifLiaison(mapSysteme, sys, mapSysteme.get(sys.getPositionPrecedente()))) {
				ensembleLiaison.get(sys).add(mapSysteme.get(sys.getPositionPrecedente()));
			}
			//Liaison droite
			if (mapSysteme.containsKey(sys.getPositionSuivante())
					&& verifLiaison(mapSysteme, sys, mapSysteme.get(sys.getPositionSuivante()))) {
				ensembleLiaison.get(sys).add(mapSysteme.get(sys.getPositionSuivante()));
			}
			//Liaison haut-gauche
			if (mapSysteme.containsKey(sys.getPositionPrecedente())
					&& mapSysteme.containsKey(mapSysteme.get(sys.getPositionPrecedente()).getDernierLien())
					&& verifLiaison(mapSysteme, sys, mapSysteme.get(mapSysteme.get(sys.getPositionPrecedente()).getDernierLien()))) {
				ensembleLiaison.get(sys).add(mapSysteme.get(mapSysteme.get(sys.getPositionPrecedente()).getDernierLien()));
			}
			//Liaison haut-droite
			if (mapSysteme.containsKey(sys.getPositionSuivante())
					&& mapSysteme.containsKey(mapSysteme.get(sys.getPositionSuivante()).getPremierLien())
					&& mapSysteme.get(sys.getPositionSuivante()).getLiens().size() > 1
					&& verifLiaison(mapSysteme, sys, mapSysteme.get(mapSysteme.get(sys.getPositionSuivante()).getPremierLien()))) {
				ensembleLiaison.get(sys).add(mapSysteme.get(mapSysteme.get(sys.getPositionSuivante()).getPremierLien()));
			}
		}

		//Liaisons des systèmes
		for (Map.Entry<Systeme, List<Systeme>> sys : ensembleLiaison.entrySet()) {
			if (!sys.getValue().isEmpty()) {
				int index = (int) (Math.random()*sys.getValue().size());
				while (index >= 0 && sys.getKey().getNbLiens() < sys.getKey().getNbLiensMax()
						&& sys.getValue().get(index).getNbLiens() < sys.getValue().get(index).getNbLiensMax()){
					if (!sys.getKey().getLiens().containsKey(sys.getValue().get(index))) {
						sys.getKey().faireLien(sys.getValue().get(index), (int)(Math.random()*10)+1);
					}

					index--;
				}
			}
		}
	}
	
	/**Réévaluation des distances entre les systèmes*/
	//TODO TEST SUR DISTANCES
	private void reevaluation() {
		boolean reevaluer = true;
		while (reevaluer) {
			reevaluer = false;
			for (Systeme sys : listeSysteme) {
				for (Map.Entry<Systeme, Integer> entry : sys.getLiens().entrySet()) {
					Systeme cible1 = sys.getLiens().firstKey();
					Systeme cible2 = entry.getKey();
					if (cible1 != cible2
							&& cible1.getCouche() > sys.getCouche()
							&& cible2.getCouche() > sys.getCouche()) {
						if (cible1.getDistance(cible2) < sys.getDistance(cible1) + sys.getDistance(cible2)) {
							cible1.setDistance(cible2, sys.getDistance(cible1) + sys.getDistance(cible2));
							reevaluer = true;
						} else if (sys.getDistance(cible2) < sys.getDistance(cible1) + cible1.getDistance(cible2)) {
							sys.setDistance(cible2, sys.getDistance(cible1) + cible1.getDistance(cible2));
							reevaluer = true;
						} else if (sys.getDistance(cible1) < cible1.getDistance(cible2) + sys.getDistance(cible2)) {
							sys.setDistance(cible1, cible1.getDistance(cible2) + sys.getDistance(cible2));
							reevaluer = true;
						}
						cible1 = cible2;
					}
				}

				if (positionSysteme.get(sys.getPositionPrecedente()) != null) {
					Systeme cible1 = positionSysteme.get(sys.getPositionPrecedente());
					Systeme premierLien = sys.getLiens().firstKey();

					//TODO Vérifier limite map
					while (premierLien.getPosition().getCouche() <= sys.getPosition().getCouche()) {
						premierLien = sys.getLiens().higherKey(premierLien);
					}

					Systeme cible2 = sys.getLiens().ceilingKey(premierLien);

					if (sys.getDistance(cible2) < sys.getDistance(cible1) + cible1.getDistance(cible2)) {
						sys.setDistance(cible2, sys.getDistance(cible1) + cible1.getDistance(cible2));
						reevaluer = true;
					} else if (sys.getDistance(cible1) < cible1.getDistance(cible2) + sys.getDistance(cible2)) {
						sys.setDistance(cible1, cible1.getDistance(cible2) + sys.getDistance(cible2));
						reevaluer = true;
					} else if (cible1.getDistance(cible2) < sys.getDistance(cible1) + sys.getDistance(cible2)) {
						cible1.setDistance(cible2, sys.getDistance(cible1) + sys.getDistance(cible2));
						reevaluer = true;
					}
				}

				if (positionSysteme.get(sys.getPositionSuivante()) != null) {
					Systeme cible1 = positionSysteme.get(sys.getPositionSuivante());
					Systeme cible2 = sys.getLiens().lastKey();

					if (sys.getDistance(cible2) < sys.getDistance(cible1) + cible1.getDistance(cible2)) {
						sys.setDistance(cible2, sys.getDistance(cible1) + cible1.getDistance(cible2));
						reevaluer = true;
					} else if (sys.getDistance(cible1) < cible1.getDistance(cible2) + sys.getDistance(cible2)) {
						sys.setDistance(cible1, cible1.getDistance(cible2) + sys.getDistance(cible2));
						reevaluer = true;
					} else if (cible1.getDistance(cible2) < sys.getDistance(cible1) + sys.getDistance(cible2)) {
						cible1.setDistance(cible2, sys.getDistance(cible1) + sys.getDistance(cible2));
						reevaluer = true;
					}
				}

			}
		}
	}

	/**
	 * Vérifie si deux systèmes peuvent être liés
	 * 
	 * @param mapSysteme	Ensemble de tous les systèmes
	 * @param origine		Système à lier
	 * @param cible			Système à lier
	 * @return	Vrai si les systèmes origine et cible peuvent être liés, Faux sinon
	 */
	private boolean verifLiaison(TreeMap<Position, Systeme> mapSysteme, Systeme origine, Systeme cible) {

		if (cible != null
				&& origine != cible
				&& !origine.getLiens().containsKey(cible)
				&& origine.getNbLiens() < origine.getNbLiensMax()
				&& cible.getNbLiens() < cible.getNbLiensMax()
				&& ((origine.getCouche() == cible.getCouche()
				&& Math.abs(origine.getRang()-cible.getRang()) == 1)
						|| (mapSysteme.containsKey(origine.getPositionSuivante())
								&& cible.getLiens().firstKey() == mapSysteme.get(origine.getPositionSuivante())
								&& (!mapSysteme.get(origine.getPositionSuivante()).getLiens().containsKey(origine.getLiens().lastKey()))
								|| (mapSysteme.containsKey(origine.getPositionPrecedente())
										&& cible.getLiens().firstKey() == mapSysteme.get(origine.getPositionPrecedente())
										&& (origine.getLiens().size() > 1
												&& !mapSysteme.get(origine.getPositionPrecedente()).getLiens().containsKey(origine.getLiens().higherKey(origine))))))) {
			return true;
		}

		return false;
	}

	/**
	 * Affichage console des différents systèmes et de leurs liens
	 */
	public void affichage() {
		for (Systeme sys : listeSysteme) {
			System.out.print("("+sys.getCouche()+"."+sys.getRang()+")->");
			for (Systeme lien : sys.getLiens().keySet()) {
				System.out.print("("+lien.getCouche()+"."+lien.getRang()+"):"+sys.getLiens().get(lien)+"->");
			}
			System.out.println("NULL");
		}
	}

	/**
	 * @return	Liste des systèmes
	 */
	public List<Systeme> getListeSysteme() {
		return listeSysteme;
	}

	public Map<Systeme.Position, Systeme> getPositionSysteme() {
		return positionSysteme;
	}
}
