package com.shoppingdistrict.microservices.productlistingservice.repository;

public class EdgeDTO {
	
	private int id;
    private int source;
    private int target;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getTarget() {
		return target;
	}
	public void setTarget(int target) {
		this.target = target;
	}
    
    

}
