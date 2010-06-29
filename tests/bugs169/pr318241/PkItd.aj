package somepackage;

privileged aspect PkItd {

	declare parents: Pk implements java.io.Serializable;

	private static final long Pk.serialVersionUID = -3602111784930992656L;
}
