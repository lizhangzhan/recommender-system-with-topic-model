package rs.text.recommender;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import cc.mallet.types.InstanceList;

import rs.text.topics.NetPLSA;
import rs.text.topics.RelationalTopicModel;
import rs.util.vlc.Task1Solution;

public class NetPLsaRecommender {
	public InstanceList documents;
	public Task1Solution solver;
	public static final int testIndexStart = RelationalTopicModel.testIndexStart;
	
	public NetPLSA model;
	
	public NetPLsaRecommender(InstanceList completeDocuments, NetPLSA plsa) {
		this.documents = completeDocuments;
		this.model = plsa;
		initSolver();
	}
	
	public void initSolver() {
		solver = new Task1Solution(documents) {
			public String recommend(String qId) {
				int qdocId = idHash.get(qId);
				
				int test_size = documents.size() - testIndexStart;
				double[] predSim = new double[test_size];
				Arrays.fill(predSim, 0);
				for(int i=0; i<test_size; i++) {
					predSim[i] = queryVSM(qdocId, i);		
				}
				String line = sortRecommendList(qdocId, predSim);
				return line;
			}
		};
	}

	protected double queryVSM(int qdocId, int tdocId) {
		// TODO Auto-generated method stub
		double[] v1 = model.p_theta_d[qdocId];
		double[] v2 = model.test_p_theta_d[tdocId];
		return rs.util.vlc.Util.cosineProduct(v1, v2);
	}
	
	public void retrieveTask1Solution(String queryFile, String solutionFile) {
		if(solver == null) initSolver();
		try {
			solver.retrieveTask1Solution(queryFile, solutionFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String trainMalletFile = "dataset/vlc/vlc_train.en.f8.mallet";
		String testMalletFile = "dataset/vlc/vlc_test.en.f8.mallet";
//		String trainMalletFile = "dataset/vlc/vlc_train.title.f2.mallet";
//		String testMalletFile = "dataset/vlc/vlc_test.title.f2.mallet";
		
//		String fullMalletFile = "dataset/vlc/vlc_lectures.all.title.en.f2.mallet";
		String fullMalletFile = "dataset/vlc/vlc_lectures.all.en.f8.mallet";
		String simFile = "dataset/vlc/sim_0p_100n.csv";
		
		String queryFile = "dataset/vlc/task1_query.en.f8.n5.txt";
		String targetFile = "dataset/vlc/task1_target.en.f8.n5.txt";
//		String queryFile = "dataset/vlc/task1_query.en.title.f2.txt";
//		String targetFile = "dataset/vlc/task1_target.en.title.f2.txt";
//		String solutionFile = "dataset/task1_solution.en.title.f2.tfidf.txt";
		String solutionFile = "dataset/task1_solution.en.title.f2.netplsa.txt";

		double lambda = 0.01;
		double gamma = 0.01;
		int numOfTopics = 160;
		NetPLSA netPlsa = new NetPLSA(numOfTopics, lambda, gamma, trainMalletFile, simFile);
		netPlsa.train(30);
//		netPlsa.printTopWords(10, true);
		
		InstanceList testDocuments = InstanceList.load(new File(testMalletFile));
		InstanceList allDocuments = InstanceList.load(new File(fullMalletFile));
		
		netPlsa.test(testDocuments, 100);
		
		NetPLsaRecommender recommender = new NetPLsaRecommender(allDocuments, netPlsa);
		recommender.retrieveTask1Solution(queryFile, solutionFile);
		try {
			double precision = Task1Solution.evaluateResult(targetFile, solutionFile);
			System.out.println("NetPLSA precision: " + precision);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}