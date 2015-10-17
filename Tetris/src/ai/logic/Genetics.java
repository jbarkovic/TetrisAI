package ai.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Random;

import interfaces.CallBackMessenger;
import interfaces.EngineInterface;
import interfaces.UI;
import interfaces.Watcher;


public class Genetics {
	private static final int POP_SIZE = 200;
	private static final int MAX_THREADS = 8;
	private static final int NUM_GENS = 400;
	private Specimen best = null;
	private ArrayList<Specimen> improvements = new ArrayList<Specimen> ();
	private Random r;
	public static void main (String [] args) {
		Genetics g = new Genetics ();
		g.run();
		if (g.best != null) {
			for (int i=0;i<g.improvements.size();i++) {
				System.out.println("Best Fitness: " + g.improvements.get(i).getFitness());
				System.out.println("\tWeights : " + Arrays.toString(g.improvements.get(i).weight.getConfig()));
			}
		} else {
			for (int i=0;i<1000;i++){
			System.out.println("Best was not found");
			}
		}
	}
	public Genetics () {
		r = new Random(System.currentTimeMillis());
	}
	
	private void run () {
		ArrayList<Specimen> population = new ArrayList<Specimen>(POP_SIZE);
		ArrayList<Specimen> active = new ArrayList<Specimen> (MAX_THREADS);
		for (int i=0;i<POP_SIZE;i++) {
			population.add(new Specimen(i+"", mutate(new Weights(), 0.15d)));
		}
		for (int gen=0;gen<NUM_GENS;gen++) {
			for (int i=0;i<POP_SIZE;i++) {
				population.set(i,new Specimen(i+"", population.get(i).weight));
			}
			for (int i=0;i<POP_SIZE;i++) {
				for (int j=i;j<Math.min(POP_SIZE,MAX_THREADS);j++) {
					active.add(population.get(j));
				}
				for (Specimen s : active) {
					s.start();
				}
				for (Specimen s : active) {
					try {
						s.join();
						//System.out.flush();
						System.out.println("Joined # " + (i+1) + " of " + POP_SIZE);
						if (best==null || s.getFitness() > best.getFitness()) best = s;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				active.clear();
				i+= MAX_THREADS;
			}
			for (int i=0;i<POP_SIZE;i++) {

			}
			Collections.sort(population);
			best = population.get(population.size()-1);
			ArrayList<Specimen> children = randomBreeding(population);
			children.addAll(randomBreeding(population));
			children.addAll(randomBreeding(population));
			if (population.size() > children.size()) {
				for (int i=0;i<children.size();i++) {
					population.set(i, children.get(i));
				}
			} else {
				System.err.println("More children than parents");
				return;
			}
			System.out.flush();
			System.out.println("Finished a generation");
			if (best != null) {
				improvements.add(best);
				System.out.println("Best Fitness: " + best.getFitness());
				System.out.println("\tWeights : " + Arrays.toString(best.weight.getConfig()));
			}
		}
		System.out.flush();
		System.out.println("Done");
	}
	private ArrayList<Specimen> randomBreeding(ArrayList<Specimen> population) {
		ArrayList<Specimen> parents= new ArrayList<Specimen> ();
		ArrayList<Specimen> children= new ArrayList<Specimen> ();
		for (int i=0;i<(int)((double)POP_SIZE*0.05d)*2;i++) {
			parents.add(population.get(r.nextInt(POP_SIZE)));
		}
		ListIterator<Specimen> li = parents.listIterator();
		while (li.hasNext()) {
			Specimen father = li.next();
			Specimen mother = null;
			if (li.hasNext()) mother = li.next();
			else mother = new Specimen("", mutate(father.weight, .25));
			
			children.add(new Specimen("child" , mutate(combine(father,mother), 0.05)));
		}
		return children;
	}
	private Weights combine (Specimen s0, Specimen s1) {
		double w0 = s0.getFitness() / Math.max(1, s0.getFitness() + s1.getFitness());
		double w1 = s1.getFitness() / Math.max(1, s0.getFitness() + s1.getFitness());
		
		double [] s0Conf = s0.weight.getConfig();
		double [] s1Conf = s1.weight.getConfig();
		
		double [] newConf = new double [Math.min(s0Conf.length, s1Conf.length)];
		
		for (int i=0;i<newConf.length;i++) {
			newConf[i] = w0*s0Conf[i] + w1*s1Conf[i];
		}
		
		return new Weights(newConf);
	}
	private Weights random () {
		return mutate(new Weights(), 1d);
	}
	private Weights mutate (Weights w0, double chance) {
		double [] w = w0.config;
		double percent = chance*100;
		for (int i=0;i<w.length;i++) {
			if (r.nextInt(100) < percent) {
				if (r.nextInt(4) > 2) {
					w[i] += (double) r.nextInt(5);
				} else {
					w[i] -= (double) r.nextInt(5);
				}
			}
		}
		return w0;
	}
	private class Specimen extends Thread implements Watcher, Comparable<Specimen> {
		private EngineInterface engine;
		private CallBackMessenger cb;
		private UI ui;
		int fitness = 0;
		int shapeCount = 0;
		String name;
		Weights weight;
		public Specimen (String name, Weights weight){
			ui = UI.produceUI(new String [] {"20", "10"});
			engine = ui.engine;
			this.weight = weight;
			cb = ui.cback;
			cb.addWatcher(this);
			this.name = name;
			// badcb.getAI().setSolutionValue(new SolutionValue(weight));
		}
		public int getFitness () {
			return fitness;
		}
		@Override
		public void notifyWatcher() {
			if (shapeCount > 150) {
				System.out.println("Simulation done ");
				cb.stopAI();;
				fitness = engine.getNumberOfRowsCleared();
				System.out.println("Stopping thread");
				this.stop();
				//this.notify();
			} else if (engine.wasThereANewShape()) {
					shapeCount++;
			}
		}
		public int compareTo(Specimen other) {
			return Integer.compare(this.getFitness(), other.getFitness());
		}
		public boolean equals (Specimen other) {
			return this.compareTo(other)==0;
		}
		@Override
		public void run() {
			cb.startAI();;
			while (!engine.isGameLost() && shapeCount < 150) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Game over...");
			this.stop();
			/*try {
				this.wait();
			} catch (InterruptedException e) {
				System.out.println("Thread " + name + " interrupted");
				e.printStackTrace();
			}*/
		}
	}
}
