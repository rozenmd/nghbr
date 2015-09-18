package com.kmji.nghbr.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.SessionFactory;

@Entity
@Table(name="APP_MESSAGE")
public class Message {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(name="Text", nullable=true)
	private String text;
	
	@Column(name="Username", nullable=false)
	private String username;
	
	@Column(name="PostCode", nullable=false)
	private int postCode;
	
//	@Column(name="PostCode", nullable=false)
//	private String postCode;
	
	@Column(name="STATE")
	private String state=State.ACTIVE.getState();
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public int getPostCode() {
		return postCode;
	}
	
	public void setPostCode(int postCode) {
		this.postCode = postCode;
	}

	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof User))
			return false;
		Message other = (Message) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Message [id=" + id + ", username=" + username + ", message text=" + text + ", state=" + state +"]";
		
	}

}