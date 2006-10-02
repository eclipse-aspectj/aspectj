package java.net;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.aspectj.weaver.loadtime.Aj;

public class URLClassLoader extends SecureClassLoader {
	
	public final static boolean debug = false;
	
	private List path = new LinkedList();
	private Aj agent;

	public URLClassLoader() {
		super();
	}

	public URLClassLoader(ClassLoader parent) {
		super(parent);
	}

	public URLClassLoader(URL[] urls) throws IOException {
		this(urls,null,null);
	}
	
	public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) throws IOException {
		super(parent);
		if (debug) System.err.println("> URLClassLoader.URLClassLoader() parent=" + parent);

		for (int i = 0; i < urls.length; i++) {
			Object pathElement;
			URL url = urls[i];
			if (debug) System.err.println("- URLClassLoader.URLClassLoader() url=" + url.getPath());
			File file = new File(encode(url.getFile()));
			if (debug) System.err.println("- URLClassLoader.URLClassLoader() file" + file);
			if (file.isDirectory()) pathElement = file;
			else if (file.exists() && file.getName().endsWith(".jar")) pathElement = new JarFile(file);
			else throw new RuntimeException(file.getAbsolutePath().toString());
			path.add(pathElement);
		}

		agent = new Aj();
		
		if (debug) System.err.println("< URLClassLoader.URLClassLoader() path=" + path);
	}
	
//	public final static boolean debug = false;
//	
//	private List path = new LinkedList();
////	private com.bea.jvm.ClassPreProcessor agent;
//	private Object agent;
//	private Method preProcess;
	
//	public JRockitClassLoader (URLClassLoader clone) throws Exception {
//		/* Use extensions loader */
//		super(clone.getParent());
//
//		URL[] urls = clone.getURLs();
//		for (int i = 0; i < urls.length; i++) {
//			Object pathElement;
//			URL url = urls[i];
//			if (debug) System.err.println("JRockitClassLoader.JRockitClassLoader() url=" + url.getPath());
//			File file = new File(encode(url.getFile()));
//			if (debug) System.err.println("JRockitClassLoader.JRockitClassLoader() file" + file);
//			if (file.isDirectory()) pathElement = file;
//			else if (file.exists() && file.getName().endsWith(".jar")) pathElement = new JarFile(file);
//			else throw new RuntimeException(file.getAbsolutePath().toString());
//			path.add(pathElement);
//		}
//		
//		Class agentClazz = Class.forName("org.aspectj.weaver.loadtime.JRockitAgent",false,this);
//		Object obj = agentClazz.newInstance();
//		if (debug) System.err.println("JRockitClassLoader.JRockitClassLoader() obj=" + obj);
//		this.agent = obj;
//		byte[] bytes = new byte[] {};
//		Class[] parameterTypes = new Class[] { java.lang.ClassLoader.class, java.lang.String.class, bytes.getClass() }; 
//		preProcess = agentClazz.getMethod("preProcess",parameterTypes);
//	}
	
	/* Get rid of escaped characters */
	private String encode (String s) {
		StringBuffer result = new StringBuffer();
		int i = s.indexOf("%");
		while (i != -1) {
			result.append(s.substring(0,i));
			String escaped = s.substring(i+1,i+3);
			s = s.substring(i+3);
			Integer value = Integer.valueOf(escaped,16);
			result.append(new Character((char)value.intValue()));
			i = s.indexOf("%");
		}
		result.append(s);
		return result.toString();
	}
	
	protected Class findClass(String name) throws ClassNotFoundException {
		if (debug) System.err.println("> URLClassLoader.findClass() name=" + name);
		Class clazz = null;
		try {
			clazz = super.findClass(name);
		}
		catch (ClassNotFoundException ex) {
			for (Iterator i = path.iterator(); clazz ==  null && i.hasNext();) {
				byte[] classBytes = null;
				try {
					Object pathElement = i.next();
					if (pathElement instanceof File) {
						File dir = (File)pathElement;
						String className = name.replace('.','/') + ".class";
						File classFile = new File(dir,className);
						if (debug) System.err.println("- URLClassLoader.findClass() classFile=" + classFile);
						if (classFile.exists()) classBytes = loadClassFromFile(name,classFile);
					}
					else {
						JarFile jar = (JarFile)pathElement;
						String className = name.replace('.','/') + ".class";
						ZipEntry entry = jar.getEntry(className);
						if (entry != null) classBytes = loadBytesFromZipEntry(jar,entry);
					}
					
					if (classBytes != null) {
						clazz = defineClass(name,classBytes);
					}
				}
				catch (IOException ioException) {
					ex.printStackTrace();
				}
			}
		}
		
		if (debug) System.err.println("< URLClassLoader.findClass() clazz=" + clazz);
		if (clazz == null) throw new ClassNotFoundException(name);
		return clazz;
	}

	protected URL findResource (String name) {
		if (debug) System.err.println("> URLClassLoader.findResource() name=" + name);
		URL url = null;

		try {
			Enumeration enu = findResources(name);
			if (enu.hasMoreElements()) url = (URL)enu.nextElement();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

		if (debug) System.err.println("< URLClassLoader.findResource() url=" + url);
		return url;
	}
	
	
	protected Enumeration findResources (String name) throws IOException {
		if (debug) System.err.println("> URLClassLoader.findResources() name=" + name);
		Vector urls = new Vector();
		
		for (Iterator i = path.iterator(); i.hasNext();) {
			Object pathElement = i.next();
			if (pathElement instanceof File) {
				File dir = (File)pathElement;
				File resourceFile = new File(dir,name);
//				if (debug) System.err.println("- URLClassLoader.findResources() file=" + resourceFile);
			}
			else {
				JarFile jar = (JarFile)pathElement;
				ZipEntry entry = jar.getEntry(name);
//				if (debug) System.err.println("- URLClassLoader.findResources() entry=" + entry);
				if (entry != null) {
					if (debug) System.err.println("- URLClassLoader.findResources() jar=" + jar.getName());

					final byte[] bytes = loadBytesFromZipEntry(jar,entry);
					URLStreamHandler streamHandler = new URLStreamHandler() {
					
						protected URLConnection openConnection(URL u) throws IOException {
							URLConnection connection = new URLConnection(u) {
								
								public void connect() throws IOException {
								}

								public InputStream getInputStream() throws IOException {
									return new ByteArrayInputStream(bytes);
								}
								
							};
							return connection;
					    }
					
					};
					URL url = new URL("file",null,0,jar.getName(),streamHandler);
					urls.add(url);
				}
			}
		}
		
		Enumeration enu = urls.elements();
		
		if (debug) System.err.println("< URLClassLoader.findResources() enu=" + enu);
		return enu;
	}
	
	private Class defineClass (String name, byte[] bytes) {
		if (debug) System.err.println("> URLClassLoader.defineClass() name=" + name);
//		try {
			if (agent != null) bytes = agent.preProcess(name,bytes,this);
//		}
//		catch (IllegalAccessException iae) {
//			iae.printStackTrace();
//			throw new ClassFormatError(iae.getMessage());
//		}
//		catch (InvocationTargetException ite) {
//			ite.printStackTrace();
//			throw new ClassFormatError(ite.getTargetException().getMessage());
//		}
		if (debug) System.err.println("< URLClassLoader.defineClass() name=" + name);
		return super.defineClass(name,bytes,0,bytes.length);
	}
	
	private byte[] loadClassFromFile (String name, File file) throws IOException {
		if (debug) System.err.println("> URLClassLoader.loadClassFromFile() file=" + file);

		byte[] bytes;
		bytes = new byte[(int)file.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			bytes = readBytes(fis,bytes);
		}
		finally {
			if (fis != null) fis.close();
		}
		
		if (debug) System.err.println("< URLClassLoader.loadClassFromFile() bytes=b[" + bytes.length + "]");
		return bytes;
	}
	
	private byte[] loadBytesFromZipEntry (JarFile jar, ZipEntry entry) throws IOException {
		if (debug) System.err.println("> URLClassLoader.loadBytesFromZipEntry() entry=" + entry);

		byte[] bytes;
		bytes = new byte[(int)entry.getSize()];
		InputStream is = null;
		try {
			is = jar.getInputStream(entry);
			bytes = readBytes(is,bytes);
		}
		finally {
			if (is != null) is.close();
		}
		
		if (debug) System.err.println("< URLClassLoader.loadBytesFromZipEntry() bytes=b[" + bytes.length + "]");
		return bytes;
	}
	
	private byte[] readBytes (InputStream is, byte[] bytes) throws IOException {
		for (int offset = 0; offset < bytes.length;) {
			int read = is.read(bytes,offset,bytes.length - offset);
			offset += read;
		}
		return bytes;
	}

}
