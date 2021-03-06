package org.vitrivr.cineast.core.config;

import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

import java.util.Optional;

public class QueryConfig {

	public static enum Distance{
		chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, minkowski, spannorm, haversine
	}
	
	private Distance distance = null;
	private float[] distanceWeights = null;
	private float norm = Float.NaN;
	private NeuralNet net = null;
	private float classificationCutoff = Float.NaN;
	
	public QueryConfig(){}
	
	public QueryConfig(QueryConfig qc){
		if(qc == null){
			return;
		}
		this.distance = qc.distance;
		this.distanceWeights = qc.distanceWeights;
		this.norm = qc.norm;
		this.net = qc.net;
		this.classificationCutoff = qc.classificationCutoff;
	}

	public Optional<NeuralNet> getNet() {
		return Optional.ofNullable(this.net);
	}

	public void setNet(NeuralNet net) {
		this.net = net;
	}

	public Optional<Float> getCutoff() { return Optional.ofNullable(Float.isNaN(classificationCutoff) ? null : classificationCutoff);}

	public Optional<Distance> getDistance(){
		return Optional.ofNullable(this.distance);
	}
	
	public Optional<float[]> getDistanceWeights(){
		return Optional.ofNullable(this.distanceWeights);
	}
	
	public Optional<Float> getNorm(){
		return Optional.ofNullable(Float.isNaN(norm) ? null : norm);
	}
	
	public QueryConfig setDistance(Distance distance){
		this.distance = distance;
		if(distance == Distance.euclidean){
			this.norm = 2f;
		}
		else if(distance == Distance.manhattan){
			this.norm = 1f;
		}
		else if(distance == Distance.chebyshev){
			this.norm = Float.POSITIVE_INFINITY;
		}
		return this;
	}
	
	public QueryConfig setDistanceIfEmpty(Distance distance){
	  if(this.distance == null){
	    return setDistance(distance);
	  }
	  return this;
	}
	
	public QueryConfig setDistanceWeights(float[] weights){
		this.distanceWeights = weights;
		return this;
	}
	
	public QueryConfig setDistanceWeightsIfEmpty(float[] weights){
	  if(this.distanceWeights == null){
	    return setDistanceWeights(weights);
	  }
	  return this;
	}
	
	public QueryConfig setNorm(float norm){
		this.norm = norm;
		if(Math.abs(norm - 2f) < 1e6f){
			this.distance = Distance.euclidean;
		}else if(Math.abs(norm - 1f) < 1e6f){
			this.distance = Distance.manhattan;
		}else if(Float.isInfinite(norm) && norm > 0){
			this.distance = Distance.chebyshev;
		}
		return this;
	}
	
	public QueryConfig setNormIfEmty(float norm){
	  if(Float.isNaN(this.norm)){
	    return setNorm(norm);
	  }
	  return this;
	}
	
	public QueryConfig clone(){
		return new QueryConfig(this);
	}
	
	public static QueryConfig clone(QueryConfig qc){
	  return new QueryConfig(qc);
	}

  public static QueryConfig notNull(QueryConfig qc) {
    if(qc == null){
      return new QueryConfig();
    }
    return qc;
  }
}
