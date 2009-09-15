package org.aspectj.apache.bcel.classfile.annotation;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;

public abstract class RuntimeParamAnnos extends Attribute {
	
	private List /*Annotation[]*/<AnnotationGen[]> parameterAnnotations;
	private boolean visible;
	

	// Keep just a byte stream of the data until someone actually asks for it
	private boolean inflated = false;
	private byte[] annotation_data;

	  
	public RuntimeParamAnnos(byte attrid, boolean visible,
            int nameIdx, int len, ConstantPool cpool) {
		super(attrid,nameIdx,len,cpool);
		this.visible = visible; 
		parameterAnnotations = new ArrayList<AnnotationGen[]>();
	}
	
	public RuntimeParamAnnos(byte attrid,boolean visible,int nameIdx,int len,byte[] data,ConstantPool cpool) {
		super(attrid,nameIdx,len,cpool);
		this.visible = visible;
		parameterAnnotations = new ArrayList<AnnotationGen[]>();
		annotation_data = data;
	}
	
	public final void dump(DataOutputStream dos) throws IOException {
	  super.dump(dos);
	  writeAnnotations(dos);
	}	  

	public Attribute copy(ConstantPool constant_pool) {
	  	throw new RuntimeException("Not implemented yet!");
	}
	  
	/** Return a list of Annotation[] - each list entry contains the annotations for one parameter */
	public List /*Annotation[]*/<AnnotationGen[]> getParameterAnnotations() {
		if (!inflated) inflate();
		return parameterAnnotations;
	}
	
	public AnnotationGen[] getAnnotationsOnParameter(int parameterIndex) {
		if (!inflated) inflate();
		// This may happen.  In a ctor for a non static inner type the compiler
		// may have added an extra parameter to the generated ctor (the parameter
		// contains the instance of the outer class) - in this case
		// it may appear that there are more parameters than there are entries
		// in the parameter annotations array
		if (parameterIndex>=parameterAnnotations.size()) {
			return AnnotationGen.NO_ANNOTATIONS;
		}
		return parameterAnnotations.get(parameterIndex);
	}
	
	public boolean areVisible() {
		return visible;
	}

	protected void readParameterAnnotations(DataInputStream dis,ConstantPool cpool) throws IOException {
		annotation_data = new byte[length];
		dis.read(annotation_data,0,length);
	}

	private void inflate() {
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotation_data));
			int numParameters = dis.readUnsignedByte();
			for (int i=0; i<numParameters; i++) {
				int numAnnotations = dis.readUnsignedShort();
				AnnotationGen[] annotations = new AnnotationGen[numAnnotations];
				for (int j=0; j<numAnnotations; j++) {
					annotations[j] = AnnotationGen.read(dis,getConstantPool(),visible);
				}
				parameterAnnotations.add(annotations);
			}
			inflated = true;
		} catch (IOException ioe) {
			throw new RuntimeException("Unabled to inflate annotation data, badly formed?");
		}
	}

	
	protected void writeAnnotations(DataOutputStream dos) throws IOException {
		if (!inflated) {
			dos.write(annotation_data,0,length);
		} else {
			dos.writeByte(parameterAnnotations.size());
			for (int i=0; i<parameterAnnotations.size(); i++) {
				AnnotationGen[] annotations = parameterAnnotations.get(i);
				dos.writeShort(annotations.length);
				for (int j=0; j<annotations.length;j++) {
					annotations[j].dump(dos);
				}
			}
		}
	}
	
	/** FOR TESTING ONLY: Tells you if the annotations have been inflated to an object graph */
	public boolean isInflated() {
		return inflated;
	}

	public String toString() {
		return "Runtime"+(visible?"Visible":"Invisible")+"ParameterAnnotations ["+(inflated?"inflated":"not yet inflated")+"]";
	}

}
