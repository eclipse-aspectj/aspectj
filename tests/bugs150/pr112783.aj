public aspect pr112783 {

	public NotYetABean.new() {
		this(new Long(1)); 
	}

}

class NotYetABean {

	public NotYetABean(Long l) {		
	}
}
