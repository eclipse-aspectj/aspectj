package org.aspectj.apache.bcel.classfile.annotation;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;

public abstract class RuntimeAnnotations extends Attribute {

	private List /*Annotation*/ annotations;
	private boolean visible;
	
	// Keep just a byte stream of the data until someone actually asks for it
	private boolean inflated = false;
	private byte[] annotation_data;
	
	public RuntimeAnnotations(byte attrid, boolean visible,
			                  int nameIdx, int len, 
							  ConstantPool cpool) {
		super(attrid,nameIdx,len,cpool);
		this.visible = visible; 
		annotations = new ArrayList();
	}
	
	public RuntimeAnnotations(byte attrid,boolean visible,int nameIdx,int len,byte[] data,ConstantPool cpool) {
		super(attrid,nameIdx,len,cpool);
		this.visible = visible;
		annotations = new ArrayList();
		annotation_data = data;
	}
	
	public List getAnnotations() {
		if (!inflated) inflate();
		return annotations;
	}
	
	public boolean areVisible() {
		return visible;
	}

	protected void readAnnotations(DataInputStream dis,ConstantPool cpool) throws IOException {
		annotation_data = new byte[length];
		dis.read(annotation_data,0,length);
	}

	private void inflate() {
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotation_data));
			int numberOfAnnotations = dis.readUnsignedShort();		
			for (int i = 0 ;i<numberOfAnnotations;i++) {
				annotations.add(Annotation.read(dis,getConstantPool(),visible));
			}
			dis.close();
			inflated = true;
		} catch (IOException ioe) {
			throw new RuntimeException("Unabled to inflate annotation data, badly formed? ");
		}
	}
	
	protected void writeAnnotations(DataOutputStream dos) throws IOException {
		if (!inflated) {
			dos.write(annotation_data,0,length);
		} else {
			dos.writeShort(annotations.size());
			for (Iterator i = annotations.iterator(); i.hasNext();) {
				Annotation ann = (Annotation) i.next();
				ann.dump(dos);
			}
		}
	}
	
	/** FOR TESTING ONLY: Tells you if the annotations have been inflated to an object graph */
	public boolean isInflated() {
		return inflated;
	}
	
}
