package org.vitrivr.cineast.core.color;

import java.util.ArrayList;
import java.util.List;

public class ReadableXYZContainer extends AbstractColorContainer<ReadableXYZContainer> implements Cloneable{

	protected float x, y, z;
	
	public ReadableXYZContainer(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ReadableXYZContainer(double x, double y, double z){
		this((float)x, (float)y, (float)z);
	}
	
	@Override
	public String toString() {
		return "XYZContainer(" + x + ", " + y + ", " + z + ")";
	}

	@Override
	public float getElement(int num) {
		switch (num) {
		case 0: return x;
		case 1: return y;
		case 2: return z;
		default: throw new IndexOutOfBoundsException(num + ">= 3");
		}
	}
	
	public String toFeatureString() {
		return "<" + x + ", " + y + ", " + z + ">";
	}
	
	public float getX(){
		return this.x;
	}
	
	public float getY(){
		return this.y;
	}
	
	public float getZ(){
		return this.z;
	}

	@Override
	public float[] toArray(float[] arr) {
		if(arr != null && arr.length == 3){
			arr[0] = x;
			arr[1] = y;
			arr[2] = z;
			return arr;
		}
		return new float[]{x, y, z};
	}
	
	@Override
	public List<Float> toList(List<Float> list) {
		if(list != null){
			list.clear();
		}else{
			list = new ArrayList<>(3);
		}
		list.add(x);
		list.add(y);
		list.add(z);
		return list;
	}
}
